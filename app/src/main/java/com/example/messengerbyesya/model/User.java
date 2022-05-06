package com.example.messengerbyesya.model;

import com.google.firebase.firestore.PropertyName;

public class User {
    private String email;
    private String password;
    private String name;
    private String avatar;
    private boolean isUserOnline;

    public User(){

    }

    public User(String email, String password, String name, String avatar) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.avatar = avatar;
        this.isUserOnline = false;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    @PropertyName("is_user_online")
    public boolean isUserOnline() {
        return isUserOnline;
    }

    @PropertyName("is_user_online")
    public void setUserOnline(boolean userOnline) {
        isUserOnline = userOnline;
    }
}
