package com.example.messengerbyesya.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private final FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    private final StorageReference firebaseStorageRef = firebaseStorage.getReferenceFromUrl("gs://messenger-by-esya.appspot.com/");
    private ActivityResultLauncher<Intent> someActivityResultLauncher;
    private AlertDialog alertDialog;
    private ProgressDialog pd;
    private ExecutorService executorService;
    private static Boolean isUsedGoogleOrYandexImages;

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
                    final URL[] secondURL = {null};
                    final String[] finalURL = {""};
                    executorService = Executors.newSingleThreadExecutor();
                    try {
                        executorService.submit(() -> {
                            try {
                                finalURL[0] = getFinalURL(photoLinkEditText.getText().toString());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return null;
                        }).get();
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }

                    Picasso.with(getContext()).load(finalURL[0]).into(currentAvatarImageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            pd.dismiss();
                            alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                        }

                        @Override
                        public void onError() {
                            pd.dismiss();
                            Log.d("qwe", "qwe");
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
                firebaseStorageRef.child(model.getCurrentUser().getAvatar()).getDownloadUrl().addOnSuccessListener(uri -> Picasso.with(getContext()).load(uri).into(currentAvatarImageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        pd.dismiss();
                    }

                    @Override
                    public void onError() {
                        if (isUsedGoogleOrYandexImages) {
                            Toast.makeText(getContext(), "Что-то пошло не так:( Попробуйте вставить другую ссылку или сохраните изображение в память устройства", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getContext(), "Что-то пошло не так:( Попробуйте вставить ссылку из Google Картинки или Яндекс.Картинки", Toast.LENGTH_LONG).show();
                        }
                    }
                }));

                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v12 -> {
                    if (!(hasConnection(v12.getContext()))) {
                        Toast.makeText(v12.getContext(), "Check your Internet connection", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    currentAvatarImageView.setDrawingCacheEnabled(true);
                    currentAvatarImageView.buildDrawingCache();
                    Bitmap bitmap = ((BitmapDrawable) currentAvatarImageView.getDrawable()).getBitmap();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] data = baos.toByteArray();
                    firebaseStorageRef.child("Avatar_" + model.getCurrentUser().getEmail()).putBytes(data);
                    //firebaseStorageRef.child("Avatar_" + model.getCurrentUser().getEmail()).putFile(imageUri);
                    model.getCurrentUser().setAvatar("Avatar_" + model.getCurrentUser().getEmail());
                    firebaseFirestore.collection("user").document(model.getCurrentUserId()).set(model.getCurrentUser());
                    firebaseStorageRef.child("none_" + model.getCurrentUser().getEmail()).delete();
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

    private static String getFinalURL(String url) throws IOException {
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        con.setInstanceFollowRedirects(false);
        con.connect();
        con.getInputStream();

        if (con.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM || con.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP) {
            String redirectUrl = con.getHeaderField("Location");
            return getFinalURL(redirectUrl);
        }
        isUsedGoogleOrYandexImages = false;
        if (url.contains("https://yandex.ru/images/")) {
            isUsedGoogleOrYandexImages = true;
            url = getUrlFromYandexImages(url);
        }
        if (url.contains("https://www.google.ru/imgres")) {
            isUsedGoogleOrYandexImages = true;
            url = getUrlFromGoogleImages(url);
        }
        return url;
    }

    private static String getUrlFromGoogleImages(String url) {
        url = url.substring(url.indexOf("?") + 1);
        for (String string : url.split("&")) {
            if (string.contains("imgurl")) {
                url = string.substring(7);
                break;
            }
        }
        return url;
    }

    private static String getUrlFromYandexImages(String url) {
        for (String string : url.split("&")) {
            if (string.contains("img_url")) {
                url = string.substring(8);
                break;
            }
        }
        url = url.replace("%3A", ":");
        url = url.replace("%2F", "/");
        url = url.replace("%3F", "?");
        url = url.replace("%26", "&");
        url = url.replace("%3D", "=");
        return url;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}