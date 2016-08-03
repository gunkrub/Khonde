package com.tny.khonde;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.cengalabs.flatui.views.FlatButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class MainActivity extends AppCompatActivity {
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final int BOOK_TRUCK = 5000;
    private static final int COMMENT_ACTIVITY = 6000;
    private static final int CHOOSE_PICKUP_LOCATION = 2000;
    private static final int CHOOSE_DESTINATION = 2001;
    static boolean loadFromDb = false;
    static boolean bid_success = false;
    static boolean comment_success = false;
    ArrayList<Dialog> dialogs = new ArrayList<>();
    static boolean isVisible = false;
    static Job job;
    static DBHelper mydb;
    private boolean isReceiverRegistered;
    static ViewPager pager;
    ViewPagerAdapter adapter;
    SlidingTabLayout tabs;
    CharSequence Titles[]={"จองรถ","รายการ","อื่นๆ"};
    int Numboftabs =3;
    ProgressDialog loadingDialog;
    BroadcastReceiver receiver;

    ArrayList<Integer> stop_by_locations_remove_buttons = new ArrayList<>();
    ArrayList<Integer> stop_by_locations_views = new ArrayList<>();
    static ArrayList<String> job_codes = new ArrayList<>();

    static Calendar pickupDate = Calendar.getInstance();

    @Override
    public void onDestroy(){
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences preferences = getSharedPreferences("UserInfo",MODE_PRIVATE);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(pager!=null) {
                    CurrentJobFragment cur1 = (CurrentJobFragment) Tab3.pager.getAdapter().instantiateItem(Tab3.pager, 0);
                    FinishedJobFragment fin1 = (FinishedJobFragment) Tab3.pager.getAdapter().instantiateItem(Tab3.pager, 1);
                    cur1.refreshTab();
                    fin1.refreshTab();
                    checkCommentNeeded();
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction("JOBDONE");
        registerReceiver(receiver,filter);

        /*
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("tel","0802334834");
        editor.commit();
*/
        if(preferences.getString("tel","").equals("")) {
            Intent loginorregister = new Intent(this, LoginOrRegisterActivity.class);
            startActivityForResult(loginorregister,3000);
        } else {
            if (checkPlayServices()) {
                // Start IntentService to register this application with GCM.
                Intent intent = new Intent(this, RegistrationIntentService.class);
                startService(intent);
            }
        }

        job = new Job();
        job_codes = new ArrayList<>();
        mydb = new DBHelper(this);

        loadingDialog = new ProgressDialog(this);
        loadingDialog.setMessage("Loading..");
        loadingDialog.setCancelable(false);

        // Creating The ViewPagerAdapter and Passing Fragment Manager, Titles fot the Tabs and Number Of Tabs.
        adapter =  new ViewPagerAdapter(getSupportFragmentManager(),Titles,Numboftabs,getApplicationContext());

        // Assigning ViewPager View and setting the adapter
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(adapter);
        pager.setOffscreenPageLimit(3);

        // Assiging the Sliding Tab Layout View
        tabs = (SlidingTabLayout) findViewById(R.id.tabs);
        tabs.setDistributeEvenly(true); // To make the Tabs Fixed set this true, This makes the tabs Space Evenly in Available width
        tabs.setCustomTabView(R.layout.custom_tab, R.id.textView0,R.id.imageView);
        // Setting Custom Color for the Scroll bar indicator of the Tab View
        tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(android.R.color.white);
            }
        });

        // Setting the ViewPager For the SlidingTabsLayout
        tabs.setViewPager(pager);

        tabs.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if(position==0 && loadFromDb){
                    loadDetailsAndMarker();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        Intent intent = getIntent();
        if(intent.getBooleanExtra("gotoJobs",false))
            pager.setCurrentItem(1);
    }

    public void loadDetailsAndMarker(){

        Tab2.mMap.clear();

        ((TextView)findViewById(R.id.pickup_location_desc)).setTypeface(null,Typeface.NORMAL);
        ((TextView)findViewById(R.id.pickup_location_desc)).setText(job.jobLocations.get(0).desc);

        Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.green_marker);
        Bitmap bitmapResized = Bitmap.createScaledBitmap(b, 40, 62, false);

        job.jobLocations.get(0).marker = Tab2.mMap.addMarker(new MarkerOptions()
                .position(new LatLng(job.jobLocations.get(0).lat,job.jobLocations.get(0).lng))
                .icon(BitmapDescriptorFactory.fromBitmap(bitmapResized)));

        LinearLayout layout = (LinearLayout) findViewById(R.id.stop_by_layout);
        for(int i=0;i<layout.getChildCount();i++) {
            layout.removeViewAt(i);
        }

        for(int i=1;i<job.jobLocations.size()-1;i++){

            stop_by_locations_remove_buttons = new ArrayList<>();
            stop_by_locations_views = new ArrayList<>();

            addStopByLocation(null);

            b = BitmapFactory.decodeResource(getResources(), R.drawable.orange_marker);
            bitmapResized = Bitmap.createScaledBitmap(b, 40, 62, false);

            job.jobLocations.get(i).marker = Tab2.mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(job.jobLocations.get(i).lat,job.jobLocations.get(i).lng))
                    .icon(BitmapDescriptorFactory.fromBitmap(bitmapResized))
            );

            TextView tv1 = (TextView) ((RelativeLayout)(layout.getChildAt(i-1))).getChildAt(0);
            tv1.setText(job.jobLocations.get(i).desc);

        }

        ((TextView)findViewById(R.id.destination_desc)).setTypeface(null,Typeface.NORMAL);
        ((TextView)findViewById(R.id.destination_desc)).setText(job.jobLocations.get(job.jobLocations.size()-1).desc);

        b = BitmapFactory.decodeResource(getResources(), R.drawable.blue_marker);
        bitmapResized = Bitmap.createScaledBitmap(b, 40, 62, false);

        job.jobLocations.get(job.jobLocations.size()-1).marker = Tab2.mMap.addMarker(new MarkerOptions()
                .position(new LatLng(job.jobLocations.get(job.jobLocations.size()-1).lat,job.jobLocations.get(job.jobLocations.size()-1).lng))
                .icon(BitmapDescriptorFactory.fromBitmap(bitmapResized)));

        loadFromDb = false;
        checkAllDestinationsSet();
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i("Khonde", "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    public void loadCustomerInfo(){
        final SharedPreferences preferences = getSharedPreferences("UserInfo",MODE_PRIVATE);
        if(preferences.getString("tel","").equals(""))
            return;

        new AsyncTask<String, Void, Boolean>() {
            String errorMsg="";
            JSONObject return1;
            @Override
            protected Boolean doInBackground(String... params) {
                BufferedReader reader;
                StringBuilder buffer = new StringBuilder();
                String line;

                try {
                    URL u = new URL(getString(R.string.website_url) + "/get_info_customer.php?tel="+ preferences.getString("tel",""));
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

                        if(return1.getBoolean("result"))
                            return true;
                        else
                            return false;

                    }

                } catch (MalformedURLException e) {
                    Log.e("UpdateLocTask", "URL Error");
                    errorMsg = "URL Error";
                } catch (IOException e) {
                    Log.e("UpdateLocTask", "No internet connection, Retry soon");
                    errorMsg = "กรุณาเชื่อมต่ออินเตอร์เน็ต";
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean result){
                SharedPreferences preferences = getSharedPreferences("UserInfo",MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();

                if(result){
                    try {
                        editor.putString("tel",return1.getString("tel"));
                        editor.putString("firstname",return1.getString("firstname"));
                        editor.putString("lastname",return1.getString("lastname"));
                        editor.putString("email",return1.getString("email"));
                        editor.apply();

                        if(return1.getString("tel").equals("")) {
                            Intent loginorregister = new Intent(getApplicationContext(), LoginOrRegisterActivity.class);
                            startActivity(loginorregister);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else {
                    makeToast(errorMsg,getApplicationContext());
                }
            }
        }.execute();
    }

    @Override
    public void onPause(){
        super.onPause();
        isVisible = false;

        for (int i=0;i<dialogs.size();i++) {
            if (dialogs.get(i) != null)
                dialogs.get(i).dismiss();
        }
    }
    @Override
    public void onResume(){
        super.onResume();
        isVisible = true;
        loadCustomerInfo();

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();

        checkCommentNeeded();
        checkShowDriverInfoNeeded();

        if(bid_success){
            bid_success = false;

            pager.setCurrentItem(1,true);
            Tab3.pager.setCurrentItem(0,true);

        }

        if(comment_success){
            comment_success = false;
            showCommentSuccessDialog();
        }
    }

    public void showCommentSuccessDialog(){
            final Dialog dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.driver_dialog);
            dialog.setCancelable(false);
            FlatButton dialogButton = (FlatButton) dialog.findViewById(R.id.dismissButton);

            ((TextView) dialog.getWindow().findViewById(R.id.tv_header)).setText("ขอบคุณค่ะ");
            ((TextView) dialog.getWindow().findViewById(R.id.tv_content)).setText("ใบเสร็จจะถูกจัดส่งผ่านอีเมลล์");
            dialog.getWindow().findViewById(R.id.driver_layout).setVisibility(View.GONE);

            dialogButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            dialog.show();

    }
    public void checkShowDriverInfoNeeded(){
        dialogs = new ArrayList<>();
        ArrayList<String> driversNotPushed = mydb.driverInfoNotPushed();
        for(int i=0;i<driversNotPushed.size();i++)
            getDriverInfo(driversNotPushed.get(i));
    }

    public void checkCommentNeeded(){
        ArrayList<String> commentNeeded = mydb.commentNeeded();
        for(int i=0;i<commentNeeded.size();i++)
            showComment(commentNeeded.get(i));
    }

    public void getDriverInfo(final String job_code){
        final Context context = this;
        new AsyncTask<String, Void, Boolean>() {
            JSONObject return1;
            BufferedReader reader;
            StringBuilder buffer = new StringBuilder();
            String line,errorMsg = "";
            @Override
            protected Boolean doInBackground(String... params) {
                try {
                    URL u = new URL(getString(R.string.website_url) + "/track_driver.php?job_code="+ job_code);
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

                } catch (MalformedURLException e) {
                    Log.e("UpdateLocTask", "URL Error");
                } catch (IOException e) {
                    errorMsg = "กรุณาเชื่อมต่ออินเตอร์เน็ต";
                    Log.e("UpdateLocTask", "No internet connection, Retry soon");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean result){
                if(!errorMsg.equals("")) {
                    makeToast(errorMsg, context);
                    return;
                }

                final Dialog dialog = new Dialog(context);

                dialogs.add(dialog);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.driver_dialog);
                dialog.setCancelable(false);
                FlatButton dialogButton = (FlatButton) dialog.findViewById(R.id.dismissButton);

                try {

                    if(!return1.getString("errorMsg").equals("")) {
                        ((TextView) dialog.getWindow().findViewById(R.id.tv_header)).setText("ไม่พบคนขับ");
                        ((TextView) dialog.getWindow().findViewById(R.id.tv_content)).setText("ไม่พบคนขับ\nกรุณาลองจองใหม่\nหรือติดต่อทีมงาน");
                        dialog.getWindow().findViewById(R.id.driver_layout).setVisibility(View.GONE);

                    } else {

                        ArrayList<String> job_code1 = new ArrayList<String>();
                        job_code1.add(job_code);
                        mydb.updateStatus(job_code1,0);
                        if(pager!=null) {
                            CurrentJobFragment cur1 = (CurrentJobFragment) Tab3.pager.getAdapter().instantiateItem(Tab3.pager, 0);
                            cur1.refreshTab();
                        }

                        JSONObject driver = return1;

                        ((TextView) dialog.getWindow().findViewById(R.id.tv_driver_name)).setText(driver.getString("name"));

                        int rating = driver.getInt("rating");

                        switch (rating) {
                            case 5:
                                dialog.getWindow().findViewById(R.id.star5).setVisibility(View.VISIBLE);
                            case 4:
                                dialog.getWindow().findViewById(R.id.star4).setVisibility(View.VISIBLE);
                            case 3:
                                dialog.getWindow().findViewById(R.id.star3).setVisibility(View.VISIBLE);
                            case 2:
                                dialog.getWindow().findViewById(R.id.star2).setVisibility(View.VISIBLE);
                            case 1:
                                dialog.getWindow().findViewById(R.id.star1).setVisibility(View.VISIBLE);
                            case 0:
                                break;
                        }
                        DecimalFormat formatter = new DecimalFormat("#,###,###");
                        mydb.updatePrice(job_code,Integer.parseInt(driver.getString("price")));
                        ((TextView) dialog.getWindow().findViewById(R.id.tv_price)).setText("฿ " + formatter.format(Integer.parseInt(driver.getString("price"))));
                        ((TextView) dialog.getWindow().findViewById(R.id.tv_truck_type)).setText(driver.getString("truck_type"));
                        ((TextView) dialog.getWindow().findViewById(R.id.tv_trunk_width)).setText("ยาว " + driver.getString("trunk_width") + " ม.");
                        ((TextView) dialog.getWindow().findViewById(R.id.tv_license)).setText(driver.getString("license"));

                        Picasso.with(getApplicationContext()).load(getString(R.string.website_url) + "get_driver_pic.php?tel=" + driver.getString("driver_tel")).transform(new CircleTransform()).into((ImageView) dialog.findViewById(R.id.driver_image));

                        //Log.e("พบคนขับ","YES");

                    }

                    dialogButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mydb.gotDriverInfo(job_code);
                            dialog.dismiss();
                        }
                    });

                    dialog.show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.execute();

    }
    public static void sendNotification(String message,Context context) {
        Intent intent = new Intent(context, MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);

        stackBuilder.addNextIntent(intent);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0,
                PendingIntent.FLAG_CANCEL_CURRENT);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.khonde_logo)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),R.drawable.khonde_logo))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setContentTitle("ขนดี")
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification = notificationBuilder.build();
        int smallIconId = context.getResources().getIdentifier("right_icon", "id", android.R.class.getPackage().getName());
        if (smallIconId != 0) {
            notification.contentView.setViewVisibility(smallIconId, View.INVISIBLE);
        }
        notificationManager.notify(0 /* ID of notification */, notification);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode==RESULT_OK){
            if(requestCode==2000){
                ((TextView)findViewById(R.id.pickup_location_desc)).setText("Loading...");
                ((TextView)findViewById(R.id.pickup_location_desc)).setTypeface(null,Typeface.ITALIC);
                getLocationDesc(1,0);

                    Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.green_marker);
                    Bitmap bitmapResized = Bitmap.createScaledBitmap(b, 40, 62, false);

                    if(job.jobLocations.get(0).marker!=null)
                        job.jobLocations.get(0).marker.remove();

                job.jobLocations.get(0).marker = Tab2.mMap.addMarker(new MarkerOptions()
                            .position(Tab2.startPoint)
                            .icon(BitmapDescriptorFactory.fromBitmap(bitmapResized)));

                job.jobLocations.get(0).lat = job.jobLocations.get(0).marker.getPosition().latitude;
                job.jobLocations.get(0).lng = job.jobLocations.get(0).marker.getPosition().longitude;

                Tab2.showThailand();
            } else if(requestCode==2001){
                ((TextView)findViewById(R.id.destination_desc)).setText("Loading...");
                ((TextView)findViewById(R.id.destination_desc)).setTypeface(null,Typeface.ITALIC);
                getLocationDesc(2,0);

                Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.blue_marker);
                Bitmap bitmapResized = Bitmap.createScaledBitmap(b, 40, 62, false);

                if(job.jobLocations.get(job.jobLocations.size()-1).marker!=null)
                    job.jobLocations.get(job.jobLocations.size()-1).marker.remove();

                job.jobLocations.get(job.jobLocations.size()-1).marker = Tab2.mMap.addMarker(new MarkerOptions()
                        .position(Tab2.startPoint)
                        .icon(BitmapDescriptorFactory.fromBitmap(bitmapResized)));
                job.jobLocations.get(job.jobLocations.size()-1).lat = job.jobLocations.get(job.jobLocations.size()-1).marker.getPosition().latitude;
                job.jobLocations.get(job.jobLocations.size()-1).lng = job.jobLocations.get(job.jobLocations.size()-1).marker.getPosition().longitude;

                Tab2.showThailand();

            } else if(requestCode==2002){
                int index1 = data.getIntExtra("index1",0);
                LinearLayout layout = (LinearLayout)findViewById(R.id.stop_by_layout);

                TextView tv1 = (TextView) ((RelativeLayout)(layout.getChildAt(index1))).getChildAt(0);
                tv1.setText("Loading...");
                tv1.setTypeface(null,Typeface.ITALIC);

                getLocationDesc(3,index1);

                if(job.jobLocations.get(index1+1).marker!=null)
                    job.jobLocations.get(index1+1).marker.remove();

                Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.orange_marker);
                Bitmap bitmapResized = Bitmap.createScaledBitmap(b, 40, 62, false);

                job.jobLocations.get(index1+1).marker = Tab2.mMap.addMarker(new MarkerOptions()
                        .position(Tab2.startPoint)
                        .icon(BitmapDescriptorFactory.fromBitmap(bitmapResized))
                );
                job.jobLocations.get(index1+1).lat = job.jobLocations.get(index1+1).marker.getPosition().latitude;
                job.jobLocations.get(index1+1).lng = job.jobLocations.get(index1+1).marker.getPosition().longitude;

                Tab2.showThailand();

            } else if(requestCode==3001) {
                SharedPreferences preferences = getSharedPreferences("UserInfo",MODE_PRIVATE);
                if(preferences.getString("tel","").equals("")) {
                    Intent loginorregister = new Intent(this, LoginOrRegisterActivity.class);
                    startActivityForResult(loginorregister,3000);
                }
            } else if(requestCode==3000){
                if (checkPlayServices()) {
                    // Start IntentService to register this application with GCM.
                    Intent intent = new Intent(this, RegistrationIntentService.class);
                    startService(intent);
                }
            }

        } else {
            if(requestCode==3000){
                finish();
            }
        }
    }

    public void checkAllDestinationsSet(){

        for(int i=0; i<job.jobLocations.size();i++) {
            if (job.jobLocations.get(i).marker == null || job.jobLocations.get(i).province == null)
                return;
        }

        findViewById(R.id.bookingButton1).setEnabled(true);
        findViewById(R.id.bookingButton2).setEnabled(true);

    }

    public void getLocationDesc(final int ofwhere, final int index){

        new AsyncTask<String, Void, Boolean>() {
            String errorMsg = "";
            String desc,province,sub1;
            @Override
            protected void onPreExecute() {

            }

            @Override
            protected Boolean doInBackground(String... params) {
                BufferedReader reader;
                StringBuilder buffer = new StringBuilder();
                String line;

                try {
                    URL u = new URL("http://maps.googleapis.com/maps/api/geocode/json?language=TH&latlng=" + Tab2.startPoint.latitude + "," + Tab2.startPoint.longitude);
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

                        JSONObject return1 = new JSONObject(buffer.toString());
                        JSONArray result0 = return1.getJSONArray("results");
                        JSONObject result = result0.getJSONObject(0);
                        desc = result.getString("formatted_address");

                        JSONArray components = result.getJSONArray("address_components");
                        for(int i=0;i<components.length();i++){
                            JSONObject component = components.getJSONObject(i);
                            JSONArray types = component.getJSONArray("types");
                            for(int j=0;j<types.length();j++){
                                String type_name = types.getString(j);
                                if(type_name.equals("administrative_area_level_1")){
                                    province = component.getString("long_name");
                                }

                                if(type_name.equals("administrative_area_level_2") || type_name.equals("sublocality_level_1")){
                                    sub1 = component.getString("long_name");
                                }
                            }
                        }

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
            protected void onPostExecute(Boolean result) {
                if(result){
                    switch (ofwhere){
                        case 1:
                            ((TextView)findViewById(R.id.pickup_location_desc)).setTypeface(null,Typeface.NORMAL);
                            ((TextView)findViewById(R.id.pickup_location_desc)).setText(desc);
                            job.jobLocations.get(0).desc = desc;
                            job.jobLocations.get(0).province = province;
                            job.jobLocations.get(0).sub1 = sub1;
                            break;
                        case 2:
                            ((TextView)findViewById(R.id.destination_desc)).setTypeface(null,Typeface.NORMAL);
                            ((TextView)findViewById(R.id.destination_desc)).setText(desc);
                            job.jobLocations.get(job.jobLocations.size()-1).desc = desc;
                            job.jobLocations.get(job.jobLocations.size()-1).province = province;
                            job.jobLocations.get(job.jobLocations.size()-1).sub1 = sub1;
                            break;
                        case 3:
                            LinearLayout layout = (LinearLayout)findViewById(R.id.stop_by_layout);

                            TextView tv1 = (TextView) ((RelativeLayout)(layout.getChildAt(index))).getChildAt(0);
                            job.jobLocations.get(index+1).desc = desc;
                            job.jobLocations.get(index+1).province = province;
                            job.jobLocations.get(index+1).sub1 = sub1;
                            tv1.setTypeface(null,Typeface.NORMAL);
                            tv1.setText(desc);
                            break;
                    }
                }

                checkAllDestinationsSet();

            }
        }.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    public void startMaps(View v){
        Intent intent = new Intent(this,MapsActivity.class);
        switch (v.getId()) {
            case R.id.pickup_location_layout:
                if(job.jobLocations.get(0).marker!=null){
                    intent.putExtra("lat",job.jobLocations.get(0).marker.getPosition().latitude);
                    intent.putExtra("long",job.jobLocations.get(0).marker.getPosition().longitude);
                }
                intent.putExtra("requestCode",CHOOSE_PICKUP_LOCATION);
                startActivityForResult(intent,CHOOSE_PICKUP_LOCATION);
                break;
            case R.id.destination_layout:
                if(job.jobLocations.get(job.jobLocations.size()-1).marker!=null){
                    intent.putExtra("lat",job.jobLocations.get(job.jobLocations.size()-1).marker.getPosition().latitude);
                    intent.putExtra("long",job.jobLocations.get(job.jobLocations.size()-1).marker.getPosition().longitude);
                }
                intent.putExtra("requestCode",CHOOSE_DESTINATION);
                startActivityForResult(intent,CHOOSE_DESTINATION);
                break;
        }

    }

    public void startChooseTruckType(View v){

        if(v.getId()==R.id.bookingButton1){
            Calendar now = Calendar.getInstance();
            pickupDate.setTimeInMillis(now.getTimeInMillis() + 3600000);
            Intent intent = new Intent(getApplicationContext(), ChooseTruckTypeActivity.class);
            startActivityForResult(intent,BOOK_TRUCK);

        }else {
            showSelectTimeDialog(this);
        }
    }



    public static void showSelectTimeDialog(final Context context){
        final Calendar now = Calendar.getInstance();

        final TimePickerDialog pickupTimeDialog = new TimePickerDialog(context, R.style.DialogTheme, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                pickupDate.set(pickupDate.get(Calendar.YEAR), pickupDate.get(Calendar.MONTH), pickupDate.get(Calendar.DATE), hourOfDay, minute);

                if(pickupDate.getTimeInMillis()< now.getTimeInMillis()+3600000)
                    pickupDate.setTimeInMillis(now.getTimeInMillis() + 3600000);

                Intent intent = new Intent(context, ChooseTruckTypeActivity.class);
                context.startActivity(intent);
            }
        }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true);

        DatePickerDialog pickupDateDialog = new DatePickerDialog(context, R.style.DialogTheme, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                pickupDate.set(year, monthOfYear, dayOfMonth);
                pickupTimeDialog.show();
            }

        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));

        pickupDateDialog.getDatePicker().setMinDate(new Date().getTime());
        pickupDateDialog.show();
    }


    public void showComment(String job_code){
        Intent intent = new Intent(this,CommentActivity.class);
        intent.putExtra("job_code",job_code);
        startActivityForResult(intent,COMMENT_ACTIVITY);
    }

    public void addStopByLocation(View v){
        final LinearLayout layout = (LinearLayout)findViewById(R.id.stop_by_layout);
        View new_view = View.inflate(this,R.layout.location_layout,null);
        int new_button_id = View.generateViewId();
        new_view.findViewById(R.id.remove_button).setId(new_button_id);
        stop_by_locations_remove_buttons.add(new_button_id);

        int new_view_id = View.generateViewId();
        new_view.findViewById(R.id.stop_by_view).setId(new_view_id);
        stop_by_locations_views.add(new_view_id);

        if(!loadFromDb)
            job.jobLocations.add(job.jobLocations.size()-1,new JobLocation());

        findViewById(R.id.bookingButton1).setEnabled(false);
        findViewById(R.id.bookingButton2).setEnabled(false);

        //To Remove Stop by Location
        new_view.findViewById(new_button_id).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(int i=0;i<stop_by_locations_remove_buttons.size();i++){
                    if(v.getId()==stop_by_locations_remove_buttons.get(i)){
                        stop_by_locations_remove_buttons.remove(i);
                        stop_by_locations_views.remove(i);
                        layout.removeViewAt(i);

                        job.clearJobDetails();

                        if(job.jobLocations.get(i+1).marker!=null)
                            job.jobLocations.get(i+1).marker.remove();

                        job.jobLocations.remove(i+1);

                        checkAllDestinationsSet();
                        break;
                    }
                }
            }
        });

        //To pick gps_coor of stop by Location
        new_view.findViewById(new_view_id).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                for (int i = 0; i < stop_by_locations_views.size(); i++) {
                    if (v.getId() == stop_by_locations_views.get(i)) {
                        intent.putExtra("index1", i);
                        if(job.jobLocations.get(i+1).marker!=null){
                            intent.putExtra("lat",job.jobLocations.get(i+1).marker.getPosition().latitude);
                            intent.putExtra("long",job.jobLocations.get(i+1).marker.getPosition().longitude);
                        }
                        intent.putExtra("requestCode",2002);
                        startActivityForResult(intent,2002);
                        break;
                    }
                }
            }
        });

        layout.addView(new_view);
    }
    public void click1(View v) {

        switch (v.getId()) {
            case R.id.annoucement_button:
                Intent intent0 = new Intent(this,AnnouncementActivity.class);
                startActivity(intent0);
                break;
            case R.id.profile_button:
                Intent intent = new Intent(this,ProfileActivity.class);
                startActivityForResult(intent,3001);
                break;
            case R.id.contact_button:
                Intent intent4 = new Intent(this, ContactUsActivity.class);
                startActivity(intent4);
                break;
        }
    }

    public static void insertJob(){
        Log.e("INSERT","JOB");
        mydb.insertJob(job);
    }


    public static void makeToast(String msg,Context context){
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

}
