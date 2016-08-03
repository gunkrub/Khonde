package com.tny.khonde;

/**
 * Created by Thitiphat on 9/17/2015.
 */
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cengalabs.flatui.views.FlatButton;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

/**
 * Created by hp1 on 21-01-2015.
 */
public class Tab3 extends Fragment {

    JobPagerAdapter adapter;
    static JobViewPager pager;
    boolean isLoading = false;
    private int totalItem=0;
    SwipeRefreshLayout swipeContainer;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v =inflater.inflate(R.layout.tab_3,container,false);

        TabLayout tabLayout = (TabLayout) v.findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("ดำเนินการอยู่"));
        tabLayout.addTab(tabLayout.newTab().setText("เสร็จสิ้น"));
        tabLayout.addTab(tabLayout.newTab().setText("รายการโปรด"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        pager = (JobViewPager) v.findViewById(R.id.pager);
        //viewPager.setPagingEnabled(false);
        pager.setOffscreenPageLimit(3);
        adapter = new JobPagerAdapter(getChildFragmentManager(), tabLayout.getTabCount());
        pager.setAdapter(adapter);
        pager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setSelectedTabIndicatorColor(Color.parseColor("#FFFFFF"));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                pager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });



        return v;
    }

    public static void getJobDetails(String job_code,Context context){
        DBHelper mydb = new DBHelper(context);
        Job new_job = mydb.getJob(job_code);
        MainActivity.job = new_job;
    }

    public static void inflateJob(int type,String job_code,View v, final Context context){
        final DBHelper mydb = new DBHelper(context);
        DecimalFormat formatter = new DecimalFormat("#,###,###");
        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yy kk:mm");

        LinearLayout layout = (LinearLayout) v.findViewById(R.id.jobs_layout);

        final Job job = mydb.getJob(job_code);
        v.findViewById(R.id.tv_job_not_found).setVisibility(View.GONE);

        View v1 =  View.inflate(context, R.layout.job_card, null);
        ((TextView)v1.findViewById(R.id.tv_job_code)).setText(job.job_code);
        ((TextView)v1.findViewById(R.id.tv_final_price)).setText(""+formatter.format(job.price));
        Date job_dt = new Date();
        job_dt.setTime((long)job.datetime * (long)1000);

        ((TextView)v1.findViewById(R.id.tv_datetime)).setText(""+dateFormatter.format(job_dt));

        final ArrayList<JobLocation> jobLocations = job.jobLocations;
        JobLocation starting = jobLocations.get(0);
        ((TextView)v1.findViewById(R.id.tv_starting_desc)).setText(starting.sub1 + ", " + starting.province);

        LinearLayout layout2 = (LinearLayout) v1.findViewById(R.id.stop_bys);

        for(int j=1;j<jobLocations.size()-1;j++){
            View v2 = View.inflate(context,R.layout.location_layout,null);
            v2.findViewById(R.id.remove_button).setVisibility(View.INVISIBLE);
            v2.findViewById(R.id.stop_by_desc).setVisibility(View.INVISIBLE);
            ((TextView)v2.findViewById(R.id.tv_stop_by_desc)).setText(jobLocations.get(j).sub1 + ", " + jobLocations.get(j).province);

            layout2.addView(v2);
        }

        JobLocation dest = jobLocations.get(jobLocations.size()-1);
        ((TextView)v1.findViewById(R.id.tv_destination_desc)).setText(dest.sub1 + ", " + dest.province);

        v1.findViewById(R.id.rebookButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getJobDetails(job.job_code,context);
                MainActivity.showSelectTimeDialog(context);
            }
        });

        if(type==1) {

            v1.findViewById(R.id.trackLocationButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, TrackerActivity.class);
                    intent.putExtra("job_code", job.job_code);
                    context.startActivity(intent);
                }
            });
        } else {
            v1.findViewById(R.id.trackLocationButton).setVisibility(View.GONE);
        }

        if(type!=3) {
            v1.findViewById(R.id.addtofavButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FavoriteJobFragment fav1 = (FavoriteJobFragment) Tab3.pager.getAdapter().instantiateItem(Tab3.pager, 2);
                    fav1.addToFav(job.job_code);
                }
            });
        } else {
            ((FlatButton)v1.findViewById(R.id.addtofavButton)).setText("ลบออกจากรายการโปรด");
            v1.findViewById(R.id.addtofavButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FavoriteJobFragment fav1 = (FavoriteJobFragment) Tab3.pager.getAdapter().instantiateItem(Tab3.pager, 2);
                    fav1.removeFromFav(job.job_code);
                }
            });
        }

        layout.addView(v1,0);

    }

    public static void inflateJobs(int type, View v, final Context context){
        final DBHelper mydb = new DBHelper(context);

        DecimalFormat formatter = new DecimalFormat("#,###,###");
        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yy kk:mm");

        LinearLayout layout = (LinearLayout) v.findViewById(R.id.jobs_layout);

        final ArrayList<Job> jobs = mydb.getJobs(type);

        if(jobs.size()>0)
            v.findViewById(R.id.tv_job_not_found).setVisibility(View.GONE);

        for(int i=0;i<jobs.size();i++) {
            View v1 =  View.inflate(context, R.layout.job_card, null);
            ((TextView)v1.findViewById(R.id.tv_job_code)).setText(jobs.get(i).job_code);
            ((TextView)v1.findViewById(R.id.tv_final_price)).setText(""+formatter.format(jobs.get(i).price));
            Date job_dt = new Date();
            job_dt.setTime((long)jobs.get(i).datetime * (long)1000);

            ((TextView)v1.findViewById(R.id.tv_datetime)).setText(""+dateFormatter.format(job_dt));

            final ArrayList<JobLocation> jobLocations = jobs.get(i).jobLocations;
            JobLocation starting = jobLocations.get(0);
            ((TextView)v1.findViewById(R.id.tv_starting_desc)).setText(starting.sub1 + ", " + starting.province);

            LinearLayout layout2 = (LinearLayout) v1.findViewById(R.id.stop_bys);

            for(int j=1;j<jobLocations.size()-1;j++){
                View v2 = View.inflate(context,R.layout.location_layout,null);
                v2.findViewById(R.id.remove_button).setVisibility(View.INVISIBLE);
                v2.findViewById(R.id.stop_by_desc).setVisibility(View.INVISIBLE);
                ((TextView)v2.findViewById(R.id.tv_stop_by_desc)).setText(jobLocations.get(j).sub1 + ", " + jobLocations.get(j).province);

                layout2.addView(v2);
            }

            JobLocation dest = jobLocations.get(jobLocations.size()-1);
            ((TextView)v1.findViewById(R.id.tv_destination_desc)).setText(dest.sub1 + ", " + dest.province);
            final int a = i;

            v1.findViewById(R.id.rebookButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getJobDetails(jobs.get(a).job_code,context);
                    MainActivity.loadFromDb = true;
                    MainActivity.pager.setCurrentItem(0,true);
                }
            });

            if(type==1) {

                v1.findViewById(R.id.trackLocationButton).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, TrackerActivity.class);
                        intent.putExtra("job_code", jobs.get(a).job_code);
                        context.startActivity(intent);
                    }
                });
            } else {
                v1.findViewById(R.id.trackLocationButton).setVisibility(View.GONE);
            }

            if(type!=3) {
                v1.findViewById(R.id.addtofavButton).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FavoriteJobFragment fav1 = (FavoriteJobFragment) Tab3.pager.getAdapter().instantiateItem(Tab3.pager, 2);
                        fav1.addToFav(jobs.get(a).job_code);
                    }
                });
            } else {
                ((FlatButton)v1.findViewById(R.id.addtofavButton)).setText("ลบออกจากรายการโปรด");
                v1.findViewById(R.id.addtofavButton).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FavoriteJobFragment fav1 = (FavoriteJobFragment) Tab3.pager.getAdapter().instantiateItem(Tab3.pager, 2);
                        fav1.removeFromFav(jobs.get(a).job_code);
                    }
                });
            }

            layout.addView(v1);
        }
    }

}
