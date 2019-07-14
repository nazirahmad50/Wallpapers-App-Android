package com.androidhive.ultrahdwallpapers.Adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.androidhive.ultrahdwallpapers.Fragments.CategoryFragment;
import com.androidhive.ultrahdwallpapers.Fragments.TrendingFragment;
import com.androidhive.ultrahdwallpapers.Fragments.RecentsFragment;

public class MyFragmentAdapter extends FragmentPagerAdapter {


    private final Context context;

    public MyFragmentAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:

                return new CategoryFragment();

            case 1:

                return new RecentsFragment();

            case 2:

                return new TrendingFragment();

            default:
                return null;
        }

    }

    @Override
    public int getCount() {
        return 3;
    }


 //Sets the names for the tabs
    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {

        switch (position){
            case 0:
                return "CATEGORY";
            case 1:
                return "RECENTS";

            case 2:
                 return "TRENDING";
        }

        return "";

    }
}
