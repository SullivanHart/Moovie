package com.moovie.model;

import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class UserProfile {
    private String userId;
    private long createdAt;

    public UserProfile() {
    }

    public UserProfile(String userId) {
        this.userId = userId;
        this.createdAt = System.currentTimeMillis();
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
