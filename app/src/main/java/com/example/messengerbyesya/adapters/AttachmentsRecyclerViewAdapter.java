package com.example.messengerbyesya.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messengerbyesya.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class AttachmentsRecyclerViewAdapter extends RecyclerView.Adapter<AttachmentsRecyclerViewAdapter.ViewHolder> {

    private List<String> attachmentsUrlStringList = new ArrayList<>();

    public AttachmentsRecyclerViewAdapter(List<String> attachmentsUrlStringList) {
        this.attachmentsUrlStringList = attachmentsUrlStringList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.attachments_recycler_view_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Picasso.with(holder.attachmentImageView.getContext()).load(attachmentsUrlStringList.get(position)).into(holder.attachmentImageView);
    }

    @Override
    public int getItemCount() {
        return attachmentsUrlStringList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView attachmentImageView;

        ViewHolder(View view){
            super(view);

            attachmentImageView = view.findViewById(R.id.attachmentImageView);

        }

    }
}
