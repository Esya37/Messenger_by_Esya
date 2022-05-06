package com.example.messengerbyesya.services;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.net.Uri;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.messengerbyesya.model.Chat;
import com.example.messengerbyesya.model.Message;
import com.example.messengerbyesya.model.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.Date;

public class FirestoreService {

    private final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private final FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    private final StorageReference firebaseStorageRef = firebaseStorage.getReferenceFromUrl("gs://messenger-by-esya.appspot.com/");
    private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private byte[] data;
    private MutableLiveData<Chat> createdChat = new MutableLiveData<>();

    public Task<Uri> getAvatar(User user) {
        return firebaseStorageRef.child("avatars/" + user.getAvatar()).getDownloadUrl();
    }

    public User uploadAvatar(Bitmap bitmap, User user, String currentUserId, ProgressDialog pd) {

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        data = baos.toByteArray();
        baos.reset();
        firebaseStorageRef.child("avatars/Avatar_" + user.getEmail()).putBytes(data).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Toast.makeText(pd.getContext(), "Что-то пошло не так, попробуйте еще раз", Toast.LENGTH_LONG).show();
            }
            pd.dismiss();
        });
        user.setAvatar("Avatar_" + user.getEmail());
        firebaseFirestore.collection("user").document(currentUserId).set(user);
        firebaseStorageRef.child("avatars/none_" + user.getEmail()).delete();
        return user;
    }

    public void uploadTempAvatar(Bitmap bitmap, User user) {
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        data = baos.toByteArray();
        baos.reset();
        firebaseStorageRef.child("avatars/none_" + user.getEmail()).putBytes(data);
    }

    public void setUser(User user, String userId) {
        firebaseFirestore.collection("user").document(userId).set(user);
    }

    public Task<Void> deleteImage(String imagePath) {
        return firebaseStorageRef.child("avatars/" + imagePath).delete();
    }

    public void sendImage(Message message, Bitmap bitmap, Chat currentChat) {
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        data = baos.toByteArray();
        baos.reset();
        firebaseStorageRef.child("chats_media_resources/" + currentChat.getChatId() + "/" + message.getText()).putBytes(data).addOnCompleteListener(task -> {
            WriteBatch writeBatch = firebaseFirestore.batch();
            writeBatch.set(firebaseFirestore.collection("chats").document(currentChat.getChatId()).collection("messages").document(), message);
            writeBatch.update(firebaseFirestore.collection("chats").document(currentChat.getChatId()), "date_of_last_message", message.getDate());
            for (int i = 0; i < currentChat.getUsersEmails().size(); i++) {
                if (!currentChat.getUsersEmails().get(i).equals(message.getSenderEmail())) {
                    currentChat.getCountOfUncheckedMessages().put(currentChat.getUsersEmails().get(i), currentChat.getCountOfUncheckedMessages().get(currentChat.getUsersEmails().get(i)) + 1);
                }
            }
            writeBatch.update(firebaseFirestore.collection("chats").document(currentChat.getChatId()), "count_of_unchecked_messages", currentChat.getCountOfUncheckedMessages());
            writeBatch.commit();
        });
    }

    public void sendMessage(Message message, Chat currentChat) {
        WriteBatch writeBatch = firebaseFirestore.batch();
        writeBatch.set(firebaseFirestore.collection("chats").document(currentChat.getChatId()).collection("messages").document(), message);
        writeBatch.update(firebaseFirestore.collection("chats").document(currentChat.getChatId()), "date_of_last_message", message.getDate());
        for (int i = 0; i < currentChat.getUsersEmails().size(); i++) {
            if (!currentChat.getUsersEmails().get(i).equals(message.getSenderEmail())) {
                currentChat.getCountOfUncheckedMessages().put(currentChat.getUsersEmails().get(i), currentChat.getCountOfUncheckedMessages().get(currentChat.getUsersEmails().get(i)) + 1);
            }
        }
        writeBatch.update(firebaseFirestore.collection("chats").document(currentChat.getChatId()), "count_of_unchecked_messages", currentChat.getCountOfUncheckedMessages());
        writeBatch.commit();
    }

    public void readMessage(String currentUserEmail, Chat currentChat) {
        for (int i = 0; i < currentChat.getUsersEmails().size(); i++) {
            if (currentChat.getUsersEmails().get(i).equals(currentUserEmail)) {
                currentChat.getCountOfUncheckedMessages().put(currentChat.getUsersEmails().get(i), 0L);
            }
        }
        firebaseFirestore.collection("chats").document(currentChat.getChatId()).update("count_of_unchecked_messages", currentChat.getCountOfUncheckedMessages());
    }

    public void changeOnlineStatus(String currentUserId, boolean isUserOnline) {
        firebaseFirestore.collection("user").document(currentUserId).update("is_user_online", isUserOnline);
    }

    public LiveData<Chat> createChat(Chat chat, String currentUserEmail, String startChatText) {
        firebaseFirestore.collection("chats").add(chat).addOnSuccessListener(documentReference -> {
            firebaseFirestore.collection("chats").document(documentReference.getId()).collection("messages").add(new Message(startChatText, new Date(), currentUserEmail)).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference documentReference1) {
                    documentReference.get().addOnSuccessListener(documentSnapshot -> {
                        createdChat.setValue(documentSnapshot.toObject(Chat.class));
                        createdChat.getValue().setChatId(documentSnapshot.getId());
                    });
                }
            });

        });
        return createdChat;
    }

    public Task<QuerySnapshot> getCurrentUserFromDB(String email) {
        return firebaseFirestore.collection("user").whereEqualTo("email", email).get();
    }

}
