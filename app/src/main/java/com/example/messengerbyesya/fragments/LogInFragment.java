package com.example.messengerbyesya.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;

import com.example.messengerbyesya.R;
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
    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

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
        submitButton.setOnClickListener(v -> {
            if(!hasConnection(getContext())){
                Toast.makeText(getContext(), "Check your Internet connection", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!(isEmailValid(email.getText().toString()))) {
                email.setError("E-mail недействителен");
                return;
            }else {
                email.setError(null);
            }

            firebaseAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                    .addOnCompleteListener(task -> {
                        email.setError(null);
                        password.setError(null);
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Вы успешно вошли в аккаунт", Toast.LENGTH_SHORT).show();
                            Navigation.findNavController(submitButton).navigate(R.id.action_authentificationFragment_to_chatFragment);
                        } else {
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthInvalidCredentialsException ex) {
                                password.setError("Неправильный пароль");
                            } catch (FirebaseAuthInvalidUserException ex) {
                                email.setError("Неправильный e-mail");
                            } catch (Exception ex) {
                                Toast.makeText(getContext(), task.getException().toString(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        });
    }


}