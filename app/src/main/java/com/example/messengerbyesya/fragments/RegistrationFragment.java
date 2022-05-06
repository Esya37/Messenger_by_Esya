package com.example.messengerbyesya.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.messengerbyesya.MainActivityViewModel;
import com.example.messengerbyesya.R;
import com.example.messengerbyesya.model.User;
import com.example.messengerbyesya.services.Validation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private MainActivityViewModel model;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private ProgressDialog pd;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        inflatedView = inflater.inflate(R.layout.fragment_registration, container, false);
        // Inflate the layout for this fragment
        return inflatedView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        model = new ViewModelProvider(getActivity()).get(MainActivityViewModel.class);

        email = inflatedView.findViewById(R.id.editTextTextEmailAddressRegistration);
        password = inflatedView.findViewById(R.id.editTextTextPasswordRegistration);
        repeatedPassword = inflatedView.findViewById(R.id.editTextTextPassword2Registration);

        pd = new ProgressDialog(getParentFragment().getContext());
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.setCancelable(false);
        pd.setMessage("Loading...");

        submitButton = inflatedView.findViewById(R.id.registrationSubmitButton);
        submitButton.setOnClickListener(v -> {
            if (!hasConnection(getContext())) {
                Toast.makeText(getContext(), "Check your Internet connection", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!(isEmailValid(email.getText().toString()))) {
                email.setError("E-mail недействителен");
                return;
            } else {
                email.setError(null);
            }
            if (password.getText().toString().length() < 6) {
                password.setError("Длина пароля должна быть 6 символов или более");
                return;
            } else {
                password.setError(null);
            }
            if (!(password.getText().toString().equals(repeatedPassword.getText().toString()))) {
                password.setError("Пароли не совпадают");
                repeatedPassword.setError("Пароли не совпадают");
                return;
            } else {
                password.setError(null);
                repeatedPassword.setError(null);
            }

            pd.show();
            model.createUser(email.getText().toString(), password.getText().toString()).observe(getViewLifecycleOwner(), s -> {
                pd.dismiss();
                Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show();
                if (s.equals("Вы успешно зарегистрировались")) {
                    model.createUser(" ", " ").removeObservers(getViewLifecycleOwner());
                    Navigation.findNavController(submitButton).navigate(R.id.action_authentificationFragment_to_selectChatFragment);
                }
            });


        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}