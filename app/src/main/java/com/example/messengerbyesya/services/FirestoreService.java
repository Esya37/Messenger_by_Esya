package com.example.messengerbyesya.services;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.net.Uri;
import android.widget.Toast;

import com.example.messengerbyesya.model.Message;
import com.example.messengerbyesya.model.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;

public class FirestoreService {

    private final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private final FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    private final StorageReference firebaseStorageRef = firebaseStorage.getReferenceFromUrl("gs://messenger-by-esya.appspot.com/");
    private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private byte[] data;

    public Task<Uri> getAvatar(User user) {
        return firebaseStorageRef.child(user.getAvatar()).getDownloadUrl();
    }

    public User uploadAvatar(Bitmap bitmap, User user, String currentUserId, ProgressDialog pd) {

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        data = baos.toByteArray();
        baos.reset();
        firebaseStorageRef.child("Avatar_" + user.getEmail()).putBytes(data).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                pd.dismiss();
            } else {
                Toast.makeText(pd.getContext(), "Что-то пошло не так, попробуйте еще раз", Toast.LENGTH_LONG).show();
            }
        });
        user.setAvatar("Avatar_" + user.getEmail());
        firebaseFirestore.collection("user").document(currentUserId).set(user);
        firebaseStorageRef.child("none_" + user.getEmail()).delete();
        return user;
    }

    public void uploadTempAvatar(Bitmap bitmap, User user) {
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        data = baos.toByteArray();
        baos.reset();
        firebaseStorageRef.child("none_" + user.getEmail()).putBytes(data);
    }

    public void setUser(User user, String userId) {
        firebaseFirestore.collection("user").document(userId).set(user);
    }

    public Task<Void> deleteImage(String imagePath) {
        return firebaseStorageRef.child(imagePath).delete();
    }

    public void sendMessage(Message message) {
        firebaseFirestore.collection("message").add(message);
    }

    public Task<QuerySnapshot> getCurrentUserFromDB(String email) {
        return firebaseFirestore.collection("user").whereEqualTo("email", email).get();
    }

}
