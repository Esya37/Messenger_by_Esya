package com.example.messengerbyesya;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.messengerbyesya.model.Chat;
import com.example.messengerbyesya.model.Message;
import com.example.messengerbyesya.model.MessengerRepository;
import com.example.messengerbyesya.model.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivityViewModel extends ViewModel {

    private User currentUser;
    private String currentUserId;
    private final MessengerRepository messengerRepository;
    private ExecutorService executorService;
    private Chat currentChat;

    public MainActivityViewModel() {
        messengerRepository = new MessengerRepository();
    }

    public Chat getCurrentChat() {
        return currentChat;
    }

    public void setCurrentChat(Chat currentChat) {
        this.currentChat = currentChat;
    }

    public String getCurrentUserId() {
        return currentUserId;
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

    public FirebaseUser getCurrentFirebaseUser() {
        return messengerRepository.getCurrentFirebaseUser();
    }

    public LiveData<String> createUser(String email, String password) {
        return messengerRepository.createUser(email, password);
    }

    public LiveData<String> signInWithEmailAndPassword(String email, String password) {
        return messengerRepository.signInWithEmailAndPassword(email, password);
    }

    public void signOut() {
        messengerRepository.signOut();
    }

    public LiveData<String> changePassword(String email, String oldPassword, String newPassword) {
        return messengerRepository.changePassword(email, oldPassword, newPassword);
    }

    public Task<Uri> getAvatar(User user) {
        return messengerRepository.getAvatar(user);
    }

    public void uploadAvatar(Bitmap bitmap, User user, String currentUserId, ProgressDialog pd) {
        currentUser = messengerRepository.uploadAvatar(bitmap, user, currentUserId, pd);
    }

    public void uploadTempAvatar(Bitmap bitmap, User user) {
        messengerRepository.uploadTempAvatar(bitmap, user);
    }

    public void sendImage(Message message, Bitmap bitmap) {
        messengerRepository.sendImage(message, bitmap, currentChat);
    }

    public void sendMessage(Message message) {
        messengerRepository.sendMessage(message, currentChat);
    }

    public void readMessage() {
        messengerRepository.readMessage(currentUser.getEmail(), currentChat);
    }

    public void changeOnlineStatus(boolean isUserOnline) {
        messengerRepository.changeOnlineStatus(currentUserId, isUserOnline);
    }

    public LiveData<Chat> createChat(Map<String, Boolean> usersCheckedMap, Context context) {
        Chat newChat = new Chat();
        Map<String, Long> countOfUncheckedMessages = new HashMap<>();
        ArrayList<String> usersEmails = new ArrayList<>();
        for (Map.Entry<String, Boolean> entry : usersCheckedMap.entrySet()) {
            if (entry.getValue()) {
                countOfUncheckedMessages.put(entry.getKey(), 0L);
                usersEmails.add(entry.getKey());
            }
        }

        newChat.setCountOfUncheckedMessages(countOfUncheckedMessages);
        newChat.setUsersEmails(usersEmails);
        newChat.setDateOfLastMessage(new Date());

        return messengerRepository.createChat(newChat, currentUser.getEmail(), context.getString(R.string.user_create_a_chat, currentUser.getName()));
    }

    public Task<QuerySnapshot> getCurrentUserFromDB(String email) {
        return messengerRepository.getCurrentUserFromDB(email);
    }

    public String getFinalImageUri(String notFinalImageUri) {
        final String[] finalURL = {""};
        executorService = Executors.newSingleThreadExecutor();
        try {
            executorService.submit(() -> {
                try {
                    finalURL[0] = getFinalURL(notFinalImageUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        executorService.shutdown();
        return finalURL[0];
    }

    public void setUser(User user, String userId) {
        messengerRepository.setUser(user, userId);
    }

    public Task<Void> deleteImage(String imagePath) {
        return messengerRepository.deleteImage(imagePath);
    }

    private static String getFinalURL(String url) throws IOException {
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        con.setInstanceFollowRedirects(false);
        con.connect();
        con.getInputStream();

        if (con.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM || con.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP) {
            String redirectUrl = con.getHeaderField("Location");
            return getFinalURL(redirectUrl);
        }
        if (url.contains("https://yandex.ru/images/")) {
            url = getUrlFromYandexImages(url);
        }
        if (url.contains("https://www.google.ru/imgres")) {
            url = getUrlFromGoogleImages(url);
        }
        return url;
    }

    private static String getUrlFromGoogleImages(String url) {
        url = url.substring(url.indexOf("?") + 1);
        for (String string : url.split("&")) {
            if (string.contains("imgurl")) {
                url = string.substring(7);
                break;
            }
        }
        return url;
    }

    private static String getUrlFromYandexImages(String url) {
        for (String string : url.split("&")) {
            if (string.contains("img_url")) {
                url = string.substring(8);
                break;
            }
        }
        url = url.replace("%3A", ":");
        url = url.replace("%2F", "/");
        url = url.replace("%3F", "?");
        url = url.replace("%26", "&");
        url = url.replace("%3D", "=");
        return url;
    }


}
