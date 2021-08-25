package com.example.messengerbyesya.fragments;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.messengerbyesya.Dialogs;
import com.example.messengerbyesya.MainActivityViewModel;
import com.example.messengerbyesya.R;

public class SettingFragment extends Fragment {

    public SettingFragment() {
        // Required empty public constructor
    }

    public static SettingFragment newInstance() {
        return new SettingFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private View inflatedView;
    private MainActivityViewModel model;
    private Button settingItemButton;
    private Dialogs dialogs = new Dialogs();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        inflatedView = inflater.inflate(R.layout.fragment_setting, container, false);
        // Inflate the layout for this fragment
        return inflatedView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        model = new ViewModelProvider(getActivity()).get(MainActivityViewModel.class);

        buttonSetOnClickListener(R.id.changeNameButton, Dialogs.DialogType.changeName);
        buttonSetOnClickListener(R.id.changePasswordButton, Dialogs.DialogType.changePassword);
        buttonSetOnClickListener(R.id.changeAvatarButton, Dialogs.DialogType.changeAvatar);
        buttonSetOnClickListener(R.id.deleteAvatarButton, Dialogs.DialogType.deleteAvatar);

    }

    public void buttonSetOnClickListener(int buttonId, Dialogs.DialogType dialogType) {

        settingItemButton = inflatedView.findViewById(buttonId);
        settingItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogs.getDialog(requireActivity(), dialogType, model.getCurrentUser(), model.getCurrentUserId()).show();
            }
        });

    }
}