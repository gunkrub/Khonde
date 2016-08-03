package com.tny.khonde;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by Thitiphat on 5/15/2016.
 */
public class JobPagerAdapter extends FragmentStatePagerAdapter {
    int NumbOfTabs; // Store the number of tabs, this will also be passed when the ViewPagerAdapter is created
    //Tab1 tab1 = new Tab1();
    CurrentJobFragment tab1 = new CurrentJobFragment();
    FinishedJobFragment tab2 = new FinishedJobFragment();
    FavoriteJobFragment tab3 = new FavoriteJobFragment();

    // Build a Constructor and assign the passed Values to appropriate values in the class
    public JobPagerAdapter(FragmentManager fm, int mNumbOfTabsumb) {
        super(fm);
        this.NumbOfTabs = mNumbOfTabsumb;
    }

    //This method return the fragment for the every position in the View Pager
    @Override
    public Fragment getItem(int position) {
        switch(position){
            case 0:
                return tab1;
            case 1:
                return tab2;
            case 2:
                return tab3;
            default:
                return tab1;
        }
    }

    // This method return the Number of tabs for the tabs Strip

    @Override
    public int getCount() {
        return NumbOfTabs;
    }


}