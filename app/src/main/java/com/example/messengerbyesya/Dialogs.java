package com.example.messengerbyesya;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.messengerbyesya.model.User;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class Dialogs {

    public enum DialogType {
        changeName,
        changePassword,
        changeAvatar,
        deleteAvatar
    }

    private View inflatedView;
    private EditText changeNameEditText;
    private EditText oldPasswordEditText;
    private EditText newPasswordEditText;
    private EditText newPasswordRepeatedEditText;
    private String[] name;
    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private final FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    private final StorageReference firebaseStorageRef = firebaseStorage.getReferenceFromUrl("gs://messenger-by-esya.appspot.com/");
    private ProgressDialog pd;

    public AlertDialog getDialog(Activity activity, DialogType dialogType, User currentUser, String currentUserId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        AlertDialog alertDialog;

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
                    if(!(hasConnection(v.getContext()))){
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
                    firebaseFirestore.collection("user").document(currentUserId).set(currentUser);
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
                    if(!(hasConnection(v.getContext()))){
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

                    firebaseAuth.getCurrentUser().reauthenticate(EmailAuthProvider.getCredential(currentUser.getEmail(), oldPasswordEditText.getText().toString())).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            oldPasswordEditText.setError(null);
                            firebaseAuth.getCurrentUser().updatePassword(newPasswordEditText.getText().toString()).addOnCompleteListener(task1 -> {
                                currentUser.setPassword(newPasswordEditText.getText().toString());
                                firebaseFirestore.collection("user").document(currentUserId).set(currentUser);
                                pd.dismiss();
                                dialog.dismiss();
                            });
                        } else {
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthInvalidCredentialsException ex) {
                                oldPasswordEditText.setError("Incorrect password");
                            } catch (Exception ex) {
                                oldPasswordEditText.setError(task.getException().toString());
                            }
                            pd.dismiss();
                        }
                    });
                }));
                return alertDialog;
            case changeAvatar:


                return builder.create();
            case deleteAvatar:
                builder.setMessage(R.string.delete_avatar_confirm);
                builder.setNegativeButton(R.string.no, (dialog, which) -> dialog.cancel());
                builder.setPositiveButton(R.string.yes, null);
                alertDialog = builder.create();
                alertDialog.setOnShowListener(dialog -> alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
                    if(!(hasConnection(v.getContext()))){
                        Toast.makeText(v.getContext(), "Check your Internet connection", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (currentUser.getAvatar().equals("none_" + currentUser.getEmail())) {
                        firebaseStorageRef.child("none_" + currentUser.getEmail()).delete();
                    } else if (currentUser.getAvatar().equals("Avatar_" + currentUser.getEmail())) {
                        firebaseStorageRef.child("Avatar_" + currentUser.getEmail()).delete();
                    }
                    currentUser.setAvatar("none");
                    firebaseFirestore.collection("user").document(currentUserId).set(currentUser);
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
