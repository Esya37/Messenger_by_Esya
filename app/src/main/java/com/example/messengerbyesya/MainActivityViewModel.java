package com.example.messengerbyesya;

import androidx.lifecycle.ViewModel;

import com.example.messengerbyesya.model.Message;
import com.example.messengerbyesya.model.User;

import java.util.ArrayList;
import java.util.List;

public class MainActivityViewModel extends ViewModel {

    private User currentUser;
    private String currentUserId;
    private List<Message> messages;

    public MainActivityViewModel() {
        messages = new ArrayList<>();
    }

    public void addMessage(Message message){
        messages.add(message);
    }

    public String getCurrentUserId() {
        return currentUserId;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
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
