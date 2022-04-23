package com.example.messengerbyesya.fragments;

import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.messengerbyesya.MainActivityViewModel;
import com.example.messengerbyesya.R;
import com.example.messengerbyesya.adapters.ChatsRecyclerViewAdapter;
import com.example.messengerbyesya.model.Chat;
import com.example.messengerbyesya.model.User;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.Random;

public class SelectChatFragment extends Fragment {

    public static SelectChatFragment newInstance() {
        return new SelectChatFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    private View inflatedView;
    private RecyclerView allChatsRecyclerView;
    private FirestoreRecyclerAdapter adapter;
    private final ChatsRecyclerViewAdapter allChatsRecyclerViewAdapter = new ChatsRecyclerViewAdapter();
    private MainActivityViewModel model;
    private TextView nameTextView;
    private ImageView avatarImageView;
    private NavigationView navigationView;
    private View header;
    private Random random;
    private DrawerLayout drawerLayout;
    private FloatingActionButton openNavigationViewButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        inflatedView = inflater.inflate(R.layout.fragment_select_chat, container, false);

        // Inflate the layout for this fragment
        return inflatedView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        model = new ViewModelProvider(requireActivity()).get(MainActivityViewModel.class);

        //TODO: Попробовать вынести этот код в BaseFragment

        navigationView = inflatedView.findViewById(R.id.selectChatFragmentNavigationView);
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

            class onItemClickListenerClass implements ChatsRecyclerViewAdapter.ItemClickListener {

                @Override
                public void onItemClick(int position) {
                    model.setCurrentChat(((Chat) (adapter.getItem(position))).getChatId());
                    Navigation.findNavController(inflatedView).navigate(R.id.action_selectChatFragment_to_chatFragment);
                }
            }

            adapter = allChatsRecyclerViewAdapter.getAdapter(adapter, model.getCurrentUser().getEmail(), new onItemClickListenerClass());

            adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onItemRangeInserted(int positionStart, int itemCount) {
                    allChatsRecyclerView.smoothScrollToPosition(positionStart);
                }
            });

            allChatsRecyclerView.setAdapter(adapter);

            adapter.startListening();

        });

        allChatsRecyclerView = inflatedView.findViewById(R.id.allChatsRecyclerView);

        //Увеличение кэша прогружаемых item'ов
        allChatsRecyclerView.setItemViewCacheSize(10);
        allChatsRecyclerView.getRecycledViewPool().setMaxRecycledViews(1, 20);
        allChatsRecyclerView.getRecycledViewPool().setMaxRecycledViews(2, 20);


        LinearLayoutManager layoutManager = new LinearLayoutManager(inflatedView.getContext());
        allChatsRecyclerView.setLayoutManager(layoutManager);

        drawerLayout = inflatedView.findViewById(R.id.selectChatFragmentDrawerLayout);
        openNavigationViewButton = inflatedView.findViewById(R.id.openNavigationViewButton);
        openNavigationViewButton.setOnClickListener(view1 -> drawerLayout.open());

        navigationView.setNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.settingFragment:
                    Navigation.findNavController(inflatedView).navigate(R.id.action_selectChatFragment_to_settingFragment);
                    break;
                case R.id.logOut:
                    model.signOut();
                    Navigation.findNavController(inflatedView).navigate(R.id.action_selectChatFragment_to_authentificationFragment);
                    break;
            }
            return false;
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        adapter.stopListening();
    }

    public String generateRandomColorHex() {
        String color = Integer.toString(random.nextInt(0X1000000), 16);
        return "000000".substring(color.length()) + color;
    }
}