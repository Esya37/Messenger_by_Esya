package com.example.messengerbyesya.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;

import com.example.messengerbyesya.MainActivityViewModel;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.Random;

public class BaseFragment extends Fragment {

    private Random random = new Random();

    public void checkAvatar(MainActivityViewModel model, ImageView avatarImageView){
        if (model.getCurrentUser().getAvatar().equals("none")) {
            if (model.getCurrentUser().getName().split(" ").length > 2) {
                model.getCurrentUser().setAvatar("none_" + model.getCurrentUser().getEmail());
                model.setUser(model.getCurrentUser(), model.getCurrentUserId());
                //firebaseFirestore.collection("user").document(model.getCurrentUserId()).set(model.getCurrentUser());

                Picasso.with(getContext()).load("https://eu.ui-avatars.com/api/?name=" + model.getCurrentUser().getName().split(" ")[2] + "&size=512?&background=" + generateRandomColorHex()).into(avatarImageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        avatarImageView.setDrawingCacheEnabled(true);
                        avatarImageView.buildDrawingCache();
                        model.uploadTempAvatar(((BitmapDrawable) avatarImageView.getDrawable()).getBitmap(), model.getCurrentUser());
                    }

                    @Override
                    public void onError() {

                    }
                });

            } else {
                model.getCurrentUser().setAvatar("none_" + model.getCurrentUser().getEmail());
                model.setUser(model.getCurrentUser(), model.getCurrentUserId());
                // firebaseFirestore.collection("user").document(model.getCurrentUserId()).set(model.getCurrentUser());
                Picasso.with(getContext()).load("https://eu.ui-avatars.com/api/?name=" + model.getCurrentUser().getName().split(" ")[0] + "%20" + model.getCurrentUser().getName().split(" ")[1] + "&size=512?&background=" + generateRandomColorHex()).into(avatarImageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        avatarImageView.setDrawingCacheEnabled(true);
                        avatarImageView.buildDrawingCache();
                        model.uploadTempAvatar(((BitmapDrawable) avatarImageView.getDrawable()).getBitmap(), model.getCurrentUser());
                    }

                    @Override
                    public void onError() {

                    }
                });
            }
        } else {
            model.getAvatar(model.getCurrentUser()).addOnSuccessListener(uri -> Picasso.with(getContext()).load(uri).into(avatarImageView));
        }
    }

    public void generateChatAvatar(MainActivityViewModel model, ImageView chatImageImageView){
        String apiRequestString;
        if(model.getCurrentChat().getChatName().isEmpty()){
            apiRequestString = "https://eu.ui-avatars.com/api/?name=" + model.getCurrentChat().getChatId() + "&size=512?&background=" + generateRandomColorHex();
        } else {
            apiRequestString = "https://eu.ui-avatars.com/api/?name=" + model.getCurrentChat().getChatName() + "&size=512?&background=" + generateRandomColorHex();
        }
        Picasso.with(getContext()).load(apiRequestString).into(chatImageImageView, new Callback() {
            @Override
            public void onSuccess() {
                chatImageImageView.setDrawingCacheEnabled(true);
                chatImageImageView.buildDrawingCache();

                ProgressDialog pd = new ProgressDialog(getParentFragment().getContext());
                pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                pd.setCancelable(false);
                pd.setMessage("Loading...");
                pd.show();

                model.uploadChatAvatar(((BitmapDrawable) chatImageImageView.getDrawable()).getBitmap(), pd);
            }

            @Override
            public void onError() {

            }
        });
    }

    public static boolean hasConnection(final Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiInfo != null && wifiInfo.isConnected()) {
            return true;
        }
        wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (wifiInfo != null && wifiInfo.isConnected()) {
            return true;
        }
        wifiInfo = cm.getActiveNetworkInfo();
        return wifiInfo != null && wifiInfo.isConnected();
    }

    public boolean isEmailValid(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public String generateRandomColorHex() {
        String color = Integer.toString(random.nextInt(0X1000000), 16);
        return "000000".substring(color.length()) + color;
    }
}
