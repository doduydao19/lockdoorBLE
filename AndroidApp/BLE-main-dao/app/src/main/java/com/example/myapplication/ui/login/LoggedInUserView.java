package com.example.myapplication.ui.login;

/**
 * Class exposing authenticated user details to the UI.
 */
class LoggedInUserView {
    private String displayName;
    //... other data fields that may be accessible to the UI
    public String roomNumber;
    public String linkLocal;
    LoggedInUserView(String displayName, String roomNumber, String linkLocal) {
        this.displayName = displayName;
        this.roomNumber = roomNumber;
        this.linkLocal = linkLocal;
    }

    String getDisplayName() {
        return displayName;
    }
}