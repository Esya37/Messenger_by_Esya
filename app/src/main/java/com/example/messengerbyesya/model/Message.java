package com.example.messengerbyesya.model;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.Date;

public class Message {
    private String text;
    private Date date;
    private User sender;
    private String sender_email;

    public Message() {
    }

    public Message(String text, Date date, User sender, String sender_email) {
        this.text = text;
        this.date = date;
        this.sender = sender;
        this.sender_email = sender_email;
    }

    public Message(String text, Date date, String sender_email) {
        this.text = text;
        this.date = date;
        this.sender_email = sender_email;
    }

    public String getSender_email() {
        return sender_email;
    }

    public void setSender_email(String sender_email) {
        this.sender_email = sender_email;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Exclude
    public User getSender() {
        return sender;
    }

    @Exclude
    public void setSender(User sender) {
        this.sender = sender;
    }
}
