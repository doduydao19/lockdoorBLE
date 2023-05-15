package com.example.myapplication.data.model;

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
public class LoggedInUser {

    private String userId;
    private String displayName;
    public String roomNumber;
    public String linkLocal;

    public LoggedInUser(String userId, String displayName, String roomNumber, String linkLocal) {
        this.userId = userId;
        this.displayName = displayName;
        this.roomNumber = roomNumber;
        this.linkLocal  = linkLocal;
    }

    public String getUserId() {
        return userId;
    }

    public String getDisplayName() {
        return displayName;
    }
}