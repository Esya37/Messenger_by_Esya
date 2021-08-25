package com.example.messengerbyesya.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.messengerbyesya.MainActivityViewModel;
import com.example.messengerbyesya.R;
import com.example.messengerbyesya.model.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

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
    private NavigationView navigationView;
    private View header;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

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

        firebaseFirestore.collection("user").whereEqualTo("email", firebaseAuth.getCurrentUser().getEmail()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                model.setCurrentUser(queryDocumentSnapshots.getDocuments().get(0).toObject(User.class));
                model.setCurrentUserId(queryDocumentSnapshots.getDocuments().get(0).getId());
                nameTextView.setText(model.getCurrentUser().getName());
            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
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

    }

}