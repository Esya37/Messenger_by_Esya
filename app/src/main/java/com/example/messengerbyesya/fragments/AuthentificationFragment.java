package com.example.messengerbyesya.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.viewpager2.widget.ViewPager2;

import com.example.messengerbyesya.MainActivityViewModel;
import com.example.messengerbyesya.R;
import com.example.messengerbyesya.adapters.ViewPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

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
    private final String[] tabNames = {"Регистрация", "Вход"};
    private MainActivityViewModel model;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        inflatedView = inflater.inflate(R.layout.fragment_authentification, container, false);
        // Inflate the layout for this fragment
        return inflatedView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        model = new ViewModelProvider(getActivity()).get(MainActivityViewModel.class);

        if (model.getCurrentFirebaseUser() != null) {
            Navigation.findNavController(inflatedView).navigate(R.id.action_authentificationFragment_to_selectChatFragment);
        }

        viewPager = inflatedView.findViewById(R.id.viewPager);
        viewPager.setAdapter(new ViewPagerAdapter(getChildFragmentManager(), getLifecycle(), 2));

        tabLayout = inflatedView.findViewById(R.id.tabLayout);
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> tab.setText(tabNames[position])).attach();
    }
}