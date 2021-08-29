package com.example.messengerbyesya.fragments;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.messengerbyesya.Dialogs;
import com.example.messengerbyesya.MainActivityViewModel;
import com.example.messengerbyesya.R;
import com.example.messengerbyesya.model.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;

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
        someActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            imageUri = data.getData();
                            currentAvatarImageView.setImageURI(imageUri);
                        }
                    }
                });
    }

    private View inflatedView;
    private MainActivityViewModel model;
    private Button settingItemButton;
    private Dialogs dialogs = new Dialogs();
    private View dialogInflatedView;
    private Button uploadFromDeviceButton;
    private ImageView currentAvatarImageView;
    private Uri imageUri;
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    private StorageReference firebaseStorageRef = firebaseStorage.getReferenceFromUrl("gs://messenger-by-esya.appspot.com/");
    private ActivityResultLauncher<Intent> someActivityResultLauncher;
    private AlertDialog alertDialog;

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
        //TODO Сделать загрузку изображения из облака
        settingItemButton = inflatedView.findViewById(R.id.changeAvatarButton);
        settingItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogInflatedView = requireActivity().getLayoutInflater().inflate(R.layout.change_avatar_dialog, null);
                uploadFromDeviceButton = dialogInflatedView.findViewById(R.id.uploadFromDeviceButton);
                currentAvatarImageView = dialogInflatedView.findViewById(R.id.currentAvatarImageView);
                AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
                builder.setView(dialogInflatedView);
                builder.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        firebaseStorageRef.child("Avatar_" + model.getCurrentUser().getEmail()).putFile(imageUri);
                        model.getCurrentUser().setAvatar("Avatar_" + model.getCurrentUser().getEmail());
                        firebaseFirestore.collection("user").document(model.getCurrentUserId()).set(model.getCurrentUser());
                        firebaseStorageRef.child("none_"+model.getCurrentUser().getEmail()).delete();
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                uploadFromDeviceButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                        photoPickerIntent.setType("image/*");
                        someActivityResultLauncher.launch(photoPickerIntent);
                    }
                });

                alertDialog = builder.create();

                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        //TODO Добавить значок загрузки
                        firebaseStorageRef.child(model.getCurrentUser().getAvatar()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Picasso.with(getContext()).load(uri).into(currentAvatarImageView);
                            }
                        });
                    }
                });

                alertDialog.show();
            }
        });


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