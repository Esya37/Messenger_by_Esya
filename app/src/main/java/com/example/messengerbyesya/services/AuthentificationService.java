package com.example.messengerbyesya.services;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.messengerbyesya.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class AuthentificationService {

    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private MutableLiveData<String> result = new MutableLiveData<>();
    private Task authTask;

    public FirebaseUser getCurrentFirebaseUser() {
        return firebaseAuth.getCurrentUser();
    }

    public LiveData<String> createUser(String email, String password) {

        authTask = firebaseAuth.createUserWithEmailAndPassword(email, password);
        authTask.addOnCompleteListener((OnCompleteListener<AuthResult>) task -> {
            if (task.isSuccessful()) {
                result.setValue("Вы успешно зарегистрировались");
                User user = new User(firebaseAuth.getCurrentUser().getEmail(), password, "Пользователь № " + firebaseAuth.getUid(), "none");
                FirebaseFirestore.getInstance().collection("user").add(user);
            } else {
                result.setValue(task.getException().getMessage());
            }
        });
        return result;
    }

    public LiveData<String> signInWithEmailAndPassword(String email, String password) {
        authTask = firebaseAuth.signInWithEmailAndPassword(email, password);
        result.setValue("");
        authTask.addOnCompleteListener((OnCompleteListener<AuthResult>) task -> {
            if (task.isSuccessful()) {
                result.setValue("Вы успешно вошли в аккаунт");
            } else {
                try {
                    throw task.getException();
                } catch (FirebaseAuthInvalidCredentialsException ex) {
                    result.setValue("Неправильный пароль");
                } catch (FirebaseAuthInvalidUserException ex) {
                    result.setValue("Неправильный e-mail");
                } catch (Exception ex) {
                    result.setValue(task.getException().toString());
                }
            }
        });
        return result;
    }

    public void signOut() {
        firebaseAuth.signOut();
    }

    public LiveData<String> changePassword(String email, String oldPassword, String newPassword) {
        result.setValue("");
        authTask = firebaseAuth.getCurrentUser().reauthenticate(EmailAuthProvider.getCredential(email, oldPassword));
        authTask.addOnCompleteListener((OnCompleteListener<AuthResult>) task -> {
            if (task.isSuccessful()) {
                firebaseAuth.getCurrentUser().updatePassword(newPassword).addOnCompleteListener(task1 -> {
                    result.setValue("Пароль успешно изменен");    //TODO: Убрать пароль в виде строки из БД
                });
            } else {
                try {
                    throw task.getException();
                } catch (FirebaseAuthInvalidCredentialsException ex) {
                    result.setValue("Неверный пароль");
                } catch (Exception ex) {
                    result.setValue(task.getException().toString());
                }
            }
        });
        return result;
    }

}

