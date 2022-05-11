package com.example.messengerbyesya;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.messengerbyesya.fragments.CreateChatFragment;
import com.example.messengerbyesya.fragments.ShowParticipantsOrAttachmentsFragment;
import com.example.messengerbyesya.model.Chat;
import com.example.messengerbyesya.model.Message;
import com.example.messengerbyesya.model.MessengerRepository;
import com.example.messengerbyesya.model.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.ListResult;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
    private CreateChatFragment.ActionType currentActionType;
    private ShowParticipantsOrAttachmentsFragment.ShowedItemType showedItemType;

    public MainActivityViewModel() {
        messengerRepository = new MessengerRepository();
    }

    public CreateChatFragment.ActionType getCurrentActionType() {
        return currentActionType;
    }

    public void setCurrentActionType(CreateChatFragment.ActionType currentActionType) {
        this.currentActionType = currentActionType;
    }

    public ShowParticipantsOrAttachmentsFragment.ShowedItemType getShowedItemType() {
        return showedItemType;
    }

    public void setShowedItemType(ShowParticipantsOrAttachmentsFragment.ShowedItemType showedItemType) {
        this.showedItemType = showedItemType;
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
        messengerRepository.changeOnlineStatus(currentUserId, false);
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

    public Task<Uri> getChatAvatar(String chatAvatarPath) {
        return messengerRepository.getChatAvatar(chatAvatarPath);
    }

    public void uploadChatAvatar(Bitmap bitmap, ProgressDialog pd) {
        messengerRepository.uploadChatAvatar(bitmap, currentChat, pd);
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
        newChat.setChatAvatar("");

        return messengerRepository.createChat(newChat, currentUser.getEmail(), context.getString(R.string.user_create_a_chat, currentUser.getName()));
    }

    public void inviteParticipants(List<User> newChatUsers) {
        for (int i = 0; i < newChatUsers.size(); i++) {
            currentChat.getUsersEmails().add(newChatUsers.get(i).getEmail());
            currentChat.getCountOfUncheckedMessages().put(newChatUsers.get(i).getEmail(), 0L);
        }
        for(int i=0; i<currentChat.getUsersEmails().size(); i++){
            currentChat.getCountOfUncheckedMessages().put(currentChat.getUsersEmails().get(i), currentChat.getCountOfUncheckedMessages().get(currentChat.getUsersEmails().get(i)) + newChatUsers.size());
        }
        messengerRepository.inviteParticipants(currentChat, currentUser, newChatUsers);
    }

    public Task<QuerySnapshot> getUserFromDB(String email) {
        return messengerRepository.getUserFromDB(email);
    }

    public String getFinalImageUri(String notFinalImageUri) {
        final String[] finalURL = {""};
        executorService = Executors.newSingleThreadExecutor();
        try {
            executorService.submit(() -> {
                try {
                    finalURL[0] = getFinalURL(notFinalImageUri);
                } catch (IOException e) {
                    finalURL[0] = ":(";
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

    public Task<Uri> getImageDownloadUrl(String imagePath) {
        return messengerRepository.getImageDownloadUrl(imagePath);
    }

    public Task<ListResult> getImagesFromFolder(String folderPath) {
        return messengerRepository.getImagesFromFolder(folderPath);
    }

    public void setUser(User user, String userId) {
        messengerRepository.setUser(user, userId);
    }

    public void setChat(Chat chat) {
        messengerRepository.setChat(chat);
    }

    public void leaveFromChat() {
        messengerRepository.leaveFromChat(currentChat, currentUser);
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
