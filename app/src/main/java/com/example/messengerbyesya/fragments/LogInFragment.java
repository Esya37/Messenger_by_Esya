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
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.messengerbyesya.MainActivityViewModel;
import com.example.messengerbyesya.R;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private MainActivityViewModel model;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private ProgressDialog pd;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        inflatedView = inflater.inflate(R.layout.fragment_log_in, container, false);
        // Inflate the layout for this fragment
        return inflatedView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        model = new ViewModelProvider(getActivity()).get(MainActivityViewModel.class);

        email = inflatedView.findViewById(R.id.editTextTextEmailAddressLogIn);
        password = inflatedView.findViewById(R.id.editTextTextPasswordLogIn);

        pd = new ProgressDialog(getParentFragment().getContext());
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.setCancelable(false);
        pd.setMessage("Loading...");

        submitButton = inflatedView.findViewById(R.id.logInSubmitButton);
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

            pd.show();
            executorService.execute(() -> {
                String resultToastText = model.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString());
                requireActivity().runOnUiThread(() -> {
                    pd.dismiss();
                    switch (resultToastText) {
                        case "Вы успешно вошли в аккаунт":
                            Toast.makeText(getContext(), resultToastText, Toast.LENGTH_SHORT).show();
                            Navigation.findNavController(submitButton).navigate(R.id.action_authentificationFragment_to_selectChatFragment);
                            break;
                        case "Неправильный e-mail":
                            email.setError("Неправильный e-mail");
                            break;
                        case "Неправильный пароль":
                            password.setError("Неправильный пароль");
                            break;
                        default:
                            Toast.makeText(getContext(), resultToastText, Toast.LENGTH_SHORT).show();
                            break;
                    }
                });
            });

        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}