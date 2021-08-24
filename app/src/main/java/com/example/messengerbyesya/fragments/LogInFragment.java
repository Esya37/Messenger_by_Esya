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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class LogInFragment extends BaseFragment {

    public LogInFragment() {
        // Required empty public constructor
    }

    public static LogInFragment newInstance() {
        return new LogInFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private View inflatedView;
    private Button submitButton;
    private EditText email;
    private EditText password;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        inflatedView = inflater.inflate(R.layout.fragment_log_in, container, false);
        // Inflate the layout for this fragment
        return inflatedView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        email = inflatedView.findViewById(R.id.editTextTextEmailAddressLogIn);
        password = inflatedView.findViewById(R.id.editTextTextPasswordLogIn);

        submitButton = inflatedView.findViewById(R.id.logInSubmitButton);
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

                firebaseAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getContext(), "You have successfully logged in", Toast.LENGTH_SHORT).show();
                                    Navigation.findNavController(submitButton).navigate(R.id.action_authentificationFragment_to_chatFragment);
                                } else {
                                    try {
                                        throw task.getException();
                                    } catch (FirebaseAuthInvalidCredentialsException ex) {
                                        Toast.makeText(getContext(), "Incorrect password", Toast.LENGTH_SHORT).show();
                                    } catch (FirebaseAuthInvalidUserException ex) {
                                        Toast.makeText(getContext(), "Incorrect email", Toast.LENGTH_SHORT).show();
                                    } catch (Exception ex) {
                                        Toast.makeText(getContext(), task.getException().toString(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        });
            }
        });
    }


}