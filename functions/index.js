const { onDocumentWritten } = require("firebase-functions/v2/firestore");
const admin = require("firebase-admin");

admin.initializeApp();
const db = admin.firestore();

exports.updateMovieAverageRank = onDocumentWritten(
  "users/{userId}/watched/{watchedDocId}",
  async (event) => {
    const startTime = Date.now();
    console.log("=== FUNCTION TRIGGERED ===");
    console.log("User ID:", event.params.userId);
    console.log("Watched Doc ID:", event.params.watchedDocId);

    // Get the tmdbId from the document data (NOT the document ID)
    const afterData = event.data?.after?.data();
    const beforeData = event.data?.before?.data();

    console.log("After data:", afterData);
    console.log("Before data:", beforeData);

    const tmdbId = afterData?.tmdbId || beforeData?.tmdbId;

    if (!tmdbId) {
      console.error("❌ No tmdbId found in document data. Cannot proceed.");
      return null;
    }

    console.log(`Processing movie with tmdbId: ${tmdbId} (type: ${typeof tmdbId})`);

    try {
      // STEP 1: Query all watched documents across all users with this tmdbId
      console.log(`\n=== QUERYING collectionGroup('watched') for tmdbId=${tmdbId} ===`);

      const snapshot = await db
        .collectionGroup("watched")
        .where("tmdbId", "==", tmdbId)
        .get();

      console.log(`✓ Query returned ${snapshot.size} documents`);

      // STEP 2: Collect all rankings for this movie
      console.log("\n=== COLLECTING RANKINGS ===");
      const rankings = [];

      snapshot.forEach((doc) => {
        const data = doc.data();
        console.log(`  - Doc ${doc.id}: rankIndex=${data.rankIndex}, ranked=${data.ranked}, title="${data.title}"`);

        // Only include movies that are ranked (ranked=true) and have a valid rankIndex
        if (data.ranked === true && typeof data.rankIndex === "number" && data.rankIndex >= 0) {
          rankings.push(data.rankIndex);
        } else {
          console.log(`    ⚠️ Skipping - not ranked or invalid rankIndex`);
        }
      });

      if (rankings.length === 0) {
        console.log("⚠️ No valid rankings found");
        return null;
      }

      // STEP 3: Calculate average rank position
      const avgRankIndex = rankings.reduce((sum, rank) => sum + rank, 0) / rankings.length;
      console.log(`\n📊 Average rankIndex: ${avgRankIndex} (from ${rankings.length} users)`);

      // STEP 4: Get all user lists to determine percentile
      console.log("\n=== CALCULATING PERCENTILE ===");

      // Get total count of watched movies across all users to establish context
      const allWatchedSnapshot = await db.collectionGroup("watched").get();
      const allRankings = [];

      allWatchedSnapshot.forEach((doc) => {
        const data = doc.data();
        if (typeof data.rankIndex === "number") {
          allRankings.push(data.rankIndex);
        }
      });

      // Sort rankings (lower rankIndex = better ranking = higher percentile)
      allRankings.sort((a, b) => a - b);

      console.log(`Total movies ranked across all users: ${allRankings.length}`);
      console.log(`This movie's avg rankIndex: ${avgRankIndex}`);

      // Find percentile (what % of movies are ranked worse than this one)
      const worseCount = allRankings.filter(rank => rank > avgRankIndex).length;
      const percentile = allRankings.length > 0 ? (worseCount / allRankings.length) * 100 : 0;

      console.log(`Movies ranked worse: ${worseCount}/${allRankings.length}`);
      console.log(`Percentile: ${percentile.toFixed(1)}%`);

      // STEP 5: Convert percentile to star rating
      // Top 10% = 5 stars
      // 10-25% = 4.5 stars
      // 25-45% = 4 stars
      // 45-65% = 3.5 stars
      // 65-80% = 3 stars
      // 80-90% = 2.5 stars
      // 90-95% = 2 stars
      // 95-98% = 1.5 stars
      // 98-100% = 1 star
      // Bottom 0-2% = 0.5 stars

      let starRating;
      if (percentile >= 90) {
        starRating = 5.0;
      } else if (percentile >= 75) {
        starRating = 4.5;
      } else if (percentile >= 55) {
        starRating = 4.0;
      } else if (percentile >= 35) {
        starRating = 3.5;
      } else if (percentile >= 20) {
        starRating = 3.0;
      } else if (percentile >= 10) {
        starRating = 2.5;
      } else if (percentile >= 5) {
        starRating = 2.0;
      } else if (percentile >= 2) {
        starRating = 1.5;
      } else if (percentile >= 1) {
        starRating = 1.0;
      } else {
        starRating = 0.5;
      }

      console.log(`⭐ Star rating: ${starRating} stars`);

      // STEP 3: Find the movie document with this tmdbId
      console.log(`\n=== FINDING movie document with tmdbId=${tmdbId} ===`);

      const movieQuery = await db
        .collection("movies")
        .where("tmdbId", "==", tmdbId)
        .limit(1)
        .get();

      if (movieQuery.empty) {
        console.log("⚠️ No movie document found with this tmdbId.");
        console.log("You may need to create the movie document first.");
        return null;
      }

      const movieDoc = movieQuery.docs[0];
      console.log(`✓ Found movie document: ${movieDoc.id}`);

      // STEP 7: Update the movie document
      console.log("\n=== UPDATING MOVIE DOCUMENT ===");
      await movieDoc.ref.update({
        avgRanking: starRating,
        numRankings: rankings.length,
        avgRankIndex: avgRankIndex,
        percentile: Math.round(percentile * 10) / 10, // Round to 1 decimal
        lastUpdated: admin.firestore.FieldValue.serverTimestamp()
      });

      console.log("✅ SUCCESS! Updated movie:", {
        docId: movieDoc.id,
        tmdbId: tmdbId,
        starRating: starRating,
        numRankings: rankings.length,
        percentile: `${percentile.toFixed(1)}%`
      });

      // STEP 8: Verify the write
      const verifyDoc = await movieDoc.ref.get();
      const verifyData = verifyDoc.data();
      console.log("✓ Verification:", {
        avgRanking: verifyData.avgRanking,
        numRankings: verifyData.numRankings,
        percentile: verifyData.percentile
      });

      const executionTime = Date.now() - startTime;
      console.log(`\n⏱️ Total execution time: ${executionTime}ms`);

      return null;

    } catch (error) {
      console.error("\n❌ === ERROR DETAILS ===");
      console.error("Error name:", error.name);
      console.error("Error message:", error.message);
      console.error("Error code:", error.code);
      
      if (error.code === 9) {
        console.error("\n🔧 SOLUTION: You need to create a Firestore index!");
        console.error("Check the Firebase Console logs for an index creation link.");
        console.error("Or manually create a composite index on collection group 'watched' with field 'tmdbId'");
      }
      
      console.error("\nFull error:", error);
      throw error;
    }
  }
);