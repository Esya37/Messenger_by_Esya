package com.example.messengerbyesya.fragments;

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
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messengerbyesya.MainActivityViewModel;
import com.example.messengerbyesya.R;
import com.example.messengerbyesya.adapters.RecyclerViewAdapter;
import com.example.messengerbyesya.model.Message;
import com.example.messengerbyesya.model.User;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

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
    private Random random;
    private RecyclerView messagesRecyclerView;
    private FirestoreRecyclerAdapter adapter;
    private Button sendMessageButton;
    private FloatingActionButton openNavigationViewButton;
    private DrawerLayout drawerLayout;
    private TextView messageTextView;
    private final RecyclerViewAdapter recyclerViewAdapter = new RecyclerViewAdapter();


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

        model.getCurrentUserFromDB(model.getCurrentFirebaseUser().getEmail()).addOnSuccessListener(queryDocumentSnapshots -> {
            model.setCurrentUser(queryDocumentSnapshots.getDocuments().get(0).toObject(User.class));
            model.setCurrentUserId(queryDocumentSnapshots.getDocuments().get(0).getId());
            nameTextView.setText(model.getCurrentUser().getName());
            if (model.getCurrentUser().getAvatar().equals("none")) {
                if (model.getCurrentUser().getName().split(" ").length > 2) {
                    model.getCurrentUser().setAvatar("none_" + model.getCurrentUser().getEmail());
                    model.setUser(model.getCurrentUser(), model.getCurrentUserId());
                    //firebaseFirestore.collection("user").document(model.getCurrentUserId()).set(model.getCurrentUser());

                    Picasso.with(getContext()).load("https://eu.ui-avatars.com/api/?name=" + model.getCurrentUser().getName().split(" ")[2] + "&size=512?&background=" + generateRandomColorHex()).into(avatarImageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            avatarImageView.setDrawingCacheEnabled(true);
                            avatarImageView.buildDrawingCache();
                            model.uploadTempAvatar(((BitmapDrawable) avatarImageView.getDrawable()).getBitmap(), model.getCurrentUser());
                        }

                        @Override
                        public void onError() {

                        }
                    });

                } else {
                    model.getCurrentUser().setAvatar("none_" + model.getCurrentUser().getEmail());
                    model.setUser(model.getCurrentUser(), model.getCurrentUserId());
                    // firebaseFirestore.collection("user").document(model.getCurrentUserId()).set(model.getCurrentUser());
                    Picasso.with(getContext()).load("https://eu.ui-avatars.com/api/?name=" + model.getCurrentUser().getName().split(" ")[0] + "%20" + model.getCurrentUser().getName().split(" ")[1] + "&size=512?&background=" + generateRandomColorHex()).into(avatarImageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            avatarImageView.setDrawingCacheEnabled(true);
                            avatarImageView.buildDrawingCache();
                            model.uploadTempAvatar(((BitmapDrawable) avatarImageView.getDrawable()).getBitmap(), model.getCurrentUser());
                        }

                        @Override
                        public void onError() {

                        }
                    });
                }
            } else {
                model.getAvatar(model.getCurrentUser()).addOnSuccessListener(uri -> Picasso.with(getContext()).load(uri).into(avatarImageView));
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
        sendMessageButton = inflatedView.findViewById(R.id.sendMessageButton);
        sendMessageButton.setOnClickListener(v -> {
            if (!(messageTextView.getText().toString().isEmpty())) {
                model.sendMessage(new Message(messageTextView.getText().toString(), new Date(), model.getCurrentUser().getEmail()));
                messageTextView.setText("");
            }
        });

        drawerLayout = inflatedView.findViewById(R.id.drawer_layout);
        openNavigationViewButton = inflatedView.findViewById(R.id.openNavigationViewButton);
        openNavigationViewButton.setOnClickListener(view1 -> drawerLayout.open());

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

        adapter = recyclerViewAdapter.getAdapter(adapter, model.getCurrentUser().getEmail());

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                messagesRecyclerView.smoothScrollToPosition(positionStart);
            }
        });

        messagesRecyclerView.setAdapter(adapter);

        adapter.startListening();

    }


}