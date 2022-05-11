package com.example.messengerbyesya.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messengerbyesya.MainActivityViewModel;
import com.example.messengerbyesya.R;
import com.example.messengerbyesya.adapters.AttachmentsRecyclerViewAdapter;
import com.example.messengerbyesya.adapters.ParticipantsRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;


public class ShowParticipantsOrAttachmentsFragment extends Fragment {

    public enum ShowedItemType {
        participants,
        attachments
    }

    public static ShowParticipantsOrAttachmentsFragment newInstance() {
        return new ShowParticipantsOrAttachmentsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private View inflatedView;
    private MainActivityViewModel model;
    private RecyclerView participantsOrAttachmentsRecyclerView;
    private ParticipantsRecyclerViewAdapter participantsRecyclerViewAdapter;
    private AttachmentsRecyclerViewAdapter attachmentsRecyclerViewAdapter;
    private List<String> attachmentsUrlStringList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        inflatedView = inflater.inflate(R.layout.fragment_show_participants_or_attachments, container, false);
        return inflatedView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        model = new ViewModelProvider(requireActivity()).get(MainActivityViewModel.class);

        participantsOrAttachmentsRecyclerView = inflatedView.findViewById(R.id.participantsRecyclerView);

        if (model.getShowedItemType() == ShowedItemType.participants) {
            participantsRecyclerViewAdapter = new ParticipantsRecyclerViewAdapter(model.getCurrentChat().getUsersEmails());
            participantsOrAttachmentsRecyclerView.setAdapter(participantsRecyclerViewAdapter);
            participantsOrAttachmentsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        }

        if (model.getShowedItemType() == ShowedItemType.attachments) {
            ((TextView) inflatedView.findViewById(R.id.participantsTextView)).setText(R.string.attachments);

            ProgressDialog pd = new ProgressDialog(getParentFragment().getContext());
            pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pd.setCancelable(false);
            pd.setMessage("Loading...");
            pd.show();

            model.getImagesFromFolder("chats_media_resources/" + model.getCurrentChat().getChatId() + "/").addOnSuccessListener(listResult -> {
                if(listResult.getItems().size() == 0){
                    pd.dismiss();
                    inflatedView.findViewById(R.id.attachmentsTextView).setVisibility(View.VISIBLE);
                }
                for (int i = 0; i < listResult.getItems().size(); i++) { //TODO: Получить с сервера content_description
                    listResult.getItems().get(i).getDownloadUrl().addOnSuccessListener((Activity) requireContext(), uri -> {
                        attachmentsUrlStringList.add(uri.toString());
                        if (attachmentsUrlStringList.size() == listResult.getItems().size()) {
                            attachmentsRecyclerViewAdapter = new AttachmentsRecyclerViewAdapter(attachmentsUrlStringList);
                            participantsOrAttachmentsRecyclerView.setAdapter(attachmentsRecyclerViewAdapter);
                            participantsOrAttachmentsRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));

                            pd.dismiss();
                        }
                    });
                }
            });
        }

    }
}