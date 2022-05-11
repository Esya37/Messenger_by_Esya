package com.example.messengerbyesya.model;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Chat {

    private Date dateOfLastMessage;
    private ArrayList<String> usersEmails;
    private Map<String, Long> countOfUncheckedMessages;
    private String chatName;
    private String chatAvatar;
    private String chatId;

    public Chat(){
        usersEmails = new ArrayList<>();
        countOfUncheckedMessages = new HashMap<>();
    }

    public Chat(Date dateOfLastMessage, ArrayList<String> usersEmails) {
        this.usersEmails = new ArrayList<>();
        this.countOfUncheckedMessages = new HashMap<>();
        this.dateOfLastMessage = dateOfLastMessage;
        this.usersEmails = usersEmails;
    }

    @Exclude
    public String getChatId() {
        return chatId;
    }

    @Exclude
    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    @PropertyName("users_emails")
    public ArrayList<String> getUsersEmails() {
        return usersEmails;
    }

    @PropertyName("users_emails")
    public void setUsersEmails(ArrayList<String> usersEmails) {
        this.usersEmails = usersEmails;
    }

    @PropertyName("date_of_last_message")
    public Date getDateOfLastMessage() {
        return dateOfLastMessage;
    }

    @PropertyName("date_of_last_message")
    public void setDateOfLastMessage(Date dateOfLastMessage) {
        this.dateOfLastMessage = dateOfLastMessage;
    }

    @PropertyName("count_of_unchecked_messages")
    public Map<String, Long> getCountOfUncheckedMessages() {
        return countOfUncheckedMessages;
    }

    @PropertyName("count_of_unchecked_messages")
    public void setCountOfUncheckedMessages(Map<String, Long> countOfUncheckedMessages) {
        this.countOfUncheckedMessages = countOfUncheckedMessages;
    }

    @PropertyName("chat_name")
    public String getChatName() {
        return chatName;
    }

    @PropertyName("chat_name")
    public void setChatName(String chatName) {
        this.chatName = chatName;
    }

    @PropertyName("chat_avatar")
    public String getChatAvatar() {
        return chatAvatar;
    }

    @PropertyName("chat_avatar")
    public void setChatAvatar(String chatAvatar) {
        this.chatAvatar = chatAvatar;
    }
}
