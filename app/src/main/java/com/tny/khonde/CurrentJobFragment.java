package com.tny.khonde;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class CurrentJobFragment extends Fragment {
    static int start_id = 0;
    View view1;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        start_id = 0;
        View v = inflater.inflate(R.layout.fragment_current_job, container, false);
        Tab3.inflateJobs(1,v,getContext());
        final View v2 = v;
        view1 = v;

        v.findViewById(R.id.scrollView3).getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                View jl = v2.findViewById(R.id.jobs_layout);
                View sv = v2.findViewById(R.id.scrollView3);

                if(jl.getHeight()-sv.getScrollY()<2000 && ((LinearLayout)jl).getChildCount()==start_id) {
                    Tab3.inflateJobs(1,v2,getContext());
                }
            }
        });
        return v;
    }

    public void refreshTab(){
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.detach(this).attach(this).commitAllowingStateLoss();
    }
}
