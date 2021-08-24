package com.example.messengerbyesya;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.messengerbyesya.fragments.AuthentificationFragment;
import com.example.messengerbyesya.fragments.LogInFragment;
import com.example.messengerbyesya.fragments.RegistrationFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {

    private final int countOfCategories;
    public ViewPagerAdapter(FragmentManager fragmentManager, Lifecycle lifecycle, int countOfCategories) {
        super(fragmentManager, lifecycle);
        this.countOfCategories = countOfCategories;
    }

    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0:
                return RegistrationFragment.newInstance();
            case 1:
                return LogInFragment.newInstance();
            default:
                return null;
        }
    }

    @Override
    public int getItemCount() {
        return countOfCategories;
    }
}
