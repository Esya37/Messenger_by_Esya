package com.example.messengerbyesya.fragments;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.messengerbyesya.MainActivityViewModel;
import com.example.messengerbyesya.R;
import com.example.messengerbyesya.model.Message;
import com.example.messengerbyesya.model.User;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.SnapshotParser;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    private StorageReference firebaseStorageRef = firebaseStorage.getReferenceFromUrl("gs://messenger-by-esya.appspot.com/");
    private Random random;
    private RecyclerView messagesRecyclerView;
    private FirestoreRecyclerAdapter adapter;
    private Timestamp timestamp;
    private Button sendMessageButton;
    private TextView messageTextView;
    private Date tempDate;

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

        firebaseFirestore.collection("user").whereEqualTo("email", firebaseAuth.getCurrentUser().getEmail()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
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
                    firebaseStorageRef.child(model.getCurrentUser().getAvatar()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Picasso.with(getContext()).load(uri).into(avatarImageView);
                        }
                    });
                }

                messagesRecyclerView = inflatedView.findViewById(R.id.chatRecyclerView);
                messagesRecyclerView.setLayoutManager(new LinearLayoutManager(inflatedView.getContext()));

                recyclerViewInitialization();

                firebaseFirestore.collection("message").orderBy("date").addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        for (DocumentChange document : value.getDocumentChanges()) {
                            Message tempMessage = new Message();
                            timestamp = (Timestamp) document.getDocument().get("date");
                            tempMessage.setDate(timestamp.toDate());
                            tempMessage.setText((String) document.getDocument().get("text"));
                            firebaseFirestore.collection("user").whereEqualTo("email", document.getDocument().get("sender_email")).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                    User tempUser = new User();
                                    tempUser = queryDocumentSnapshots.getDocuments().get(0).toObject(User.class);
                                    tempMessage.setSender(tempUser);
                                    model.addMessage(tempMessage);
                                    //adapter.notifyItemInserted(model.getMessages().size() - 1);
                                }
                            });
                        }
                    }
                });

            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
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
            }
        });

        messageTextView = inflatedView.findViewById(R.id.editTextMessage);
        sendMessageButton = inflatedView.findViewById(R.id.sendMessageButton);
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    public void recyclerViewInitialization() {
        Query query = firebaseFirestore.collection("message").orderBy("date").limitToLast(50);

        FirestoreRecyclerOptions<Message> options = new FirestoreRecyclerOptions.Builder<Message>().setQuery(query, new SnapshotParser<Message>() {
            @NonNull
            @Override
            public Message parseSnapshot(@NonNull DocumentSnapshot snapshot) {
                Message tempMessage = new Message();
                timestamp = (Timestamp) snapshot.get("date");
                tempMessage.setDate(timestamp.toDate());
                tempMessage.setText((String) snapshot.get("text"));
                tempMessage.setSender_email((String) snapshot.get("sender_email"));

                return tempMessage;
            }
        }).build();

        adapter = new FirestoreRecyclerAdapter<Message, ViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Message message) {
                holder.messageTextView.setText(message.getText());
                holder.timeTextView.setText(message.getDate().toString());

                tempDate = new Date();
                tempDate.setTime(message.getDate().getTime());
                //TODO Переделать время в 24-часовой формат
                //TODO Сделать перемотку в самый низ recyclerView при появлении новых сообщений
                //TODO Посмотреть про подгрузку новых данных во время скроллинга
                if ((int) (new Date().getTime() - message.getDate().getTime()) / (24 * 60 * 60 * 1000) == 0) {
                    holder.timeTextView.setText(new SimpleDateFormat("hh:mm:ss").format(tempDate));
                } else {
                    holder.timeTextView.setText(new SimpleDateFormat("dd.MM.yyyy").format(tempDate));
                }

                firebaseFirestore.collection("user").whereEqualTo("email", message.getSender_email()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        User tempUser = new User();
                        tempUser = queryDocumentSnapshots.getDocuments().get(0).toObject(User.class);
                        message.setSender(tempUser);
                        holder.senderNameTextView.setText(message.getSender().getName());
                        firebaseStorageRef.child(message.getSender().getAvatar()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Picasso.with(holder.senderAvatarImageView.getContext()).load(uri).resize(50, 50).centerCrop().into(holder.senderAvatarImageView);
                            }
                        });
                    }
                });

            }

            @Override
            public ViewHolder onCreateViewHolder(ViewGroup group, int i) {

                View view = LayoutInflater.from(group.getContext()).inflate(R.layout.chat_recycler_view_item, group, false);

                return new ViewHolder(view);
            }
        };

        messagesRecyclerView.setAdapter(adapter);

        adapter.startListening();

    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView senderNameTextView;
        TextView messageTextView;
        TextView timeTextView;
        ImageView senderAvatarImageView;

        ViewHolder(View view) {
            super(view);
            senderNameTextView = view.findViewById(R.id.senderNameTextView);
            messageTextView = view.findViewById(R.id.messageTextView);
            timeTextView = view.findViewById(R.id.timeTextView);
            senderAvatarImageView = view.findViewById(R.id.senderAvatarImageView);
        }

    }
}