package com.example.messengerbyesya.model;

import java.util.ArrayList;
import java.util.Date;

public class Chat {

    private Date dateOfLastMessage;
    private ArrayList<String> usersEmails;
    private String chatId;

    public Chat(){
        usersEmails = new ArrayList<>();
    }

    public Chat(Date dateOfLastMessage, ArrayList<String> usersEmails) {
        this.usersEmails = new ArrayList<>();
        this.dateOfLastMessage = dateOfLastMessage;
        this.usersEmails = usersEmails;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public ArrayList<String> getUsersEmails() {
        return usersEmails;
    }

    public void setUsersEmails(ArrayList<String> usersEmails) {
        this.usersEmails = usersEmails;
    }

    public Date getDateOfLastMessage() {
        return dateOfLastMessage;
    }

    public void setDateOfLastMessage(Date dateOfLastMessage) {
        this.dateOfLastMessage = dateOfLastMessage;
    }


}
