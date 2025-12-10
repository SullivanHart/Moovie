/**
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 package com.moovie.adapter;

import android.util.Log;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

/**
 * RecyclerView adapter for displaying the results of a Firestore {@link Query}.
 *
 * Note that this class forgoes some efficiency to gain simplicity. For example, the result of
 * {@link DocumentSnapshot#toObject(Class)} is not cached so the same object may be deserialized
 * many times as the user scrolls.
 * 
 * See the adapter classes in FirebaseUI (https://github.com/firebase/FirebaseUI-Android/tree/master/firestore) for a
 * more efficient implementation of a Firestore RecyclerView Adapter.
 */
public abstract class FirestoreAdapter<VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> implements EventListener<QuerySnapshot> {

    private static final String TAG = "Firestore Adapter";

    private Query mQuery;
    private ListenerRegistration mRegistration;

    private ArrayList<DocumentSnapshot> mSnapshots = new ArrayList<>();

    /**
     * Constructor for FirestoreAdapter.
     * @param query The Firestore query to listen to.
     */
    public FirestoreAdapter(Query query) {
        mQuery = query;
    }

    /**
     * Starts listening for Firestore updates.
     */
    public void startListening() {
        if (mQuery != null && mRegistration == null) {
            mRegistration = mQuery.addSnapshotListener(this);
        }
    }

    /**
     * Stops listening for Firestore updates.
     */
    public void stopListening() {
        if (mRegistration != null) {
            mRegistration.remove();
            mRegistration = null;
        }

        mSnapshots.clear();
        notifyDataSetChanged();
    }

    /**
     * Sets a new query and starts listening to it.
     * @param query The new Firestore query.
     */
    public void setQuery(Query query) {
        // Stop listening
        stopListening();

        // Clear existing data
        mSnapshots.clear();
        notifyDataSetChanged();

        // Listen to new query
        mQuery = query;
        startListening();
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return mSnapshots.size();
    }

    /**
     * Gets the DocumentSnapshot at the specified index.
     * @param index The index of the snapshot.
     * @return The DocumentSnapshot at the specified index.
     */
    protected DocumentSnapshot getSnapshot(int index) {
        return mSnapshots.get(index);
    }

    /**
     * Called when an error occurs while listening to the query.
     * @param e The exception that occurred.
     */
    protected void onError(FirebaseFirestoreException e) {};

    /**
     * Called when the data has changed.
     */
    protected void onDataChanged() {}

    // Add this method
    /**
     * Called when the Firestore query returns a snapshot.
     * @param documentSnapshots The new query snapshot.
     * @param e The exception that occurred, if any.
     */
    @Override
    public void onEvent(QuerySnapshot documentSnapshots,
                        FirebaseFirestoreException e) {

        Log.d(TAG, "onEvent()");

        // Handle errors
        if (e != null) {
            Log.w(TAG, "onEvent:error", e); return;
        }
        // Dispatch the event
        for (DocumentChange change : documentSnapshots.getDocumentChanges()) {
            // Snapshot of the changed document
            DocumentSnapshot snapshot = change.getDocument();
            switch (change.getType()) {
                case ADDED:
                    onDocumentAdded( change );
                    break;
                case MODIFIED:
                    onDocumentModified( change );
                    break;
                case REMOVED:
                    onDocumentRemoved( change );
                    break;
            }
        }
        onDataChanged();
    }

    /**
     * Called when a document is added.
     * @param change The document change event.
     */
    protected void onDocumentAdded(DocumentChange change) {
        mSnapshots.add(change.getNewIndex(), change.getDocument());
        notifyItemInserted(change.getNewIndex());
    }

    /**
     * Called when a document is modified.
     * @param change The document change event.
     */
    protected void onDocumentModified(DocumentChange change) {
        if (change.getOldIndex() == change.getNewIndex()) {
            // Item changed but remained in same position
            mSnapshots.set(change.getOldIndex(), change.getDocument());
            notifyItemChanged(change.getOldIndex());
        } else {
            // Item changed and changed position
            mSnapshots.remove(change.getOldIndex());
            mSnapshots.add(change.getNewIndex(), change.getDocument());
            notifyItemMoved(change.getOldIndex(), change.getNewIndex());
        }
    }

    /**
     * Called when a document is removed.
     * @param change The document change event.
     */
    protected void onDocumentRemoved(DocumentChange change) {
        mSnapshots.remove(change.getOldIndex());
        notifyItemRemoved(change.getOldIndex());
    }

}
