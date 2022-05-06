package com.example.messengerbyesya.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messengerbyesya.R;
import com.example.messengerbyesya.model.User;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class UsersPaginationRecyclerViewAdapter {

    private final FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    private final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private final StorageReference firebaseStorageRef = firebaseStorage.getReferenceFromUrl("gs://messenger-by-esya.appspot.com/");

    private Map<String, Boolean> usersCheckedMap = new HashMap<>();
    private Context context;
    private String currentUserEmail;

    public UsersPaginationRecyclerViewAdapter(Context context, Map<String, Boolean> usersCheckedMap, String currentUserEmail) {
        this.context = context;
        this.usersCheckedMap = usersCheckedMap;
        this.currentUserEmail = currentUserEmail;
    }

    PagedList.Config config = new PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPrefetchDistance(3)
            .setPageSize(10)
            .build();

    FirestorePagingOptions options = new FirestorePagingOptions.Builder<User>()
            .setLifecycleOwner((LifecycleOwner) context)
            .setQuery(firebaseFirestore.collection("user").orderBy("name"), config, User.class)
            .build();

    public FirestorePagingAdapter<User, UsersViewHolder> getAdapter() {
        return new FirestorePagingAdapter<User, UsersViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull UsersViewHolder holder, int position, @NonNull User user) {
                holder.userEmailTextView.setText(user.getEmail());
                holder.userNameTextView.setText(user.getName());
                if (usersCheckedMap.get(user.getEmail()) != null) {
                    holder.isUserCheckedCheckBox.setChecked(usersCheckedMap.get(user.getEmail()));
                } else {
                    holder.isUserCheckedCheckBox.setChecked(false);
                }
                firebaseStorageRef.child("avatars/" + user.getAvatar())
                        .getDownloadUrl().addOnSuccessListener(uri -> Picasso.with(holder.userAvatarImageView.getContext())
                        .load(uri)
                        .fit()
                        .centerCrop()
                        .into(holder.userAvatarImageView));

            }

            @NonNull
            @Override
            public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_recycler_view_item, parent, false);
                return new UsersViewHolder(view);
            }
        };
    }

    public class UsersViewHolder extends RecyclerView.ViewHolder {
        TextView userEmailTextView;
        TextView userNameTextView;
        CheckBox isUserCheckedCheckBox;
        ImageView userAvatarImageView;

        UsersViewHolder(View view) {
            super(view);
            userEmailTextView = view.findViewById(R.id.userEmailTextView);
            userNameTextView = view.findViewById(R.id.userNameTextView);
            isUserCheckedCheckBox = view.findViewById(R.id.isUserCheckedCheckBox);
            userAvatarImageView = view.findViewById(R.id.userAvatarImageView);

            isUserCheckedCheckBox.setOnCheckedChangeListener((compoundButton, b) -> {
                if ((userEmailTextView.getText().toString().equals(currentUserEmail)) && (!b)) {
                    Toast.makeText(context, context.getString(R.string.you_cant_not_be_a_chat_participant), Toast.LENGTH_SHORT).show();
                    isUserCheckedCheckBox.setChecked(true);
                    return;
                }
                usersCheckedMap.put(userEmailTextView.getText().toString(), b);
            });
        }

    }

    public Map<String, Boolean> getUsersCheckedMap() {
        return usersCheckedMap;
    }

    public void setUsersCheckedMap(Map<String, Boolean> usersCheckedMap) {
        this.usersCheckedMap = usersCheckedMap;
    }
}
