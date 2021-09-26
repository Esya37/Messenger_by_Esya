package com.example.messengerbyesya.model;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.net.Uri;

import com.example.messengerbyesya.services.AuthentificationService;
import com.example.messengerbyesya.services.FirestoreService;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.QuerySnapshot;

public class MessengerRepository {

    private final AuthentificationService authentificationService;
    private final FirestoreService firestoreService;

    public MessengerRepository() {
        authentificationService = new AuthentificationService();
        firestoreService = new FirestoreService();
    }

    public FirebaseUser getCurrentFirebaseUser(){
        return authentificationService.getCurrentFirebaseUser();
    }

    public String createUser(String email, String password) {
        return authentificationService.createUser(email, password);
    }

    public String signInWithEmailAndPassword(String email, String password) {
        return authentificationService.signInWithEmailAndPassword(email, password);
    }

    public void signOut(){
        authentificationService.signOut();
    }

    public String changePassword(String email, String oldPassword, String newPassword) {
        return authentificationService.changePassword(email, oldPassword, newPassword);
    }

    public Task<Uri> getAvatar(User user) {
        return firestoreService.getAvatar(user);
    }

    public User uploadAvatar(Bitmap bitmap, User user, String currentUserId, ProgressDialog pd) {
        return firestoreService.uploadAvatar(bitmap, user, currentUserId, pd);
    }

    public void uploadTempAvatar(Bitmap bitmap, User user ) {
        firestoreService.uploadTempAvatar(bitmap, user);
    }

    public void setUser(User user, String userId) {
        firestoreService.setUser(user, userId);
    }

    public Task<Void> deleteImage(String imagePath) {
        return firestoreService.deleteImage(imagePath);
    }

    public void sendMessage(Message message) {
        firestoreService.sendMessage(message);
    }
    public Task<QuerySnapshot> getCurrentUserFromDB(String email) {
        return firestoreService.getCurrentUserFromDB(email);
    }


}
