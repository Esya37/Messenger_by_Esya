package com.example.messengerbyesya.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.Barrier;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messengerbyesya.R;
import com.example.messengerbyesya.model.Message;
import com.example.messengerbyesya.model.User;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class MessagesRecyclerViewAdapter {

    private Date tempDate;
    private final Calendar dateNowCalendar = new GregorianCalendar();
    private Timestamp timestamp;
    private final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private final FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    private final StorageReference firebaseStorageRef = firebaseStorage.getReferenceFromUrl("gs://messenger-by-esya.appspot.com/");

    public FirestoreRecyclerAdapter getAdapter(FirestoreRecyclerAdapter adapter, String currentUserEmail, String currentChatId){

        Query query = firebaseFirestore.collection("chats").document(currentChatId).collection("messages").orderBy("date");


        FirestoreRecyclerOptions<Message> options = new FirestoreRecyclerOptions.Builder<Message>().setQuery(query, snapshot -> {
            Message tempMessage = new Message();
            timestamp = (Timestamp) snapshot.get("date");
            tempMessage.setDate(timestamp.toDate());
            tempMessage.setText((String) snapshot.get("text"));
            tempMessage.setMediaResource((Boolean) snapshot.get("is_media_resource"));
            tempMessage.setSenderEmail((String) snapshot.get("sender_email"));
            tempMessage.setContentDescription((String) snapshot.get("content_description"));
            return tempMessage;
        }).build();

        adapter = new FirestoreRecyclerAdapter<Message, RecyclerView.ViewHolder>(options) {

            private static final int MESSAGE_FROM_CURRENT_USER_TYPE = 1;
            private static final int MESSAGE_FROM_OTHER_USERS_TYPE = 2;

            @Override
            protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull Message message) {
                switch (holder.getItemViewType()){
                    case MESSAGE_FROM_CURRENT_USER_TYPE:
                        onBindViewHolderCurrentUser((CurrentUserViewHolder) holder, message);
                        break;
                    case MESSAGE_FROM_OTHER_USERS_TYPE:
                        onBindViewHolderOtherUsers((OtherUsersViewHolder) holder, message);
                        break;
                }

            }

            private void onBindViewHolderOtherUsers(OtherUsersViewHolder holder, Message message) {
                if(message.getMediaResource()){
                    firebaseStorageRef.child("chats_media_resources/" + currentChatId + "/" + message.getText()).getDownloadUrl().addOnSuccessListener(uri -> Picasso.with(holder.messageImageImageView.getContext()).load(uri).into(holder.messageImageImageView));
                    holder.messageImageImageView.setContentDescription(message.getContentDescription());
                    holder.messageTextView.setVisibility(View.GONE);
                    holder.messageImageImageView.setVisibility(View.VISIBLE);

                    ConstraintSet constraintSet = new ConstraintSet();
                    constraintSet.clone(holder.constraintLayout);
                    constraintSet.connect(holder.senderAvatarImageView.getId(), ConstraintSet.BOTTOM, holder.messageImageImageView.getId(), ConstraintSet.BOTTOM);
                    constraintSet.connect(holder.timeTextView.getId(), ConstraintSet.TOP, holder.messageImageImageView.getId(), ConstraintSet.BOTTOM);
                    constraintSet.applyTo(holder.constraintLayout);
                } else {
                    holder.messageTextView.setText(message.getText());
                    holder.messageTextView.setVisibility(View.VISIBLE);
                    holder.messageImageImageView.setVisibility(View.GONE);

                    ConstraintSet constraintSet = new ConstraintSet();
                    constraintSet.clone(holder.constraintLayout);
                    constraintSet.connect(holder.senderAvatarImageView.getId(), ConstraintSet.BOTTOM, holder.messageTextView.getId(), ConstraintSet.BOTTOM);
                    constraintSet.connect(holder.timeTextView.getId(), ConstraintSet.TOP, holder.messageTextView.getId(), ConstraintSet.BOTTOM);
                    constraintSet.applyTo(holder.constraintLayout);
                }

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

                firebaseFirestore.collection("user").whereEqualTo("email", message.getSenderEmail()).get().addOnSuccessListener(queryDocumentSnapshots -> {
                    User tempUser = new User();
                    tempUser = queryDocumentSnapshots.getDocuments().get(0).toObject(User.class);
                    message.setSender(tempUser);
                    holder.senderNameTextView.setText(message.getSender().getName());
                    holder.senderAvatarImageView.setImageDrawable(null);
                    holder.senderAvatarImageView.setContentDescription("Аватар пользователя " + tempUser.getName());
                    firebaseStorageRef.child("avatars/" + message.getSender().getAvatar()).getDownloadUrl().addOnSuccessListener(uri -> Picasso.with(holder.senderAvatarImageView.getContext()).load(uri).fit().centerCrop().into(holder.senderAvatarImageView));
                });
            }

            private void onBindViewHolderCurrentUser(CurrentUserViewHolder holder, Message message) {

                if(message.getMediaResource()){
                    firebaseStorageRef.child("chats_media_resources/" + currentChatId + "/" + message.getText()).getDownloadUrl().addOnSuccessListener(uri -> Picasso.with(holder.messageImageImageView.getContext()).load(uri).into(holder.messageImageImageView));
                    holder.messageImageImageView.setContentDescription(message.getContentDescription());
                    holder.messageTextView.setVisibility(View.GONE);
                    holder.messageImageImageView.setVisibility(View.VISIBLE);
                    holder.barrier.addView(holder.messageImageImageView);

                    ConstraintSet constraintSet = new ConstraintSet();
                    constraintSet.clone(holder.constraintLayout);
                    constraintSet.connect(holder.senderAvatarImageView.getId(), ConstraintSet.BOTTOM, holder.messageImageImageView.getId(), ConstraintSet.BOTTOM);
                    constraintSet.connect(holder.timeTextView.getId(), ConstraintSet.TOP, holder.messageImageImageView.getId(), ConstraintSet.BOTTOM);
                    constraintSet.applyTo(holder.constraintLayout);
                } else {
                    holder.messageTextView.setText(message.getText());
                    holder.messageTextView.setVisibility(View.VISIBLE);
                    holder.messageImageImageView.setVisibility(View.GONE);
                    holder.barrier.removeView(holder.messageImageImageView);

                    ConstraintSet constraintSet = new ConstraintSet();
                    constraintSet.clone(holder.constraintLayout);
                    constraintSet.connect(holder.senderAvatarImageView.getId(), ConstraintSet.BOTTOM, holder.messageTextView.getId(), ConstraintSet.BOTTOM);
                    constraintSet.connect(holder.timeTextView.getId(), ConstraintSet.TOP, holder.messageTextView.getId(), ConstraintSet.BOTTOM);
                    constraintSet.applyTo(holder.constraintLayout);
                }

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

                firebaseFirestore.collection("user").whereEqualTo("email", message.getSenderEmail()).get().addOnSuccessListener(queryDocumentSnapshots -> {
                    User tempUser = new User();
                    tempUser = queryDocumentSnapshots.getDocuments().get(0).toObject(User.class);
                    message.setSender(tempUser);
                    holder.senderNameTextView.setText(message.getSender().getName());
                    holder.senderAvatarImageView.setImageDrawable(null);
                    holder.senderAvatarImageView.setContentDescription("Аватар пользователя " + tempUser.getName());
                    firebaseStorageRef.child("avatars/" + message.getSender().getAvatar()).getDownloadUrl().addOnSuccessListener(uri -> Picasso.with(holder.senderAvatarImageView.getContext()).load(uri).fit().centerCrop().into(holder.senderAvatarImageView));
                });
            }

            @Override
            public int getItemViewType(int position) {
                if (getItem(position).getSenderEmail().equals(currentUserEmail)) {
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
        return adapter;
    }

    public static class OtherUsersViewHolder extends RecyclerView.ViewHolder {
        TextView senderNameTextView;
        TextView messageTextView;
        TextView timeTextView;
        ImageView senderAvatarImageView;
        ImageView messageImageImageView;
        ConstraintLayout constraintLayout;

        OtherUsersViewHolder(View view) {
            super(view);
            senderNameTextView = view.findViewById(R.id.senderNameTextView);
            messageTextView = view.findViewById(R.id.messageTextView);
            timeTextView = view.findViewById(R.id.timeTextView);
            senderAvatarImageView = view.findViewById(R.id.senderAvatarImageView);
            messageImageImageView = view.findViewById(R.id.messageImageImageView);
            constraintLayout = view.findViewById(R.id.chatOtherUsersRecyclerViewItemConstraintLayout);
        }

    }

    public static class CurrentUserViewHolder extends RecyclerView.ViewHolder {
        TextView senderNameTextView;
        TextView messageTextView;
        TextView timeTextView;
        ImageView senderAvatarImageView;
        ImageView messageImageImageView;
        ConstraintLayout constraintLayout;
        Barrier barrier;

        CurrentUserViewHolder(View view) {
            super(view);
            senderNameTextView = view.findViewById(R.id.senderNameTextView2);
            messageTextView = view.findViewById(R.id.messageTextView2);
            timeTextView = view.findViewById(R.id.timeTextView2);
            senderAvatarImageView = view.findViewById(R.id.senderAvatarImageView2);
            messageImageImageView = view.findViewById(R.id.messageImageImageView2);
            constraintLayout = view.findViewById(R.id.chatOtherUsersRecyclerViewItemConstraintLayout2);
            barrier = view.findViewById(R.id.verticalBarrier2);
        }

    }
}
