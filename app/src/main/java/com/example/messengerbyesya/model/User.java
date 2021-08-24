package com.example.messengerbyesya.model;

public class User {
    private String email;
    private String password;
    private String name;
    private String avatar;

    public User(){

    }

    public User(String email, String password, String name, String avatar) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.avatar = avatar;
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
}
