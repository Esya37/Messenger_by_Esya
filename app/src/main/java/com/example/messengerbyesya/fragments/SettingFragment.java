package com.example.messengerbyesya.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;

import com.example.messengerbyesya.Dialogs;
import com.example.messengerbyesya.MainActivityViewModel;
import com.example.messengerbyesya.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class SettingFragment extends BaseFragment {

    public SettingFragment() {
        // Required empty public constructor
    }

    public static SettingFragment newInstance() {
        return new SettingFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        someActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        imageUri = data.getData();
                        currentAvatarImageView.setImageURI(imageUri);
                    }
                });
    }

    private View inflatedView;
    private MainActivityViewModel model;
    private Button settingItemButton;
    private final Dialogs dialogs = new Dialogs();
    private View dialogInflatedView;
    private Button uploadFromDeviceButton;
    private Button uploadWithLinkButton;
    private EditText photoLinkEditText;
    private ImageView currentAvatarImageView;
    private Uri imageUri;
    private ActivityResultLauncher<Intent> someActivityResultLauncher;
    private AlertDialog alertDialog;
    private ProgressDialog pd;

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
        //buttonSetOnClickListener(R.id.changeAvatarButton, Dialogs.DialogType.changeAvatar);
        buttonSetOnClickListener(R.id.deleteAvatarButton, Dialogs.DialogType.deleteAvatar);

        settingItemButton = inflatedView.findViewById(R.id.changeAvatarButton);
        settingItemButton.setOnClickListener(v -> {
            dialogInflatedView = requireActivity().getLayoutInflater().inflate(R.layout.change_avatar_dialog, null);
            uploadFromDeviceButton = dialogInflatedView.findViewById(R.id.uploadFromDeviceButton);
            uploadWithLinkButton = dialogInflatedView.findViewById(R.id.uploadWithLinkButton);
            photoLinkEditText = dialogInflatedView.findViewById(R.id.photoLinkEditText);
            currentAvatarImageView = dialogInflatedView.findViewById(R.id.currentAvatarImageView);

            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setView(dialogInflatedView);
            builder.setPositiveButton(R.string.accept, null);
            builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());

            uploadFromDeviceButton.setOnClickListener(view1 -> {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                someActivityResultLauncher.launch(photoPickerIntent);

                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
            });

            uploadWithLinkButton.setOnClickListener(v1 -> {
                if (photoLinkEditText.getText().toString().isEmpty()) {
                    photoLinkEditText.setError("Вставьте ссылку на фото");
                } else {
                    pd.show();
                    photoLinkEditText.setError(null);

                    Picasso.with(getContext()).load(model.getFinalImageUri(photoLinkEditText.getText().toString())).into(currentAvatarImageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            pd.dismiss();
                            alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                        }

                        @Override
                        public void onError() {
                            pd.dismiss();
                        }
                    });
                }
            });

            alertDialog = builder.create();

            alertDialog.setOnShowListener(dialog -> {
                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                if (!(hasConnection(v.getContext()))) {
                    Toast.makeText(v.getContext(), "Check your Internet connection", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    return;
                }
                pd = new ProgressDialog(getParentFragment().getContext());
                pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                pd.setCancelable(false);
                pd.setMessage("Loading...");
                pd.show();

                model.getAvatar(model.getCurrentUser()).addOnSuccessListener(uri -> Picasso.with(getContext()).load(uri).into(currentAvatarImageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        pd.dismiss();
                    }

                    @Override
                    public void onError() {
//                        if (isUsedGoogleOrYandexImages) {
                        Toast.makeText(getContext(), "Что-то пошло не так:( Попробуйте вставить другую ссылку или сохраните изображение в память устройства", Toast.LENGTH_LONG).show();
//                        } else {
//                            Toast.makeText(getContext(), "Что-то пошло не так:( Попробуйте вставить ссылку из Google Картинки или Яндекс.Картинки", Toast.LENGTH_LONG).show();
//                        }
                    }
                }));

                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v12 -> {
                    if (!(hasConnection(v12.getContext()))) {
                        Toast.makeText(v12.getContext(), "Check your Internet connection", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    currentAvatarImageView.setDrawingCacheEnabled(true);
                    currentAvatarImageView.buildDrawingCache();
                    pd.show();
                    model.uploadAvatar(((BitmapDrawable) currentAvatarImageView.getDrawable()).getBitmap(), model.getCurrentUser(), model.getCurrentUserId(), pd);
                    dialog.dismiss();
                });
            });

            alertDialog.show();
        });


    }

    public void buttonSetOnClickListener(int buttonId, Dialogs.DialogType dialogType) {

        settingItemButton = inflatedView.findViewById(buttonId);
        settingItemButton.setOnClickListener(v -> dialogs.getDialog(requireActivity(), dialogType, model.getCurrentUser(), model.getCurrentUserId()).show());

    }


}