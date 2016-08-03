package com.tny.khonde;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.android.gms.maps.model.Marker;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Thitiphat on 5/17/2016.
 */
public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "Jobs.db";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, 18);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(
                "create table job (id integer primary key, job_code text,datetime integer,remark text, price integer, status integer,infav integer,truck_type text, roof_type text,trunk_width text,extra text,truck_count text,helper_count text,gotDriverInfo integer,commented integer)"
        );
        db.execSQL(
                "create table job_location (id integer primary key, job_id integer, desc1 text, province text, sub1 text, sub2 text, lat real, long real,contact_name text, contact_tel text)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS job");
        db.execSQL("DROP TABLE IF EXISTS job_location");

        onCreate(db);
    }

    public boolean insertJob(Job job) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("job_code",job.job_code);
        contentValues.put("datetime",job.datetime);
        contentValues.put("price",job.price);
        contentValues.put("remark",job.remark);
        contentValues.put("status",-1);
        contentValues.put("infav",0);
        contentValues.put("gotDriverInfo",0);
        contentValues.put("commented",0);

        contentValues.put("truck_type",job.truck_type);
        contentValues.put("roof_type",job.roof_type);
        contentValues.put("trunk_width",job.trunk_width);
        contentValues.put("extra",job.extra);
        contentValues.put("truck_count",job.truck_count);
        contentValues.put("helper_count",job.helper_count);

        long job_id = db.insert("job",null,contentValues);

        for(int i=0; i<job.jobLocations.size();i++){
            ContentValues contentValues1 = new ContentValues();
            contentValues1.put("job_id",job_id);
            contentValues1.put("desc1",job.jobLocations.get(i).desc);
            contentValues1.put("lat",job.jobLocations.get(i).lat);
            contentValues1.put("long",job.jobLocations.get(i).lng);
            contentValues1.put("province",job.jobLocations.get(i).province);
            contentValues1.put("sub1",job.jobLocations.get(i).sub1);
            contentValues1.put("contact_name",job.jobLocations.get(i).contact_name);
            contentValues1.put("contact_tel",job.jobLocations.get(i).contact_tel);
            db.insert("job_location",null,contentValues1);
        }

        db.close();
        return true;
    }

    public ArrayList<Job> getJobs(int tab){
        ArrayList<Job> jobs = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = null;
        switch (tab){
            case 1:
                res =  db.rawQuery( "select * from job where status='0' order by datetime asc limit " + CurrentJobFragment.start_id + ",10", null );
                CurrentJobFragment.start_id += 10;
                break;
            case 2:
                res =  db.rawQuery( "select * from job where status='1' order by datetime desc limit " + FinishedJobFragment.start_id + ",10", null );
                FinishedJobFragment.start_id += 10;
                break;
            case 3:
                res =  db.rawQuery( "select * from job where infav='1' order by datetime desc", null );
                break;
        }
        res.moveToFirst();

        while(res.isAfterLast() == false){
            int job_id = res.getInt(res.getColumnIndex("id"));
            ArrayList<JobLocation> jobLocations = new ArrayList<>();

            Cursor res2 = db.rawQuery("select * from job_location where job_id=" + job_id,null);
            res2.moveToFirst();

            while(res2.isAfterLast()==false){
                String desc = res2.getString(res2.getColumnIndex("desc1"));
                double lat = res2.getDouble(res2.getColumnIndex("lat"));
                double lng = res2.getDouble(res2.getColumnIndex("long"));
                String contact_name = res2.getString(res2.getColumnIndex("contact_name"));
                String contact_tel = res2.getString(res2.getColumnIndex("contact_tel"));
                String province = res2.getString(res2.getColumnIndex("province"));
                String sub1 = res2.getString(res2.getColumnIndex("sub1"));

                JobLocation jobLocation = new JobLocation(desc,lat,lng,contact_name,contact_tel);
                jobLocation.province = province;
                jobLocation.sub1 = sub1;
                jobLocations.add(jobLocation);
                res2.moveToNext();
            }
            String job_code = res.getString(res.getColumnIndex("job_code"));
            int datetime = res.getInt(res.getColumnIndex("datetime"));
            int price = res.getInt(res.getColumnIndex("price"));
            String remark = res.getString(res.getColumnIndex("remark"));

            Job new_job = new Job(jobLocations,job_code,datetime,price);
            new_job.remark = remark;
            new_job.truck_type = res.getString(res.getColumnIndex("truck_type"));
            new_job.roof_type = res.getString(res.getColumnIndex("roof_type"));
            new_job.trunk_width = res.getString(res.getColumnIndex("trunk_width"));
            new_job.extra = res.getString(res.getColumnIndex("extra"));
            new_job.truck_count = res.getString(res.getColumnIndex("truck_count"));
            new_job.helper_count = res.getString(res.getColumnIndex("helper_count"));

            jobs.add(new_job);
            res2.close();
            res.moveToNext();
        }
        res.close();

        db.close();
        return jobs;
    }

    public Job getJob(String job_code){
        Job job = new Job();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from job where job_code='" + job_code + "'", null );

        res.moveToFirst();

        int job_id = res.getInt(res.getColumnIndex("id"));
        ArrayList<JobLocation> jobLocations = new ArrayList<>();

        Cursor res2 = db.rawQuery("select * from job_location where job_id=" + job_id,null);
        res2.moveToFirst();

        while(res2.isAfterLast()==false){
            String desc = res2.getString(res2.getColumnIndex("desc1"));
            double lat = res2.getDouble(res2.getColumnIndex("lat"));
            double lng = res2.getDouble(res2.getColumnIndex("long"));

            String contact_name = res2.getString(res2.getColumnIndex("contact_name"));
            String contact_tel = res2.getString(res2.getColumnIndex("contact_tel"));

            String province = res2.getString(res2.getColumnIndex("province"));
            String sub1 = res2.getString(res2.getColumnIndex("sub1"));

            JobLocation jobLocation = new JobLocation(desc,lat,lng,contact_name,contact_tel);
            jobLocation.province = province;
            jobLocation.sub1 = sub1;
            jobLocations.add(jobLocation);
            res2.moveToNext();
        }

        int datetime = res.getInt(res.getColumnIndex("datetime"));
        int price = res.getInt(res.getColumnIndex("price"));
        String remark = res.getString(res.getColumnIndex("remark"));

        Job new_job = new Job(jobLocations,job_code,datetime,price);
        new_job.remark = remark;
        new_job.truck_type = res.getString(res.getColumnIndex("truck_type"));
        new_job.roof_type = res.getString(res.getColumnIndex("roof_type"));
        new_job.trunk_width = res.getString(res.getColumnIndex("trunk_width"));
        new_job.extra = res.getString(res.getColumnIndex("extra"));
        new_job.truck_count = res.getString(res.getColumnIndex("truck_count"));
        new_job.helper_count = res.getString(res.getColumnIndex("helper_count"));
        job = new_job;
        res.close();
        res2.close();
        db.close();
        return job;
    }

    public boolean addToFav(String job_code){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("infav",1);

        db.update("job", contentValues, "job_code='"+job_code+"'", null);
        db.close();
        return true;
    }

    public boolean gotDriverInfo(String job_code){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("gotDriverInfo",1);

        db.update("job", contentValues, "job_code='"+job_code+"'", null);
        db.close();
        return true;
    }

    public ArrayList<String> driverInfoNotPushed(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = null;

        ArrayList<String> return1 = new ArrayList<>();

        res =  db.rawQuery( "select * from job where gotDriverInfo='0' AND status='-1'", null );
        res.moveToFirst();

        while(res.isAfterLast() == false) {
            return1.add(res.getString(res.getColumnIndex("job_code")));
            res.moveToNext();
        }
        res.close();
        db.close();
        return return1;
    }

    public ArrayList<String> commentNeeded(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = null;

        ArrayList<String> return1 = new ArrayList<>();

        res =  db.rawQuery( "select * from job where commented='0' AND status='1'", null );
        res.moveToFirst();

        while(res.isAfterLast() == false) {
            return1.add(res.getString(res.getColumnIndex("job_code")));
            res.moveToNext();
        }
        res.close();
        db.close();
        return return1;
    }

    public boolean updatePrice(String job_code,int price){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("price",price);

        db.update("job", contentValues, "job_code='"+job_code+"'", null);
        db.close();
        return true;
    }


    public boolean updateStatus(ArrayList<String> job_codes, int status){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("status",status);

        for(int i=0;i<job_codes.size();i++)
            db.update("job", contentValues, "job_code='"+job_codes.get(i)+"'", null);

        db.close();
        return true;
    }

    public boolean removeFromFav(String job_code){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("infav",0);

        db.update("job", contentValues, "job_code='"+job_code+"'", null);
        db.close();
        return true;
    }

    public boolean cancelJob(ArrayList<String> job_codes){
        SQLiteDatabase db = this.getWritableDatabase();

        for(int i=0;i<job_codes.size();i++)
            db.delete("job","job_code='" + job_codes.get(i)+ "'",null);

        db.close();
        return true;
    }

    public boolean doneComment(String job_code){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("commented",1);

        db.update("job", contentValues, "job_code='"+job_code+"'", null);
        db.close();
        return true;
    }

    //Exampless

    public Cursor getData(int id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from contacts where id="+id+"", null );
        return res;
    }

    public int numberOfRows(){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, "job");
        return numRows;
    }

    public Integer deleteContact (Integer id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("contacts",
                "id = ? ",
                new String[] { Integer.toString(id) });
    }
}
