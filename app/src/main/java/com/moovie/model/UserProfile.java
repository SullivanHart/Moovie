package com.moovie.model;

import com.google.firebase.firestore.IgnoreExtraProperties;

/**
 * Model class for a user profile.
 */
@IgnoreExtraProperties
public class UserProfile {
    private String userId;
    private long createdAt;

    /**
     * Default constructor for UserProfile.
     */
    public UserProfile() {
    }

    /**
     * Constructor for UserProfile.
     * @param userId The unique user ID.
     */
    public UserProfile(String userId) {
        this.userId = userId;
        this.createdAt = System.currentTimeMillis();
    }

    /**
     * Gets the user ID.
     * @return The user ID.
     */
    public String getUserId() { return userId; }

    /**
     * Sets the user ID.
     * @param userId The user ID to set.
     */
    public void setUserId(String userId) { this.userId = userId; }

    /**
     * Gets the creation timestamp.
     * @return The timestamp in milliseconds.
     */
    public long getCreatedAt() { return createdAt; }

    /**
     * Sets the creation timestamp.
     * @param createdAt The timestamp in milliseconds.
     */
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
