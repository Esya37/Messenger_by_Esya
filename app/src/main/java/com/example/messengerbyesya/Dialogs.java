package com.example.messengerbyesya;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.example.messengerbyesya.model.Chat;
import com.example.messengerbyesya.model.User;

public class Dialogs {

    public enum DialogType {
        changeName,
        changePassword,
        changeAvatar,
        deleteAvatar,
        changeChatName
    }

    private View inflatedView;
    private EditText changeNameEditText;
    private EditText oldPasswordEditText;
    private EditText newPasswordEditText;
    private EditText newPasswordRepeatedEditText;
    private String[] name;
    private ProgressDialog pd;
    private MainActivityViewModel model;

    public AlertDialog getDialog(Activity activity, DialogType dialogType, User currentUser, String currentUserId, Chat currentChat) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        AlertDialog alertDialog;

        model = new ViewModelProvider((ViewModelStoreOwner) activity).get(MainActivityViewModel.class);

        switch (dialogType) {
            case changeName:
                inflatedView = activity.getLayoutInflater().inflate(R.layout.change_name_dialog, null);
                changeNameEditText = inflatedView.findViewById(R.id.changeNameEditText);
                changeNameEditText.setText(currentUser.getName());
                name = new String[2];
                builder.setView(inflatedView);
                builder.setPositiveButton(R.string.accept, null);
                builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
                alertDialog = builder.create();
                alertDialog.setOnShowListener(dialog -> alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
                    if (!(hasConnection(v.getContext()))) {
                        Toast.makeText(v.getContext(), "Check your Internet connection", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (changeNameEditText.getText().toString().split(" ").length != 2) {
                        changeNameEditText.setError("Имя должно состоять из двух слов");
                        return;
                    }
                    name = changeNameEditText.getText().toString().split(" ");
                    if (Character.isLowerCase(name[0].charAt(0)) || (Character.isLowerCase(name[1].charAt(0)))) {
                        changeNameEditText.setError("Имя и фамилия должны начинаться с заглавной буквы");
                        return;
                    }
                    for (int i = 0; i < changeNameEditText.getText().toString().length(); i++) {
                        if (!((Character.UnicodeBlock.of(changeNameEditText.getText().toString().charAt(i)).equals(Character.UnicodeBlock.CYRILLIC)) || Character.isSpaceChar(changeNameEditText.getText().toString().charAt(i)))) {
                            changeNameEditText.setError("Имя и фамилия должны содержать только буквы русского алфавита");
                            return;
                        }
                    }
                    changeNameEditText.setError(null);
                    if (currentUser.getAvatar().equals("none") || currentUser.getAvatar().equals("none_" + currentUser.getEmail())) {
                        currentUser.setAvatar("none");
                    }
                    currentUser.setName(changeNameEditText.getText().toString());
                    model.setUser(currentUser, currentUserId);
                    dialog.dismiss();
                }));
                return alertDialog;
            case changePassword:
                inflatedView = activity.getLayoutInflater().inflate(R.layout.change_password_dialog, null);

                oldPasswordEditText = inflatedView.findViewById(R.id.oldPasswordEditText);
                newPasswordEditText = inflatedView.findViewById(R.id.newPasswordEditText);
                newPasswordRepeatedEditText = inflatedView.findViewById(R.id.newPasswordRepeatedEditText);

                builder.setView(inflatedView);
                builder.setPositiveButton(R.string.accept, null);
                builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
                alertDialog = builder.create();
                alertDialog.setOnShowListener(dialog -> alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
                    if (!(hasConnection(v.getContext()))) {
                        Toast.makeText(v.getContext(), "Check your Internet connection", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (newPasswordEditText.getText().toString().length() < 6) {
                        newPasswordEditText.setError("Password length must be 6 characters or more");
                        return;
                    } else {
                        newPasswordEditText.setError(null);
                    }
                    if (!(newPasswordEditText.getText().toString().equals(newPasswordRepeatedEditText.getText().toString()))) {
                        newPasswordEditText.setError("Password and repeated password is not equals");
                        newPasswordRepeatedEditText.setError("Password and repeated password is not equals");
                        return;
                    } else {
                        newPasswordEditText.setError(null);
                        newPasswordRepeatedEditText.setError(null);
                    }

                    pd = new ProgressDialog(v.getContext());
                    pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    pd.setCancelable(false);
                    pd.setMessage("Loading...");
                    pd.show();

                    model.changePassword(model.getCurrentUser().getEmail(), oldPasswordEditText.getText().toString(), newPasswordEditText.getText().toString()).observe((LifecycleOwner) activity, (Observer<String>) s -> {
                        if (!s.equals("")) {
                            pd.dismiss();
                        }
                        switch (s) {
                            case "Пароль успешно изменен":
                                oldPasswordEditText.setError(null);
                                model.changePassword(" ", " ", " ").removeObservers((LifecycleOwner) activity);
                                Toast.makeText(activity, s, Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                                break;
                            case "Неверный пароль":
                                oldPasswordEditText.setError("Неверный пароль");
                                break;
                            case "":
                                break;
                            default:
                                Toast.makeText(activity, s, Toast.LENGTH_SHORT).show();
                                break;
                        }
                    });

                }));
                return alertDialog;
            case changeAvatar:

                return null;
            case deleteAvatar:
                builder.setMessage(R.string.delete_avatar_confirm);
                builder.setNegativeButton(R.string.no, (dialog, which) -> dialog.cancel());
                builder.setPositiveButton(R.string.yes, null);
                alertDialog = builder.create();
                alertDialog.setOnShowListener(dialog -> alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
                    if (!(hasConnection(v.getContext()))) {
                        Toast.makeText(v.getContext(), "Check your Internet connection", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    pd = new ProgressDialog(v.getContext());
                    pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    pd.setCancelable(false);
                    pd.setMessage("Loading...");
                    pd.show();

                    model.deleteImage(currentUser.getAvatar()).addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            Toast.makeText(pd.getContext(), "Что-то пошло не так, попробуйте еще раз", Toast.LENGTH_LONG).show();
                        }
                        pd.dismiss();
                    });
                    currentUser.setAvatar("none");
                    model.setUser(currentUser, currentUserId);
                    dialog.dismiss();
                }));

                return alertDialog;
            case changeChatName:
                inflatedView = activity.getLayoutInflater().inflate(R.layout.change_name_dialog, null);
                changeNameEditText = inflatedView.findViewById(R.id.changeNameEditText);
                changeNameEditText.setText(currentChat.getChatName());
                ((TextView) inflatedView.findViewById(R.id.textView3)).setText(R.string.rename_chat_name_and_press_accept_button);
                builder.setView(inflatedView);
                builder.setPositiveButton(R.string.accept, null);
                builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
                alertDialog = builder.create();
                alertDialog.setOnShowListener(dialog -> alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
                    if (!(hasConnection(v.getContext()))) {
                        Toast.makeText(v.getContext(), "Check your Internet connection", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (changeNameEditText.getText().toString().isEmpty()) {
                        changeNameEditText.setError("Имя не должно быть пустым");
                        return;
                    }
                    changeNameEditText.setError(null);
                    currentChat.setChatName(changeNameEditText.getText().toString());
                    model.setChat(currentChat);
                    dialog.dismiss();
                }));
                return alertDialog;
            default:
                return null;
        }
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

}
