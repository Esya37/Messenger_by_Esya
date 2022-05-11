package com.example.messengerbyesya.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messengerbyesya.MainActivityViewModel;
import com.example.messengerbyesya.R;
import com.example.messengerbyesya.adapters.UsersPaginationRecyclerViewAdapter;
import com.example.messengerbyesya.model.User;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CreateChatFragment extends BaseFragment {

    public enum ActionType {
        createChat,
        inviteParticipants
    }

    public static CreateChatFragment newInstance() {
        return new CreateChatFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private View inflatedView;
    private MainActivityViewModel model;
    private Map<String, Boolean> usersCheckedMap = new HashMap<>();
    private UsersPaginationRecyclerViewAdapter recyclerViewAdapter;
    private FirestorePagingAdapter<User, UsersPaginationRecyclerViewAdapter.UsersViewHolder> paginationAdapter;
    private RecyclerView usersRecyclerView;
    private Button createChatButton;
    private SearchView searchView;

    PagedList.Config config = new PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPrefetchDistance(3)
            .setPageSize(10)
            .build();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        inflatedView = inflater.inflate(R.layout.fragment_create_chat, container, false);
        return inflatedView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        model = new ViewModelProvider(requireActivity()).get(MainActivityViewModel.class);

        if(model.getCurrentActionType() == ActionType.createChat) {
            usersCheckedMap.put(model.getCurrentUser().getEmail(), true);
        }
        if(model.getCurrentActionType() == ActionType.inviteParticipants){
            for(int i=0; i<model.getCurrentChat().getUsersEmails().size(); i++){
                usersCheckedMap.put(model.getCurrentChat().getUsersEmails().get(i), true);
            }
        }

        usersRecyclerView = inflatedView.findViewById(R.id.usersRecyclerView);
        createChatButton = inflatedView.findViewById(R.id.createChatButton);

        if(model.getCurrentActionType() == ActionType.createChat) {
            recyclerViewAdapter = new UsersPaginationRecyclerViewAdapter(requireContext(), usersCheckedMap, model.getCurrentUser().getEmail(), new ArrayList<>());
        }
        if(model.getCurrentActionType() == ActionType.inviteParticipants){
            recyclerViewAdapter = new UsersPaginationRecyclerViewAdapter(requireContext(), usersCheckedMap, model.getCurrentUser().getEmail(), model.getCurrentChat().getUsersEmails());
        }
        paginationAdapter = recyclerViewAdapter.getAdapter();
        recyclerViewAdapter.setUsersCheckedMap(usersCheckedMap);

        usersRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        usersRecyclerView.setAdapter(paginationAdapter);

        createChatButton.setOnClickListener(view1 -> {
            usersCheckedMap = recyclerViewAdapter.getUsersCheckedMap();
            if (usersCheckedMap.size() < 2) {
                Toast.makeText(requireContext(), getString(R.string.users_cant_be_less_than_2), Toast.LENGTH_SHORT).show();
                return;
            }

            if(model.getCurrentActionType() == ActionType.createChat) {
                ProgressDialog pd = new ProgressDialog(getParentFragment().getContext());
                pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                pd.setCancelable(false);
                pd.setMessage("Loading...");
                pd.show();

                model.createChat(usersCheckedMap, requireContext()).observe(getViewLifecycleOwner(), chat -> {
                    if (chat != null) {
                        model.setCurrentChat(chat);
                        pd.dismiss();
                        Navigation.findNavController(inflatedView).navigate(R.id.action_createChatFragment_to_chatFragment);
                    }
                });
            }
            if(model.getCurrentActionType() == ActionType.inviteParticipants) {
                model.inviteParticipants(recyclerViewAdapter.getNewChatUsers());
                Navigation.findNavController(inflatedView).navigate(R.id.action_createChatFragment_to_chatFragment);
            }

        });

        searchView = inflatedView.findViewById(R.id.searchUserSearchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                FirestorePagingOptions options = new FirestorePagingOptions.Builder<User>()
                        .setLifecycleOwner((LifecycleOwner) requireContext())
                        .setQuery(FirebaseFirestore.getInstance().collection("user").orderBy("name").startAt(query).endAt(query + '\uf8ff'), config, User.class)
                        .build();
                paginationAdapter.updateOptions(options);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    FirestorePagingOptions options = new FirestorePagingOptions.Builder<User>()
                            .setLifecycleOwner((LifecycleOwner) requireContext())
                            .setQuery(FirebaseFirestore.getInstance().collection("user").orderBy("name"), config, User.class)
                            .build();
                    paginationAdapter.updateOptions(options);
                }
                return false;
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        paginationAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        paginationAdapter.stopListening();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (model.getCurrentUserId() != null) {
            model.changeOnlineStatus(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (model.getCurrentUserId() != null) {
            model.changeOnlineStatus(true);
        }
    }
}