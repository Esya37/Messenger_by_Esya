package com.example.messengerbyesya.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.messengerbyesya.R;
import com.example.messengerbyesya.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegistrationFragment extends BaseFragment {

    public RegistrationFragment() {
        // Required empty public constructor
    }

    public static RegistrationFragment newInstance() {
        return new RegistrationFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private View inflatedView;
    private Button submitButton;
    private EditText email;
    private EditText password;
    private EditText repeatedPassword;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    private String uid;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        inflatedView = inflater.inflate(R.layout.fragment_registration, container, false);
        // Inflate the layout for this fragment
        return inflatedView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        email = inflatedView.findViewById(R.id.editTextTextEmailAddressRegistration);
        password = inflatedView.findViewById(R.id.editTextTextPasswordRegistration);
        repeatedPassword = inflatedView.findViewById(R.id.editTextTextPassword2Registration);

        submitButton = inflatedView.findViewById(R.id.registrationSubmitButton);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!hasConnection(getContext())){
                    Toast.makeText(getContext(), "Check your Internet connection", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!(isEmailValid(email.getText().toString()))) {
                    Toast.makeText(getContext(), "E-mail is not valid", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (password.getText().toString().length() < 6) {
                    Toast.makeText(getContext(), "Password length must be 6 characters or more", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!(password.getText().toString().equals(repeatedPassword.getText().toString()))) {
                    Toast.makeText(getContext(), "Password and repeated password is not equals", Toast.LENGTH_SHORT).show();
                    return;
                }

                firebaseAuth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getContext(), "Added new user", Toast.LENGTH_SHORT).show();
                                    uid = firebaseAuth.getUid();

                                    User user = new User(firebaseAuth.getCurrentUser().getEmail(), password.getText().toString(), "Пользователь № " + uid, "none");
                                    FirebaseFirestore.getInstance().collection("user").add(user);

                                    Navigation.findNavController(submitButton).navigate(R.id.action_authentificationFragment_to_chatFragment);
                                }
                            }
                        });
            }
        });

    }

}