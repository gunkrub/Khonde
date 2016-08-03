package com.tny.khonde;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import com.github.lzyzsd.circleprogress.DonutProgress;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.ClientProtocolException;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.message.BasicNameValuePair;
import cz.msebera.android.httpclient.protocol.HTTP;
import cz.msebera.android.httpclient.util.TextUtils;

public class InAuctionActivity extends AppCompatActivity {
    DecimalFormat formatter = new DecimalFormat("#,###,###");
    int current_bid = 9999999;
    ArrayList<String> job_codes = new ArrayList<>();
    int countdown_time = 0;
    int max_time =0;
    boolean isVisible = true;
    ProgressDialog loadingDialog;
    BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_auction);

        job_codes = MainActivity.job_codes;

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                TextView tv1 = ((TextView)findViewById(R.id.bid_price));
                String new_bid = intent.getStringExtra("new_bid");
                String job_code = intent.getStringExtra("job_code");

                int new_bid_int = Integer.parseInt(new_bid);
                DBHelper mydb = new DBHelper(context);
                mydb.updatePrice(job_code, new_bid_int);

                if(new_bid_int< current_bid) {
                    tv1.setText("" + formatter.format(new_bid_int));

                    Animation anim = new AlphaAnimation(0.0f, 1.0f);
                    anim.setDuration(1000); //You can manage the time of the blink with this parameter
                    tv1.startAnimation(anim);
                }

                current_bid = new_bid_int;
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction("JOBBID");
        registerReceiver(receiver,filter);

        loadingDialog = new ProgressDialog(this);
        loadingDialog.setMessage("Please wait...");
        loadingDialog.setCancelable(false);

        Intent intent = getIntent();
        int count_down_time = intent.getIntExtra("time",0);
        int bid_price = intent.getIntExtra("bid_price",0);
        this.countdown_time = count_down_time;
        max_time = count_down_time;
        ((TextView)findViewById(R.id.tv_countdown_time)).setText("" + count_down_time);
        ((TextView)findViewById(R.id.bid_price)).setText("" + formatter.format(bid_price));
        ((DonutProgress)findViewById(R.id.donut_progress)).setMax(max_time);
        ((DonutProgress)findViewById(R.id.donut_progress)).setProgress(max_time);

        ((TextView)findViewById(R.id.tv_pickup_province)).setText(MainActivity.job.jobLocations.get(0).province);
        ((TextView)findViewById(R.id.tv_dropoff_province)).setText(MainActivity.job.jobLocations.get(MainActivity.job.jobLocations.size()-1).province);

        String []truck_types = MainActivity.job.truck_type.split(";");
        String truck_type_string = "";
        for(int i=0;i<truck_types.length-1;i++)
            truck_type_string += truck_types[i] + "\n";

        truck_type_string += truck_types[truck_types.length-1];

        ((TextView)findViewById(R.id.tv_truck_type)).setText(truck_type_string);

            if (!MainActivity.job.trunk_width.toString().substring(0, 1).equals("เ"))
                ((TextView) findViewById(R.id.tv_trunk_width)).setText("ยาวมากกว่า " + MainActivity.job.trunk_width);

        ((TextView)findViewById(R.id.tv_roof_type)).setText(MainActivity.job.roof_type);

        String extra = "";
        if(!MainActivity.job.extra.equals("ไม่ต้องการอุปกรณ์เสริม"))
            extra = MainActivity.job.extra;

        ((TextView)findViewById(R.id.tv_extra)).setText(extra);


        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        countdown();
                    }
                },
                1000);
    }

    public void countdown() {
        countdown_time--;
        ((TextView) findViewById(R.id.tv_countdown_time)).setText("" + countdown_time);
        ((DonutProgress) findViewById(R.id.donut_progress)).setProgress(countdown_time);

        if(countdown_time<=7)
            findViewById(R.id.cancleButton).setEnabled(false);

        if (countdown_time > 0) {
            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            countdown();
                        }
                    },
                    1000);
        } else {
            end_bid();
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        isVisible = false;
    }

    @Override
    public void onResume(){
        super.onResume();
        isVisible = true;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    public void end_bid(){

        MainActivity.sendNotification("การประมูลงานของท่านเสร็จสิ้นแล้ว",this);
        MainActivity.bid_success = true;
        Intent res = new Intent();
        setResult(RESULT_OK, res);
        finish();
        /*
        finishAffinity();
        if(isVisible) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("gotoJobs",true);
            startActivity(intent);
        }
        */
    }



    public void cancel_job(View v){

        new AlertDialog.Builder(this)
                .setTitle("แจ้งเตือน")
                .setMessage("ยืนยันยกเลิกการประมูลงาน?")
                .setNegativeButton("ไม่ใช่", null)
                .setPositiveButton("ยืนยัน", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {

                        new AsyncTask<String, Void, Boolean>() {
                            String errorMsg = "",line ="";
                            StringBuilder buffer = new StringBuilder();
                            protected void onPreExecute() {
                                loadingDialog.show();
                            }

                            @Override
                            protected Boolean doInBackground(String... params) {

                                List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();

                                HttpClient httpClient = new DefaultHttpClient();
                                // replace with your url
                                HttpPost httpPost = new HttpPost(getString(R.string.website_url) + "/cancel_job.php?job_codes=" + android.text.TextUtils.join(";",job_codes));

                                //Encoding POST data
                                try {
                                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair, HTTP.UTF_8));
                                } catch (UnsupportedEncodingException e) {
                                    // log exception
                                    e.printStackTrace();
                                }

                                //making POST request.

                                try {
                                    HttpResponse response = httpClient.execute(httpPost);
                                    BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                                    while((line = reader.readLine()) != null) {
                                        buffer.append(line);
                                    }
                                    Log.e("Return: ",buffer.toString());

                                    JSONObject json = new JSONObject(buffer.toString());
                                    errorMsg = json.getString("errorMsg");

                                    if(errorMsg.equals(""))
                                        return true;
                                    else
                                        return false;
                                } catch (ClientProtocolException e) {
                                    // Log exception
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    // Log exception
                                    Log.e("", "กรุณาเชื่อมต่ออินเตอร์เน็ต");
                                    errorMsg = "กรุณาเชื่อมต่ออินเตอร์เน็ต";
                                    e.printStackTrace();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                return false;
                            }

                            @Override
                            protected void onPostExecute(Boolean result) {

                                if(result){
                                    DBHelper mydb = new DBHelper(getApplicationContext());
                                    mydb.cancelJob(job_codes);
                                    MainActivity.makeToast("ยกเลิกงานสำเร็จ",getApplicationContext());
                                    finish();
                                } else {
                                    MainActivity.makeToast(errorMsg,getApplicationContext());
                                }

                                loadingDialog.dismiss();

                            }
                        }.execute();
                    }

                }).create().show();
    }
    @Override

    public void onBackPressed() {
        if(countdown_time<=7)
            return;
        cancel_job(null);
    }
}
