package com.example.messengerbyesya.model;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.net.Uri;

import androidx.lifecycle.LiveData;

import com.example.messengerbyesya.services.AuthentificationService;
import com.example.messengerbyesya.services.FirestoreService;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.ListResult;

import java.util.List;

public class MessengerRepository {

    private final AuthentificationService authentificationService;
    private final FirestoreService firestoreService;

    public MessengerRepository() {
        authentificationService = new AuthentificationService();
        firestoreService = new FirestoreService();
    }

    public FirebaseUser getCurrentFirebaseUser() {
        return authentificationService.getCurrentFirebaseUser();
    }

    public LiveData<String> createUser(String email, String password) {
        return authentificationService.createUser(email, password);
    }

    public LiveData<String> signInWithEmailAndPassword(String email, String password) {
        return authentificationService.signInWithEmailAndPassword(email, password);
    }

    public void signOut() {
        authentificationService.signOut();
    }

    public LiveData<String> changePassword(String email, String oldPassword, String newPassword) {
        return authentificationService.changePassword(email, oldPassword, newPassword);
    }

    public Task<Uri> getAvatar(User user) {
        return firestoreService.getAvatar(user);
    }

    public User uploadAvatar(Bitmap bitmap, User user, String currentUserId, ProgressDialog pd) {
        return firestoreService.uploadAvatar(bitmap, user, currentUserId, pd);
    }

    public void uploadTempAvatar(Bitmap bitmap, User user) {
        firestoreService.uploadTempAvatar(bitmap, user);
    }

    public Task<Uri> getChatAvatar(String chatAvatarPath) {
        return firestoreService.getChatAvatar(chatAvatarPath);
    }

    public void uploadChatAvatar(Bitmap bitmap, Chat chat, ProgressDialog pd) {
        firestoreService.uploadChatAvatar(bitmap, chat, pd);
    }

    public Task<Uri> getImageDownloadUrl (String imagePath) {
        return firestoreService.getImageDownloadUrl(imagePath);
    }

    public Task<ListResult> getImagesFromFolder(String folderPath) {
        return firestoreService.getImagesFromFolder(folderPath);
    }

    public void setUser(User user, String userId) {
        firestoreService.setUser(user, userId);
    }

    public void setChat(Chat chat) {
        firestoreService.setChat(chat);
    }

    public Task<Void> deleteImage(String imagePath) {
        return firestoreService.deleteImage(imagePath);
    }

    public void sendImage(Message message, Bitmap bitmap, Chat currentChat) {
        firestoreService.sendImage(message, bitmap, currentChat);
    }

    public void sendMessage(Message message, Chat currentChat) {
        firestoreService.sendMessage(message, currentChat);
    }

    public void readMessage(String currentUserEmail, Chat currentChat) {
        firestoreService.readMessage(currentUserEmail, currentChat);
    }

    public void changeOnlineStatus(String currentUserId, boolean isUserOnline) {
        firestoreService.changeOnlineStatus(currentUserId, isUserOnline);
    }

    public LiveData<Chat> createChat(Chat chat, String currentUserEmail, String startChatText) {
        return firestoreService.createChat(chat, currentUserEmail, startChatText);
    }

    public void inviteParticipants(Chat chat, User currentUser, List<User> newChatUsers) {
        firestoreService.inviteParticipants(chat, currentUser, newChatUsers);
    }

    public void leaveFromChat(Chat chat, User currentUser) {
        firestoreService.leaveFromChat(chat, currentUser);
    }

    public Task<QuerySnapshot> getUserFromDB(String email) {
        return firestoreService.getUserFromDB(email);
    }


}
