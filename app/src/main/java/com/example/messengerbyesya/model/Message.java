package com.example.messengerbyesya.model;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;

import java.util.Date;

public class Message {
    private String text;
    private Date date;
    private User sender;
    private String senderEmail;
    private Boolean isMediaResource;

    public Message() {
    }

    public Message(String text, Date date, User sender, String senderEmail) {
        this.text = text;
        this.date = date;
        this.sender = sender;
        this.senderEmail = senderEmail;
        this.isMediaResource = false;
    }

    public Message(String text, Date date, String senderEmail) {
        this.text = text;
        this.date = date;
        this.senderEmail = senderEmail;
        this.isMediaResource = false;
    }

    @PropertyName("sender_email")
    public String getSenderEmail() {
        return senderEmail;
    }

    @PropertyName("sender_email")
    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
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

    @PropertyName("is_media_resource")
    public Boolean getMediaResource() {
        return isMediaResource;
    }

    @PropertyName("is_media_resource")
    public void setMediaResource(Boolean mediaResource) {
        isMediaResource = mediaResource;
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
