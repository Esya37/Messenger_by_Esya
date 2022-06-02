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
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.List;

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

    public Task<Uri> getChatAvatar(String chatAvatarPath) {
        return firebaseStorageRef.child(chatAvatarPath).getDownloadUrl();
    }

    public void uploadChatAvatar(Bitmap bitmap, Chat chat, ProgressDialog pd) {

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        data = baos.toByteArray();
        baos.reset();

        chat.setChatAvatar("chats_avatars/" + chat.getChatId());

        firebaseStorageRef.child(chat.getChatAvatar()).putBytes(data).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Toast.makeText(pd.getContext(), "Что-то пошло не так, попробуйте еще раз", Toast.LENGTH_LONG).show();
            } else {
                firebaseFirestore.collection("chats").document(chat.getChatId()).set(chat);
            }
            pd.dismiss();
        });

    }

    public Task<Uri> getImageDownloadUrl(String imagePath) {
        return firebaseStorageRef.child(imagePath).getDownloadUrl();
    }

    public Task<ListResult> getImagesFromFolder(String folderPath) {
        return firebaseStorageRef.child(folderPath).listAll();
    }

    public void setUser(User user, String userId) {
        firebaseFirestore.collection("user").document(userId).set(user);
    }

    public void setChat(Chat chat) {
        firebaseFirestore.collection("chats").document(chat.getChatId()).set(chat);
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
        firebaseFirestore.collection("chats").document(currentChat.getChatId()).get().addOnSuccessListener(documentSnapshot -> {
            Chat currentChat1 = documentSnapshot.toObject(Chat.class);
            currentChat1.setChatId(documentSnapshot.getId());
            WriteBatch writeBatch = firebaseFirestore.batch();
            writeBatch.set(firebaseFirestore.collection("chats").document(currentChat1.getChatId()).collection("messages").document(), message);
            writeBatch.update(firebaseFirestore.collection("chats").document(currentChat1.getChatId()), "date_of_last_message", message.getDate());
            for (int i = 0; i < currentChat1.getUsersEmails().size(); i++) {
                if (!currentChat1.getUsersEmails().get(i).equals(message.getSenderEmail())) {
                    currentChat1.getCountOfUncheckedMessages().put(currentChat1.getUsersEmails().get(i), currentChat1.getCountOfUncheckedMessages().get(currentChat1.getUsersEmails().get(i)) + 1);
                }
            }
            writeBatch.update(firebaseFirestore.collection("chats").document(currentChat1.getChatId()), "count_of_unchecked_messages", currentChat1.getCountOfUncheckedMessages());
            writeBatch.commit();
        });

    }

    public void readMessage(String currentUserEmail, Chat currentChat) {
        firebaseFirestore.collection("chats").document(currentChat.getChatId()).get().addOnSuccessListener(documentSnapshot -> {
            Chat currentChat1 = documentSnapshot.toObject(Chat.class);
            currentChat1.setChatId(documentSnapshot.getId());
            for (int i = 0; i < currentChat1.getUsersEmails().size(); i++) {
                if (currentChat1.getUsersEmails().get(i).equals(currentUserEmail)) {
                    currentChat1.getCountOfUncheckedMessages().put(currentChat1.getUsersEmails().get(i), 0L);
                }
            }
            firebaseFirestore.collection("chats").document(currentChat1.getChatId()).update("count_of_unchecked_messages", currentChat1.getCountOfUncheckedMessages());

        });
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
                        firebaseFirestore.collection("chats").document(documentSnapshot.getId()).update("chat_name", "Чат " + documentSnapshot.getId()).addOnSuccessListener(unused -> {
                            createdChat.setValue(documentSnapshot.toObject(Chat.class));
                            createdChat.getValue().setChatId(documentSnapshot.getId());
                            createdChat.getValue().setChatName("Чат " + documentSnapshot.getId());
                        });
                    });
                }
            });

        });
        return createdChat;
    }

    public void inviteParticipants(Chat chat, User currentUser, List<User> newChatUsers) {
        WriteBatch writeBatch = firebaseFirestore.batch();
        writeBatch.update(firebaseFirestore.collection("chats").document(chat.getChatId()), "users_emails", chat.getUsersEmails());
        writeBatch.update(firebaseFirestore.collection("chats").document(chat.getChatId()), "count_of_unchecked_messages", chat.getCountOfUncheckedMessages());

        for (int i = 0; i < newChatUsers.size(); i++) {
            writeBatch.set(firebaseFirestore.collection("chats").document(chat.getChatId()).collection("messages").document(), new Message(
                    "Пользователь " + currentUser.getName() + " пригласил в чат пользователя " + newChatUsers.get(i).getName(),
                    new Date(),
                    currentUser.getEmail()
            ));
        }
        writeBatch.commit();
    }

    public void leaveFromChat(Chat chat, User currentUser) {
        if (chat.getUsersEmails().size() == 2) {
            chat.setChatName(currentUser.getName());
            chat.setChatAvatar("avatars/" + currentUser.getAvatar());
        }
        chat.getUsersEmails().remove(currentUser.getEmail());
        chat.getCountOfUncheckedMessages().remove(currentUser.getEmail());

        for (int i = 0; i < chat.getUsersEmails().size(); i++) {
            chat.getCountOfUncheckedMessages().put(chat.getUsersEmails().get(i), chat.getCountOfUncheckedMessages().get(chat.getUsersEmails().get(i)) + 1);
        }

        WriteBatch writeBatch = firebaseFirestore.batch();
        writeBatch.set(firebaseFirestore.collection("chats").document(chat.getChatId()), chat);
        writeBatch.set(firebaseFirestore.collection("chats").document(chat.getChatId()).collection("messages").document(), new Message(
                "Пользователь " + currentUser.getName() + " покинул чат",
                new Date(),
                currentUser.getEmail()
        ));
        writeBatch.commit();
    }

    public Task<QuerySnapshot> getUserFromDB(String email) {
        return firebaseFirestore.collection("user").whereEqualTo("email", email).get();
    }

}
