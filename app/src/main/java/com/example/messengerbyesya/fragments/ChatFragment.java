package com.example.messengerbyesya.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messengerbyesya.Dialogs;
import com.example.messengerbyesya.MainActivityViewModel;
import com.example.messengerbyesya.R;
import com.example.messengerbyesya.adapters.MessagesRecyclerViewAdapter;
import com.example.messengerbyesya.model.Message;
import com.example.messengerbyesya.model.User;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.Date;

public class ChatFragment extends BaseFragment {

    public ChatFragment() {
        // Required empty public constructor
    }

    public static ChatFragment newInstance() {
        return new ChatFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        photoPickerActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();

                        View inflatedDialogView = requireActivity().getLayoutInflater().inflate(R.layout.send_image_dialog, null);

                        ImageView imageView = inflatedDialogView.findViewById(R.id.pickedImageImageView);
                        EditText imageContentDescriptionEditText = inflatedDialogView.findViewById(R.id.imageContentDescriptionEditText);
                        imageView.setImageURI(data.getData());

                        AlertDialog.Builder builder =
                                new AlertDialog.Builder(requireContext()).
                                        setTitle(getString(R.string.send_image)).
                                        setView(inflatedDialogView).
                                        setPositiveButton(getString(R.string.yes), null).
                                        setNegativeButton(getString(R.string.no), (dialog, i) -> {
                                            dialog.dismiss();
                                        });
                        AlertDialog imageMessageAlertDialog = builder.create();

                        imageMessageAlertDialog.setOnShowListener(dialogInterface -> {
                            imageMessageAlertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(view -> {
                                imageContentDescriptionEditText.setError(null);
                                if (imageContentDescriptionEditText.getText().toString().trim().isEmpty()) {
                                    imageContentDescriptionEditText.setError(getString(R.string.enter_image_content_description));
                                    return;
                                }
                                Message imageMessage = new Message(model.getCurrentUserId() + ";" + new Date().toString(), new Date(), model.getCurrentUser().getEmail());
                                imageMessage.setMediaResource(true);
                                imageMessage.setContentDescription(imageContentDescriptionEditText.getText().toString());
                                model.sendImage(imageMessage, ((BitmapDrawable) imageView.getDrawable()).getBitmap());
                                imageMessageAlertDialog.dismiss();
                            });
                        });

                        imageMessageAlertDialog.show();
                    }
                });

        someActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        currentAvatarImageView.setImageURI(data.getData());
                    }
                });
    }

    private View inflatedView;
    private MainActivityViewModel model;
    private TextView nameTextView;
    private ImageView avatarImageView;
    private NavigationView navigationView;
    private View header;
    private RecyclerView messagesRecyclerView;
    private FirestoreRecyclerAdapter adapter;
    private Button sendMessageButton;
    private Button addMediaResourceButton;
    private FloatingActionButton openNavigationViewButton;
    private DrawerLayout drawerLayout;
    private TextView messageTextView;
    private final MessagesRecyclerViewAdapter recyclerViewAdapter = new MessagesRecyclerViewAdapter();
    private ActivityResultLauncher<Intent> photoPickerActivityResultLauncher;
    private ActivityResultLauncher<Intent> someActivityResultLauncher;
    private ImageView chatImageImageView;
    private TextView chatOrUserNameTextView;
    private Button optionButton;
    private Dialogs dialogs = new Dialogs();
    private ImageView currentAvatarImageView;
    private AlertDialog alertDialog;
    private ProgressDialog pd;
    private RecyclerView.AdapterDataObserver adapterDataObserver;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        inflatedView = inflater.inflate(R.layout.fragment_chat, container, false);
        // Inflate the layout for this fragment
        return inflatedView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        model = new ViewModelProvider(getActivity()).get(MainActivityViewModel.class);

        navigationView = inflatedView.findViewById(R.id.selectChatFragmentNavigationView);
        header = navigationView.getHeaderView(0);
        nameTextView = header.findViewById(R.id.nameTextView);
        avatarImageView = header.findViewById(R.id.avatarImageView);
        avatarImageView.setContentDescription("Ваш аватар");
        chatImageImageView = inflatedView.findViewById(R.id.chatImageImageView);
        chatOrUserNameTextView = inflatedView.findViewById(R.id.chatOrUserNameTextView);
        optionButton = inflatedView.findViewById(R.id.optionButton);

        updateInformationAboutChat();

        model.getUserFromDB(model.getCurrentFirebaseUser().getEmail()).addOnSuccessListener(queryDocumentSnapshots -> {
            model.setCurrentUser(queryDocumentSnapshots.getDocuments().get(0).toObject(User.class));
            model.setCurrentUserId(queryDocumentSnapshots.getDocuments().get(0).getId());
            nameTextView.setText(model.getCurrentUser().getName());

            checkAvatar(model, avatarImageView);

            model.readMessage();

            messagesRecyclerView = inflatedView.findViewById(R.id.messagesRecyclerView);

            //Увеличение кэша прогружаемых item'ов
//            messagesRecyclerView.setItemViewCacheSize(10);
//            messagesRecyclerView.getRecycledViewPool().setMaxRecycledViews(1, 20);
//            messagesRecyclerView.getRecycledViewPool().setMaxRecycledViews(2, 20);


            LinearLayoutManager layoutManager = new LinearLayoutManager(inflatedView.getContext());
            layoutManager.setStackFromEnd(true);
            layoutManager.setReverseLayout(false);
            messagesRecyclerView.setLayoutManager(layoutManager);

            recyclerViewInitialization();

        });

        navigationView.setNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.settingFragment:
                    Navigation.findNavController(inflatedView).navigate(R.id.action_chatFragment_to_settingFragment);
                    break;
                case R.id.logOut:
                    model.signOut();
                    Navigation.findNavController(inflatedView).navigate(R.id.action_chatFragment_to_authentificationFragment);
                    break;
            }
            return false;
        });

        messageTextView = inflatedView.findViewById(R.id.editTextMessage);

        addMediaResourceButton = inflatedView.findViewById(R.id.addMediaResourceButton);
        addMediaResourceButton.setOnClickListener(view12 -> {
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            photoPickerActivityResultLauncher.launch(photoPickerIntent);
        });

        sendMessageButton = inflatedView.findViewById(R.id.sendMessageButton);
        sendMessageButton.setOnClickListener(v -> {
            if (!(messageTextView.getText().toString().isEmpty())) {
                model.sendMessage(new Message(messageTextView.getText().toString(), new Date(), model.getCurrentUser().getEmail()));
                messageTextView.setText("");
            }
        });

        optionButton.setOnClickListener(view13 -> {
            PopupMenu menu = new PopupMenu(requireContext(), view13);
            menu.inflate(R.menu.chat_options_menu);

            SpannableString s = new SpannableString(getString(R.string.leave_from_chat));
            s.setSpan(new ForegroundColorSpan(Color.RED), 0, s.length(), 0);
            menu.getMenu().getItem(6).setTitle(s);

            if (model.getCurrentChat().getUsersEmails().size() == 2) {
                menu.getMenu().getItem(3).setEnabled(false);
                menu.getMenu().getItem(4).setEnabled(false);
                menu.getMenu().getItem(5).setEnabled(false);
            }

            menu.setOnMenuItemClickListener(menuItem -> {
                switch (menuItem.getItemId()) {
                    case R.id.inviteParticipants:
                        model.setCurrentActionType(CreateChatFragment.ActionType.inviteParticipants);
                        Navigation.findNavController(inflatedView).navigate(R.id.action_chatFragment_to_createChatFragment);
                        break;
                    case R.id.showChatParticipants:
                        model.setShowedItemType(ShowParticipantsOrAttachmentsFragment.ShowedItemType.participants);
                        Navigation.findNavController(inflatedView).navigate(R.id.action_chatFragment_to_showParticipantsOrAttachmentsFragment);
                        break;
                    case R.id.showChatAttachments:
                        model.setShowedItemType(ShowParticipantsOrAttachmentsFragment.ShowedItemType.attachments);
                        Navigation.findNavController(inflatedView).navigate(R.id.action_chatFragment_to_showParticipantsOrAttachmentsFragment);
                        break;
                    case R.id.changeChatName:
                        dialogs.getDialog(requireActivity(), Dialogs.DialogType.changeChatName, null, null, model.getCurrentChat()).show();
                        break;
                    case R.id.changeChatAvatar:
                        showChangeAvatarDialog();
                        break;
                    case R.id.deleteChatAvatar:
                        generateChatAvatar(model, chatImageImageView);
                        break;
                    case R.id.leaveFromChat:
                        model.leaveFromChat();
                        Navigation.findNavController(inflatedView).navigate(R.id.action_chatFragment_to_selectChatFragment); //TODO: Разобраться с анимацией
                        break;
                }
                return false;
            });
            menu.show();
        });

        drawerLayout = inflatedView.findViewById(R.id.selectChatFragmentDrawerLayout);
        openNavigationViewButton = inflatedView.findViewById(R.id.openNavigationViewButton);
        openNavigationViewButton.setOnClickListener(view1 -> drawerLayout.open());

    }

    private void updateInformationAboutChat() {
        if (model.getCurrentChat().getUsersEmails().size() == 2) {
            String userEmail;
            if (model.getCurrentChat().getUsersEmails().get(0).equals(model.getCurrentUser().getEmail())) {
                userEmail = model.getCurrentChat().getUsersEmails().get(1);
            } else {
                userEmail = model.getCurrentChat().getUsersEmails().get(0);
            }
            model.getUserFromDB(userEmail).addOnSuccessListener(requireActivity(), queryDocumentSnapshots -> {
                if ((queryDocumentSnapshots != null) && (!queryDocumentSnapshots.isEmpty())) {
                    User tempUser = queryDocumentSnapshots.getDocuments().get(0).toObject(User.class);
                    chatOrUserNameTextView.setText(tempUser.getName());
                    chatImageImageView.setContentDescription("Аватар пользователя " + tempUser.getName());
                    model.getImageDownloadUrl("avatars/" + tempUser.getAvatar()).addOnSuccessListener(uri -> Picasso.with(chatImageImageView.getContext()).load(uri).resize(50, 50).centerCrop().into(chatImageImageView));
                }
            });

        } else {
            chatOrUserNameTextView.setText(model.getCurrentChat().getChatName());
            chatImageImageView.setContentDescription("Аватар чата " + model.getCurrentChat().getChatName());
            if (model.getCurrentChat().getChatAvatar().isEmpty()) {
                generateChatAvatar(model, chatImageImageView);
            } else {
                model.getImageDownloadUrl(model.getCurrentChat().getChatAvatar()).addOnSuccessListener(uri -> Picasso.with(chatImageImageView.getContext()).load(uri).fit().centerCrop().into(chatImageImageView));
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(adapter!=null) {
            adapter.unregisterAdapterDataObserver(adapterDataObserver);
            adapter.stopListening();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        model.changeOnlineStatus(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        model.changeOnlineStatus(true);
    }

    public void recyclerViewInitialization() {

        adapter = recyclerViewAdapter.getAdapter(adapter, model.getCurrentUser().getEmail(), model.getCurrentChat().getChatId());

        adapterDataObserver = new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                messagesRecyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
                model.readMessage();
            }
        };
        adapter.registerAdapterDataObserver(adapterDataObserver);

        messagesRecyclerView.setAdapter(adapter);

        adapter.startListening();

    }

    public void showChangeAvatarDialog() {
        View dialogInflatedView = requireActivity().getLayoutInflater().inflate(R.layout.change_avatar_dialog, null);
        Button uploadFromDeviceButton = dialogInflatedView.findViewById(R.id.uploadFromDeviceButton);
        Button uploadWithLinkButton = dialogInflatedView.findViewById(R.id.uploadWithLinkButton);
        EditText photoLinkEditText = dialogInflatedView.findViewById(R.id.photoLinkEditText);
        currentAvatarImageView = dialogInflatedView.findViewById(R.id.pickedImageImageView);

        ((TextView) dialogInflatedView.findViewById(R.id.textView11)).setText(R.string.chat_current_avatar);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setView(dialogInflatedView);
        builder.setPositiveButton(R.string.accept, null);
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());

        alertDialog = builder.create();

        alertDialog.setOnShowListener(dialog -> {
            alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
            if (!(hasConnection(requireContext()))) {
                Toast.makeText(requireContext(), getString(R.string.check_your_internet_connection), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                return;
            }
            pd = new ProgressDialog(getParentFragment().getContext());
            pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pd.setCancelable(false);
            pd.setMessage("Loading...");
            pd.show();

            model.getChatAvatar(model.getCurrentChat().getChatAvatar()).addOnSuccessListener(uri -> Picasso.with(getContext()).load(uri).into(currentAvatarImageView, new Callback() {
                @Override
                public void onSuccess() {
                    currentAvatarImageView.setContentDescription("Текущий аватар чата");
                    pd.dismiss();
                }

                @Override
                public void onError() {

                }
            }));

            alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v12 -> {
                if (!(hasConnection(v12.getContext()))) {
                    Toast.makeText(v12.getContext(), getString(R.string.check_your_internet_connection), Toast.LENGTH_SHORT).show();
                    return;
                }
                currentAvatarImageView.setDrawingCacheEnabled(true);
                currentAvatarImageView.buildDrawingCache();
                pd.show();
                model.uploadChatAvatar(((BitmapDrawable) currentAvatarImageView.getDrawable()).getBitmap(), pd);
                dialog.dismiss();
            });
        });

        uploadFromDeviceButton.setOnClickListener(view1 -> {
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            someActivityResultLauncher.launch(photoPickerIntent);

            currentAvatarImageView.setContentDescription("Измененный аватар чата");
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
                        currentAvatarImageView.setContentDescription("Измененный аватар чата");
                        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                    }

                    @Override
                    public void onError() {
                        Toast.makeText(getContext(), getString(R.string.something_went_wrong_try_inserting_another_link_or_saving_the_image_to_the_device_memory), Toast.LENGTH_LONG).show();
                        pd.dismiss();
                    }
                });
            }
        });

        alertDialog.show();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }
}