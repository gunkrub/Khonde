package com.tny.khonde;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cengalabs.flatui.views.FlatButton;

import java.text.DecimalFormat;
import java.util.ArrayList;


public class FavoriteJobFragment extends Fragment {
    DBHelper mydb;
    View v;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_favorite_job, container, false);

        mydb = new DBHelper(getContext());
        Tab3.inflateJobs(3,v,getContext());

        return v;
    }

    public void refreshTab(){
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.detach(this).attach(this).commitAllowingStateLoss();
    }

    public void addToFav(String job_code){
        mydb.addToFav(job_code);
        refreshTab();
    }

    public void removeFromFav(String job_code){
        mydb.removeFromFav(job_code);
        refreshTab();
    }

}
