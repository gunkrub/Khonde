package com.tny.khonde;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.ads.formats.NativeAd;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

public class TrackerActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    String driver_tel = "";
    LatLng truck_location;
    Boolean isVisible = false;
    Marker driver_marker;
    Job job;

    @Override
    public void onPause(){
        super.onPause();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        isVisible=false;
    }

    @Override
    public void onResume(){
        super.onResume();
        isVisible = true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracker);

        DecimalFormat formatter = new DecimalFormat("#,###,###");
        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yy kk:mm");

        DBHelper mydb = new DBHelper(this);
        final Intent intent = getIntent();

        job = mydb.getJob(intent.getStringExtra("job_code"));

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.location_map);
        mapFragment.getMapAsync(this);


        ((TextView)findViewById(R.id.tv_job_code)).setText(intent.getStringExtra("job_code"));
        ((TextView)findViewById(R.id.tv_datetime)).setText(dateFormatter.format((long)job.datetime*1000));
        ((TextView)findViewById(R.id.tv_price)).setText("฿" +formatter.format(job.price));
        ((TextView)findViewById(R.id.tv_from_to)).setText(job.jobLocations.get(0).province + " - " + job.jobLocations.get(job.jobLocations.size()-1).province);


        new AsyncTask<String,Void,Boolean>(){
            String errorMsg="";
            JSONObject return1;
            @Override
            protected Boolean doInBackground(String... params) {
                BufferedReader reader;
                StringBuilder buffer = new StringBuilder();
                String line;

                try {
                    URL u = new URL(getString(R.string.website_url) + "/track_driver.php?job_code=" + intent.getStringExtra("job_code"));
                    HttpURLConnection h = (HttpURLConnection)u.openConnection();
                    h.setRequestMethod("GET");
                    h.setDoOutput(true);
                    h.connect();

                    int response = h.getResponseCode();
                    if (response == 200) {
                        reader = new BufferedReader(new InputStreamReader(h.getInputStream(),"UTF-8"));
                        while((line = reader.readLine()) != null) {
                            buffer.append(line);
                        }

                        return1 = new JSONObject(buffer.toString());
                        Log.e("Return",buffer.toString());
                        return true;
                    }
                    else {
                        errorMsg = "HTTP Error";
                    }
                } catch (MalformedURLException e) {
                    Log.e("UpdateLocTask", "URL Error");
                    errorMsg = "URL Error";
                } catch (IOException e) {
                    Log.e("UpdateLocTask", "No internet connection, Retry soon");
                    MainActivity.makeToast("กรุณาเชื่อมต่ออินเตอร์เน็ต",getApplicationContext());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean result){
                try {
                    driver_tel = return1.getString("driver_tel");

                    findViewById(R.id.image_call).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(Intent.ACTION_DIAL);
                            intent.setData(Uri.parse("tel:" + driver_tel));
                            startActivity(intent);
                        }
                    });

                    findViewById(R.id.image_call).setVisibility(View.VISIBLE);
                    ((TextView)findViewById(R.id.tv_truck_type)).setText(return1.getString("truck_type"));
                    ((TextView)findViewById(R.id.tv_driver_name)).setText(return1.getString("name"));
                    ((TextView)findViewById(R.id.tv_trunk_width)).setText(return1.getString("trunk_width"));
                    ((TextView)findViewById(R.id.tv_license)).setText(return1.getString("license"));
                    findViewById(R.id.trunk_width_layout).setVisibility(View.VISIBLE);

                    DBHelper mydb = new DBHelper(getApplicationContext());
                    if(job.price!=Integer.parseInt(return1.getString("price"))){
                        mydb.updatePrice(job.job_code,Integer.parseInt(return1.getString("price")));
                        job.price = Integer.parseInt(return1.getString("price"));
                        DecimalFormat formatter = new DecimalFormat("#,###,###");
                        ((TextView)findViewById(R.id.tv_price)).setText("฿" +formatter.format(job.price));
                    }

                    int rating = return1.getInt("rating");

                    switch(rating){
                        case 5:
                            findViewById(R.id.star5).setVisibility(View.VISIBLE);
                        case 4:
                            findViewById(R.id.star4).setVisibility(View.VISIBLE);
                        case 3:
                            findViewById(R.id.star3).setVisibility(View.VISIBLE);
                        case 2:
                            findViewById(R.id.star2).setVisibility(View.VISIBLE);
                        case 1:
                            findViewById(R.id.star1).setVisibility(View.VISIBLE);
                        case 0:
                            break;
                    }


                    String gps_coor = return1.getString("gps_coor");
                    String[] gps_coor2 = gps_coor.split(",");
                    LatLng point = new LatLng(Double.parseDouble(gps_coor2[0]),Double.parseDouble(gps_coor2[1]));

                    Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.truck_marker_orange);
                    Bitmap bitmapResized = Bitmap.createScaledBitmap(b, 80, 80, false);

                    driver_marker = mMap.addMarker(new MarkerOptions()
                            .position(point)
                            .icon(BitmapDescriptorFactory.fromBitmap(bitmapResized))
                            .anchor((float)0.5,(float)0.5)
                    );

                    CameraPosition position = new CameraPosition.Builder().target(point).zoom((float) 12).build();
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(position));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Picasso.with(getApplicationContext()).load(getString(R.string.website_url) + "get_driver_pic.php?tel=" + driver_tel).transform(new CircleTransform()).into((ImageView) findViewById(R.id.driver_image));
                findViewById(R.id.driverLoading).setVisibility(View.INVISIBLE);
            }
        }.execute();

        isVisible = true;
        updateTruckLocation();
    }

    public void updateTruckLocation(){
        if(!isVisible){
            return;
        }

        Log.e("UPDATE","LOCATION");
        new AsyncTask<String,Void,Boolean>(){
            String errorMsg="";
            JSONObject return1;
            @Override
            protected Boolean doInBackground(String... params) {
                BufferedReader reader;
                StringBuilder buffer = new StringBuilder();
                String line;

                try {
                    URL u = new URL(getString(R.string.website_url) + "/track_driver.php?only_gps&job_code=" + job.job_code);
                    HttpURLConnection h = (HttpURLConnection)u.openConnection();
                    h.setRequestMethod("GET");
                    h.setDoOutput(true);
                    h.connect();

                    int response = h.getResponseCode();
                    if (response == 200) {
                        reader = new BufferedReader(new InputStreamReader(h.getInputStream(),"UTF-8"));
                        while((line = reader.readLine()) != null) {
                            buffer.append(line);
                        }

                        return1 = new JSONObject(buffer.toString());
                        return true;
                    }
                    else {
                        errorMsg = "HTTP Error";
                    }
                } catch (MalformedURLException e) {
                    Log.e("UpdateLocTask", "URL Error");
                    errorMsg = "URL Error";
                } catch (IOException e) {
                    Log.e("UpdateLocTask", "No internet connection, Retry soon");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean result){
                try {

                    String gps_coor = return1.getString("gps_coor");
                    String[] gps_coor2 = gps_coor.split(",");
                    LatLng point = new LatLng(Double.parseDouble(gps_coor2[0]),Double.parseDouble(gps_coor2[1]));

                    Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.truck_marker_orange);
                    Bitmap bitmapResized = Bitmap.createScaledBitmap(b, 80, 80, false);

                    driver_marker.remove();
                    driver_marker = mMap.addMarker(new MarkerOptions()
                            .position(point)
                            .icon(BitmapDescriptorFactory.fromBitmap(bitmapResized))
                            .anchor((float)0.5,(float)0.5));


                } catch (JSONException e) {
                    e.printStackTrace();
                }



            }
        }.execute();

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        updateTruckLocation();
                    }
                },
                35000);
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        //mMap.setMyLocationEnabled(true);

        Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.green_marker);
        Bitmap bitmapResized = Bitmap.createScaledBitmap(b, 40, 62, false);
        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(job.jobLocations.get(0).lat,job.jobLocations.get(0).lng))
                .icon(BitmapDescriptorFactory.fromBitmap(bitmapResized))
        );

        for(int i=1;i<job.jobLocations.size();i++){
            b = BitmapFactory.decodeResource(getResources(), R.drawable.orange_marker);
            bitmapResized = Bitmap.createScaledBitmap(b, 40, 62, false);
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(job.jobLocations.get(i).lat,job.jobLocations.get(i).lng))
                    .icon(BitmapDescriptorFactory.fromBitmap(bitmapResized))
            );
        }

        b = BitmapFactory.decodeResource(getResources(), R.drawable.blue_marker);
        bitmapResized = Bitmap.createScaledBitmap(b, 40, 62, false);
        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(job.jobLocations.get(job.jobLocations.size()-1).lat,job.jobLocations.get(job.jobLocations.size()-1).lng))
                .icon(BitmapDescriptorFactory.fromBitmap(bitmapResized))
        );

        CameraPosition position = new CameraPosition.Builder().target(new LatLng(job.jobLocations.get(0).lat,job.jobLocations.get(0).lng)).zoom((float) 12).build();
        this.mMap.moveCamera(CameraUpdateFactory.newCameraPosition(position));
        
    }
}
