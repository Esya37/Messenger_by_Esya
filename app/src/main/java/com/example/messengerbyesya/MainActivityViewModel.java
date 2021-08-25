package com.example.messengerbyesya;

import androidx.lifecycle.ViewModel;

import com.example.messengerbyesya.model.User;

public class MainActivityViewModel extends ViewModel {

    private User currentUser;
    private String currentUserId;

    public String getCurrentUserId() {
        return currentUserId;
    }

    public void setCurrentUserId(String currentUserId) {
        this.currentUserId = currentUserId;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }
}
