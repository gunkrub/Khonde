package com.tny.khonde;

import com.google.android.gms.maps.model.Marker;

/**
 * Created by Thitiphat on 5/22/2016.
 */
public class JobLocation {
    String desc,province,sub1,sub2;
    double lat;
    double lng;
    Marker marker = null;
    String contact_name="";
    String contact_tel="";
    public JobLocation(String desc, double lat,double lng,String contact_name, String contact_tel){
        this(desc,lat,lng);
        this.contact_name = contact_name;
        this.contact_tel = contact_tel;
    }

    public JobLocation(){
    }

    public JobLocation(String desc, double lat,double lng){
        this.desc = desc;
        this.lat = lat;
        this.lng = lng;
    }
}
