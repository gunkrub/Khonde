package com.tny.khonde;

import java.util.ArrayList;

/**
 * Created by Thitiphat on 5/22/2016.
 */
public class Job {
    ArrayList<JobLocation> jobLocations;
    String job_code,remark="";
    int datetime, price;
    String truck_type,roof_type,trunk_width,extra,truck_count,helper_count;

    public Job(){
        jobLocations = new ArrayList<>();

        //As pickup location
        jobLocations.add(new JobLocation());

        //As Dropoff location
        jobLocations.add(new JobLocation());
    }
    public Job(ArrayList<JobLocation> jobLocations,String job_code,int datetime, int price){
        this.jobLocations = jobLocations;
        this.job_code = job_code;
        this.datetime= datetime;
        this.price = price;
    }

    public void clearJobDetails(){
        price=0;
        truck_type=null;
        job_code=null;
        remark="";

        for(int i=0;i<jobLocations.size();i++){
            jobLocations.get(i).contact_name="";
            jobLocations.get(i).contact_tel="";
        }
    }

    public Job(Job job){
        jobLocations = job.jobLocations;
        remark = job.remark;
        truck_type = job.truck_type;
        roof_type = job.roof_type;
        trunk_width = job.trunk_width;
        extra = job.extra;
        truck_count = job.truck_count;
        helper_count = job.helper_count;
    }
}
