package com.example.messengerbyesya.services;

import androidx.annotation.NonNull;
import androidx.core.util.PatternsCompat;

import com.example.messengerbyesya.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validation {

    //private final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private List<User> users = new ArrayList<>();

//    public List<User> getUsers(){
//        firebaseFirestore.collection("user").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                for(int i=0; i<task.getResult().getDocuments().size(); i++){
//                    users.add(task.getResult().getDocuments().get(i).toObject(User.class));
//                }
//
//            }
//        });
//        return users;
//    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public boolean fullValidation(String email, String password, String repeatedPassword) {

        Pattern lowercaseLetter;    //1
        Pattern uppercaseLetter;
        Pattern digit;
        Pattern specialSymbol;

        Matcher hasLowercaseLetter;
        Matcher hasUppercaseLetter;
        Matcher hasDigit;
        Matcher hasSpecialSymbol;

        int secureLevel;

        if (email == null) {
            return false;
        }
        if (password == null) {
            return false;
        }
        if(repeatedPassword == null){
            return false;
        }

        if (!(PatternsCompat.EMAIL_ADDRESS.matcher(email).matches())) {   //2
            return false;   //20
        }

        boolean flag = false;
        for (int i = 0; i < users.size(); i++) {  //3
            if ((email.equals(users.get(i).getEmail()))) {  //4
                flag = true;    //5
            }
        }   //6
        if (!flag) {  //7
            return false; //20
        }

        if (!isPasswordsEquals(password, repeatedPassword)) {   //8
            return false;   //20
        } else {
            if (!isGoodPasswordLength(password)) {   //9
                return false;   //20
            } else {

                lowercaseLetter = Pattern.compile("[a-z]");  //10
                uppercaseLetter = Pattern.compile("[A-Z]");
                digit = Pattern.compile("[0-9]");
                specialSymbol = Pattern.compile("[!@#$%&*()_+=|<>?{}\\[\\]~-]");

                secureLevel = 0;

                hasLowercaseLetter = lowercaseLetter.matcher(password);
                hasUppercaseLetter = uppercaseLetter.matcher(password);
                hasDigit = digit.matcher(password);
                hasSpecialSymbol = specialSymbol.matcher(password);

                if (hasLowercaseLetter.find()) {  //11
                    secureLevel++;  //12
                }
                if (hasUppercaseLetter.find()) {  //13
                    secureLevel++;   //14
                }
                if (hasDigit.find()) {   //15
                    secureLevel++;   //16
                }
                if (hasSpecialSymbol.find()) {  //17
                    secureLevel++;  //18
                }

                if (secureLevel > 1) {   //19
                    return true;  //20
                } else {
                    return false;   //20
                }
            }
        }
    }

    public boolean isGoodPasswordLength(String password) {
        return password.length() >= 6;
    }

    public boolean isPasswordsEquals(String password, String repeatedPassword){
        return password.equals(repeatedPassword);
    }

    public boolean isNameValid(String name){
        if(name == null){ //1
            return false;  //9
        }
        if (name.split(" ").length != 2) { //2  //Имя должно состоять из двух слов
            return false;  //9
        }                                    //3                                                   //4
        if (Character.isLowerCase(name.split(" ")[0].charAt(0)) || (Character.isLowerCase(name.split(" ")[1].charAt(0)))) { //Имя и фамилия должны начинаться с заглавной буквы
            return false;  //9
        }
        for (int i = 0; i < name.length(); i++) {  //5            //6                                                              //7
            if (!((Character.UnicodeBlock.of(name.charAt(i)).equals(Character.UnicodeBlock.CYRILLIC)) || Character.isSpaceChar(name.toString().charAt(i)))) { //Имя и фамилия должны содержать только буквы русского алфавита
                return false;  //9
            }
        } //8
        return true;  //9
    }
}
