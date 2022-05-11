package com.example.messengerbyesya.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messengerbyesya.R;
import com.example.messengerbyesya.model.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ParticipantsRecyclerViewAdapter extends RecyclerView.Adapter<ParticipantsRecyclerViewAdapter.ViewHolder> {

    private List<String> participantsEmailsList = new ArrayList<>();
    private final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private final FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    private final StorageReference firebaseStorageRef = firebaseStorage.getReferenceFromUrl("gs://messenger-by-esya.appspot.com/");

    public ParticipantsRecyclerViewAdapter(List<String> participantsEmailsList) {
        this.participantsEmailsList = participantsEmailsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.participants_recycler_view_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.participantEmailTextView.setText(participantsEmailsList.get(position));

        firebaseFirestore.collection("user").whereEqualTo("email", participantsEmailsList.get(position)).addSnapshotListener((Activity) holder.participantEmailTextView.getContext(), (value, error) -> {
            if ((value != null) && (!value.isEmpty())) {
                User tempUser = value.getDocuments().get(0).toObject(User.class);

                holder.participantNameTextView.setText(tempUser.getName());
                if (tempUser.isUserOnline()) {
                    holder.participantOnlineMarkerImageView.setVisibility(View.VISIBLE);
                    holder.participantOnlineMarkerImageView.setContentDescription(holder.participantOnlineMarkerImageView.getContext().getString(R.string.user_online));
                } else {
                    holder.participantOnlineMarkerImageView.setVisibility(View.INVISIBLE);
                    holder.participantOnlineMarkerImageView.setContentDescription(holder.participantOnlineMarkerImageView.getContext().getString(R.string.user_offline));
                }
                holder.participantAvatarImageView.setContentDescription("Аватар пользователя " + tempUser.getName());
                firebaseStorageRef.child("avatars/" + tempUser.getAvatar()).getDownloadUrl().addOnSuccessListener(uri -> Picasso.with(holder.participantAvatarImageView.getContext()).load(uri).resize(70, 70).centerCrop().into(holder.participantAvatarImageView));
            }
        });

    }

    @Override
    public int getItemCount() {
        return participantsEmailsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView participantNameTextView;
        TextView participantEmailTextView;
        ImageView participantAvatarImageView;
        ImageView participantOnlineMarkerImageView;

        ViewHolder(View view) {
            super(view);
            participantNameTextView = view.findViewById(R.id.participantNameTextView);
            participantEmailTextView = view.findViewById(R.id.participantEmailTextView);
            participantAvatarImageView = view.findViewById(R.id.participantAvatarImageView);
            participantOnlineMarkerImageView = view.findViewById(R.id.participantOnlineMarkerImageView);

        }
    }
}
