package com.example.messengerbyesya.fragments;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messengerbyesya.MainActivityViewModel;
import com.example.messengerbyesya.R;
import com.example.messengerbyesya.model.Message;
import com.example.messengerbyesya.model.User;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Random;

public class ChatFragment extends Fragment {

    public ChatFragment() {
        // Required empty public constructor
    }

    public static ChatFragment newInstance() {
        return new ChatFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private View inflatedView;
    private MainActivityViewModel model;
    private TextView nameTextView;
    private ImageView avatarImageView;
    private NavigationView navigationView;
    private View header;
    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private final FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    private final StorageReference firebaseStorageRef = firebaseStorage.getReferenceFromUrl("gs://messenger-by-esya.appspot.com/");
    private Random random;
    private RecyclerView messagesRecyclerView;
    private FirestoreRecyclerAdapter adapter;
    private Timestamp timestamp;
    private Button sendMessageButton;
    private TextView messageTextView;
    private Date tempDate;
    private final Calendar dateNowCalendar = new GregorianCalendar();

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

        navigationView = inflatedView.findViewById(R.id.navigationView);
        header = navigationView.getHeaderView(0);
        nameTextView = header.findViewById(R.id.nameTextView);
        avatarImageView = header.findViewById(R.id.avatarImageView);

        random = new Random();

        firebaseFirestore.collection("user").whereEqualTo("email", firebaseAuth.getCurrentUser().getEmail()).get().addOnSuccessListener(queryDocumentSnapshots -> {
            model.setCurrentUser(queryDocumentSnapshots.getDocuments().get(0).toObject(User.class));
            model.setCurrentUserId(queryDocumentSnapshots.getDocuments().get(0).getId());
            nameTextView.setText(model.getCurrentUser().getName());
            if (model.getCurrentUser().getAvatar().equals("none")) {
                if (model.getCurrentUser().getName().split(" ").length > 2) {
                    model.getCurrentUser().setAvatar("none_" + model.getCurrentUser().getEmail());
                    firebaseFirestore.collection("user").document(model.getCurrentUserId()).set(model.getCurrentUser());

                    Picasso.with(getContext()).load("https://eu.ui-avatars.com/api/?name=" + model.getCurrentUser().getName().split(" ")[2] + "&size=512?&background=" + generateRandomColorHex()).into(avatarImageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            avatarImageView.setDrawingCacheEnabled(true);
                            avatarImageView.buildDrawingCache();
                            Bitmap bitmap = ((BitmapDrawable) avatarImageView.getDrawable()).getBitmap();
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                            byte[] data = baos.toByteArray();
                            firebaseStorageRef.child("none_" + model.getCurrentUser().getEmail()).putBytes(data);
                        }

                        @Override
                        public void onError() {

                        }
                    });

                } else {
                    model.getCurrentUser().setAvatar("none_" + model.getCurrentUser().getEmail());
                    firebaseFirestore.collection("user").document(model.getCurrentUserId()).set(model.getCurrentUser());
                    Picasso.with(getContext()).load("https://eu.ui-avatars.com/api/?name=" + model.getCurrentUser().getName().split(" ")[0] + "%20" + model.getCurrentUser().getName().split(" ")[1] + "&size=512?&background=" + generateRandomColorHex()).into(avatarImageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            avatarImageView.setDrawingCacheEnabled(true);
                            avatarImageView.buildDrawingCache();
                            Bitmap bitmap = ((BitmapDrawable) avatarImageView.getDrawable()).getBitmap();
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                            byte[] data = baos.toByteArray();
                            firebaseStorageRef.child("none_" + model.getCurrentUser().getEmail()).putBytes(data);
                        }

                        @Override
                        public void onError() {

                        }
                    });
                }
            } else {
                firebaseStorageRef.child(model.getCurrentUser().getAvatar()).getDownloadUrl().addOnSuccessListener(uri -> Picasso.with(getContext()).load(uri).into(avatarImageView));
            }

            messagesRecyclerView = inflatedView.findViewById(R.id.chatRecyclerView);

            //TODO: Добавить float action button для выдвижения шторки меню
            //Увеличение кэша прогружаемых item'ов
            messagesRecyclerView.setItemViewCacheSize(10);
            messagesRecyclerView.getRecycledViewPool().setMaxRecycledViews(1, 20);
            messagesRecyclerView.getRecycledViewPool().setMaxRecycledViews(2, 20);


            LinearLayoutManager layoutManager = new LinearLayoutManager(inflatedView.getContext());
            layoutManager.setStackFromEnd(true);
            messagesRecyclerView.setLayoutManager(layoutManager);


            recyclerViewInitialization();

            firebaseFirestore.collection("message").orderBy("date").addSnapshotListener((value, error) -> {
                if (value != null) {
                    for (DocumentChange document : value.getDocumentChanges()) {
                        Message tempMessage = new Message();
                        timestamp = (Timestamp) document.getDocument().get("date");
                        tempMessage.setDate(timestamp.toDate());
                        tempMessage.setText((String) document.getDocument().get("text"));
                        firebaseFirestore.collection("user").whereEqualTo("email", document.getDocument().get("sender_email")).get().addOnSuccessListener(queryDocumentSnapshots1 -> {
                            User tempUser = new User();
                            tempUser = queryDocumentSnapshots1.getDocuments().get(0).toObject(User.class);
                            tempMessage.setSender(tempUser);
                            model.addMessage(tempMessage);
                            messagesRecyclerView.smoothScrollToPosition(adapter.getItemCount());
                            //adapter.notifyItemInserted(model.getMessages().size() - 1);
                        });
                    }
                }
            });

        });

        navigationView.setNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.settingFragment:
                    Navigation.findNavController(inflatedView).navigate(R.id.action_chatFragment_to_settingFragment);
                    break;
                case R.id.logOut:
                    firebaseAuth.signOut();
                    Navigation.findNavController(inflatedView).navigate(R.id.action_chatFragment_to_authentificationFragment);
                    break;
            }
            return false;
        });

        messageTextView = inflatedView.findViewById(R.id.editTextMessage);
        sendMessageButton = inflatedView.findViewById(R.id.sendMessageButton);
        sendMessageButton.setOnClickListener(v -> {
            if (!(messageTextView.getText().toString().isEmpty())) {
                firebaseFirestore.collection("message").add(new Message(messageTextView.getText().toString(), new Date(), model.getCurrentUser().getEmail()));
                messageTextView.setText("");
            }
        });

    }

    public String generateRandomColorHex() {
        String color = Integer.toString(random.nextInt(0X1000000), 16);
        return "000000".substring(color.length()) + color;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        adapter.stopListening();
    }

    public void recyclerViewInitialization() {
        Query query = firebaseFirestore.collection("message").orderBy("date");

        FirestoreRecyclerOptions<Message> options = new FirestoreRecyclerOptions.Builder<Message>().setQuery(query, snapshot -> {
            Message tempMessage = new Message();
            timestamp = (Timestamp) snapshot.get("date");
            tempMessage.setDate(timestamp.toDate());
            tempMessage.setText((String) snapshot.get("text"));
            tempMessage.setSender_email((String) snapshot.get("sender_email"));

            return tempMessage;
        }).build();

        adapter = new FirestoreRecyclerAdapter<Message, RecyclerView.ViewHolder>(options) {

            private static final int MESSAGE_FROM_CURRENT_USER_TYPE = 1;
            private static final int MESSAGE_FROM_OTHER_USERS_TYPE = 2;

            @Override
            protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull Message message) {
                switch (holder.getItemViewType()){
                    case MESSAGE_FROM_CURRENT_USER_TYPE:
                        onBindViewHolderCurrentUser((CurrentUserViewHolder) holder, position, message);
                        break;
                    case MESSAGE_FROM_OTHER_USERS_TYPE:
                        onBindViewHolderOtherUsers((OtherUsersViewHolder) holder, position, message);
                        break;
                }

            }

            private void onBindViewHolderOtherUsers(OtherUsersViewHolder holder, int position, Message message) {
                holder.messageTextView.setText(message.getText());
                holder.timeTextView.setText(message.getDate().toString());

                tempDate = new Date();
                tempDate.setTime(message.getDate().getTime());

                dateNowCalendar.setTimeInMillis(new Date().getTime());
                dateNowCalendar.set(Calendar.HOUR, 0);
                dateNowCalendar.set(Calendar.MINUTE, 0);
                dateNowCalendar.set(Calendar.SECOND, 0);

                if (dateNowCalendar.getTime().before(tempDate)) {
                    holder.timeTextView.setText(new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH).format(tempDate));
                } else {
                    holder.timeTextView.setText(new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH).format(tempDate));
                }

                firebaseFirestore.collection("user").whereEqualTo("email", message.getSender_email()).get().addOnSuccessListener(queryDocumentSnapshots -> {
                    User tempUser = new User();
                    tempUser = queryDocumentSnapshots.getDocuments().get(0).toObject(User.class);
                    message.setSender(tempUser);
                    holder.senderNameTextView.setText(message.getSender().getName());
                    firebaseStorageRef.child(message.getSender().getAvatar()).getDownloadUrl().addOnSuccessListener(uri -> Picasso.with(holder.senderAvatarImageView.getContext()).load(uri).resize(50, 50).centerCrop().into(holder.senderAvatarImageView));
                });
            }

            private void onBindViewHolderCurrentUser(CurrentUserViewHolder holder, int position, Message message) {
                holder.messageTextView.setText(message.getText());
                holder.timeTextView.setText(message.getDate().toString());

                tempDate = new Date();
                tempDate.setTime(message.getDate().getTime());

                dateNowCalendar.setTimeInMillis(new Date().getTime());
                dateNowCalendar.set(Calendar.HOUR, 0);
                dateNowCalendar.set(Calendar.MINUTE, 0);
                dateNowCalendar.set(Calendar.SECOND, 0);

                if (dateNowCalendar.getTime().before(tempDate)) {
                    holder.timeTextView.setText(new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH).format(tempDate));
                } else {
                    holder.timeTextView.setText(new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH).format(tempDate));
                }

                firebaseFirestore.collection("user").whereEqualTo("email", message.getSender_email()).get().addOnSuccessListener(queryDocumentSnapshots -> {
                    User tempUser = new User();
                    tempUser = queryDocumentSnapshots.getDocuments().get(0).toObject(User.class);
                    message.setSender(tempUser);
                    holder.senderNameTextView.setText(message.getSender().getName());
                    firebaseStorageRef.child(message.getSender().getAvatar()).getDownloadUrl().addOnSuccessListener(uri -> Picasso.with(holder.senderAvatarImageView.getContext()).load(uri).resize(50, 50).centerCrop().into(holder.senderAvatarImageView));
                });
            }

            @Override
            public int getItemViewType(int position) {
                if (getItem(position).getSender_email().equals(model.getCurrentUser().getEmail())) {
                    return MESSAGE_FROM_CURRENT_USER_TYPE;
                } else {
                    return MESSAGE_FROM_OTHER_USERS_TYPE;
                }
            }

            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup group, int viewType) {

                if (viewType == MESSAGE_FROM_CURRENT_USER_TYPE) {
                    View view = LayoutInflater.from(group.getContext()).inflate(R.layout.chat_current_user_recycler_view_item, group, false);
                    return new CurrentUserViewHolder(view);
                } else if (viewType == MESSAGE_FROM_OTHER_USERS_TYPE) {
                    View view = LayoutInflater.from(group.getContext()).inflate(R.layout.chat_other_users_recycler_view_item, group, false);
                    return new OtherUsersViewHolder(view);
                }

                return null;
            }
        };

        messagesRecyclerView.setAdapter(adapter);

        adapter.startListening();

    }

    public static class OtherUsersViewHolder extends RecyclerView.ViewHolder {
        TextView senderNameTextView;
        TextView messageTextView;
        TextView timeTextView;
        ImageView senderAvatarImageView;

        OtherUsersViewHolder(View view) {
            super(view);
            senderNameTextView = view.findViewById(R.id.senderNameTextView);
            messageTextView = view.findViewById(R.id.messageTextView);
            timeTextView = view.findViewById(R.id.timeTextView);
            senderAvatarImageView = view.findViewById(R.id.senderAvatarImageView);
        }

    }

    public static class CurrentUserViewHolder extends RecyclerView.ViewHolder {
        TextView senderNameTextView;
        TextView messageTextView;
        TextView timeTextView;
        ImageView senderAvatarImageView;

        CurrentUserViewHolder(View view) {
            super(view);
            senderNameTextView = view.findViewById(R.id.senderNameTextView2);
            messageTextView = view.findViewById(R.id.messageTextView2);
            timeTextView = view.findViewById(R.id.timeTextView2);
            senderAvatarImageView = view.findViewById(R.id.senderAvatarImageView2);
        }

    }
}