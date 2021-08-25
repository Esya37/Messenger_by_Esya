package com.example.messengerbyesya;

import android.app.Activity;
import android.content.DialogInterface;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;

import com.example.messengerbyesya.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.regex.Pattern;

public class Dialogs {

    public enum DialogType {
        changeName,
        changePassword,
        changeAvatar,
        deleteAvatar;
    }

    private View inflatedView;
    private EditText changeNameEditText;
    private EditText oldPasswordEditText;
    private EditText newPasswordEditText;
    private EditText newPasswordRepeatedEditText;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

    public AlertDialog getDialog(Activity activity, DialogType dialogType, User currentUser, String currentUserId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        AlertDialog alertDialog;

        switch (dialogType) {
            case changeName:
                inflatedView = activity.getLayoutInflater().inflate(R.layout.change_name_dialog, null);
                changeNameEditText = inflatedView.findViewById(R.id.changeNameEditText);
                changeNameEditText.setText(currentUser.getName());
                builder.setView(inflatedView);
                builder.setPositiveButton(R.string.accept, null);
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                alertDialog = builder.create();
                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //TODO Добавить валидацию
                                currentUser.setName(changeNameEditText.getText().toString());
                                firebaseFirestore.collection("user").document(currentUserId).set(currentUser);
                                dialog.dismiss();
                            }
                        });
                    }
                });
                return alertDialog;
            case changePassword:
                inflatedView = activity.getLayoutInflater().inflate(R.layout.change_password_dialog, null);

                oldPasswordEditText = inflatedView.findViewById(R.id.oldPasswordEditText);
                newPasswordEditText = inflatedView.findViewById(R.id.newPasswordEditText);
                newPasswordRepeatedEditText = inflatedView.findViewById(R.id.newPasswordRepeatedEditText);

                builder.setView(inflatedView);
                builder.setPositiveButton(R.string.accept, null);
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                alertDialog = builder.create();
                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //TODO Проверить наличие подключение к Интернету
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

                                firebaseAuth.getCurrentUser().reauthenticate(EmailAuthProvider.getCredential(currentUser.getEmail(), oldPasswordEditText.getText().toString())).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            oldPasswordEditText.setError(null);
                                            firebaseAuth.getCurrentUser().updatePassword(newPasswordEditText.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    //TODO Добавить значок загрузки
                                                    currentUser.setPassword(newPasswordEditText.getText().toString());
                                                    firebaseFirestore.collection("user").document(currentUserId).set(currentUser);
                                                    dialog.dismiss();
                                                }
                                            });
                                        } else {
                                            try {
                                                throw task.getException();
                                            } catch (FirebaseAuthInvalidCredentialsException ex) {
                                                oldPasswordEditText.setError("Incorrect password");
                                            } catch (Exception ex) {
                                                oldPasswordEditText.setError(task.getException().toString());
                                            }
                                        }
                                    }
                                });
                            }
                        });
                    }
                });
                return alertDialog;
            case changeAvatar:

                return builder.create();
            case deleteAvatar:

                return builder.create();
            default:
                return null;
        }
    }

}
