package com.example.messengerbyesya.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messengerbyesya.R;
import com.example.messengerbyesya.model.Chat;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class ChatsRecyclerViewAdapter {

    private Date tempDate;
    private final Calendar dateNowCalendar = new GregorianCalendar();
    private String tempEmail;
    private Timestamp timestamp;
    private final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private final FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    private final StorageReference firebaseStorageRef = firebaseStorage.getReferenceFromUrl("gs://messenger-by-esya.appspot.com/");

    public FirestoreRecyclerAdapter getAdapter(FirestoreRecyclerAdapter adapter, String currentUserEmail, ItemClickListener itemClickListener) {
        Query query = firebaseFirestore.collection("chats").orderBy("date_of_last_message"); //TODO: Добавить фильтрацию по почте текущего пользователя (чтобы чужие чаты не отображались)

        FirestoreRecyclerOptions<Chat> options = new FirestoreRecyclerOptions.Builder<Chat>().setQuery(query, snapshot -> {
            Chat tempChat = new Chat();
            timestamp = (Timestamp) snapshot.get("date_of_last_message");
            tempChat.setDateOfLastMessage(timestamp.toDate());
            tempChat.setUsersEmails((ArrayList<String>) snapshot.get("users_emails"));
            tempChat.setChatId(snapshot.getId());
            return tempChat;
        }).build();

        adapter = new FirestoreRecyclerAdapter<Chat, RecyclerView.ViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull Chat chat) {
                onBindViewHolderChat((ChatViewHolder) holder, chat);
            }

            private void onBindViewHolderChat(ChatViewHolder holder, Chat chat) {
                tempDate = new Date();
                tempDate.setTime(chat.getDateOfLastMessage().getTime());

                holder.itemView.setOnClickListener(v -> itemClickListener.onItemClick(holder.getAdapterPosition()));

                dateNowCalendar.setTimeInMillis(new Date().getTime());
                dateNowCalendar.set(Calendar.HOUR, 0);
                dateNowCalendar.set(Calendar.MINUTE, 0);
                dateNowCalendar.set(Calendar.SECOND, 0);

                if (dateNowCalendar.getTime().before(tempDate)) {
                    holder.timeOfLastMessageTextView.setText(new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH).format(tempDate));
                } else {
                    holder.timeOfLastMessageTextView.setText(new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH).format(tempDate));
                }

                if(chat.getUsersEmails().size()==2) {
                    if (chat.getUsersEmails().get(0).equals(currentUserEmail)) {
                        tempEmail = chat.getUsersEmails().get(1);
                    } else {
                        tempEmail = chat.getUsersEmails().get(0);
                    }
                } else {
                    //TODO: Добавить код для обозначения беседы из нескольких людей
                }

                firebaseFirestore.collection("user").whereEqualTo("email", tempEmail).get().addOnSuccessListener(queryDocumentSnapshots -> {
                    User tempUser = new User();
                    tempUser = queryDocumentSnapshots.getDocuments().get(0).toObject(User.class);
                    holder.chatNameTextView.setText(tempUser.getName());
                    firebaseStorageRef.child(tempUser.getAvatar()).getDownloadUrl().addOnSuccessListener(uri -> Picasso.with(holder.chatAvatarImageView.getContext()).load(uri).resize(50, 50).centerCrop().into(holder.chatAvatarImageView));
                });

            }


            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup group, int viewType) {
                View view = LayoutInflater.from(group.getContext()).inflate(R.layout.chat_recycler_view_item, group, false);
                return new ChatsRecyclerViewAdapter.ChatViewHolder(view);
            }
        };
        return adapter;
    }

    public interface ItemClickListener{
        public void onItemClick(int position);
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView chatNameTextView;
        TextView timeOfLastMessageTextView;
        ImageView chatAvatarImageView;

        ChatViewHolder(View view) {
            super(view);
            chatNameTextView = view.findViewById(R.id.chatNameTextView);
            timeOfLastMessageTextView = view.findViewById(R.id.timeOfLastMessageTextView);
            chatAvatarImageView = view.findViewById(R.id.chatAvatarImageView);
        }

    }

}
