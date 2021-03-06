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
                email.setError("E-mail ????????????????????????????");
                return;
            } else {
                email.setError(null);
            }

            pd.show();
            model.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString()).observe(getViewLifecycleOwner(), s -> {
                if(!s.equals("")) {
                    pd.dismiss();
                }
                switch (s) {
                    case "???? ?????????????? ?????????? ?? ??????????????":
                        Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show();
                        model.signInWithEmailAndPassword(" ", " ").removeObservers(getViewLifecycleOwner());
                        Navigation.findNavController(submitButton).navigate(R.id.action_authentificationFragment_to_selectChatFragment);
                        break;
                    case "???????????????????????? e-mail":
                        email.setError("???????????????????????? e-mail");
                        break;
                    case "???????????????????????? ????????????":
                        password.setError("???????????????????????? ????????????");
                        break;
                    case "":
                        break;
                    default:
                        Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show();
                        break;
                }
            });

        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}