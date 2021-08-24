package com.example.messengerbyesya.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.example.messengerbyesya.R;
import com.example.messengerbyesya.ViewPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;

public class AuthentificationFragment extends Fragment {

    public AuthentificationFragment() {
        // Required empty public constructor
    }

    public static AuthentificationFragment newInstance() {
        return new AuthentificationFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private View inflatedView;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private String[] tabNames = {"Регистрация", "Вход"};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        inflatedView = inflater.inflate(R.layout.fragment_authentification, container, false);
        // Inflate the layout for this fragment
        return inflatedView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            Navigation.findNavController(inflatedView).navigate(R.id.action_authentificationFragment_to_chatFragment);
        }

        viewPager = inflatedView.findViewById(R.id.viewPager);
        viewPager.setAdapter(new ViewPagerAdapter(getChildFragmentManager(), getLifecycle(), 2));

        tabLayout = inflatedView.findViewById(R.id.tabLayout);
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> tab.setText(tabNames[position])).attach();
    }
}