package com.tny.khonde;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.cengalabs.flatui.FlatUI;
import com.cengalabs.flatui.views.FlatButton;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
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

public class StartAuctionActivity extends AppCompatActivity {

    int count_time=120;
    int bid_price;
    ProgressDialog loadingDialog;


    @Override
    public void onResume(){
        super.onResume();
        if(MainActivity.job==null)
            finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_auction);

        loadingDialog = new ProgressDialog(this);
        loadingDialog.setMessage("Please wait...");
        loadingDialog.setCancelable(false);

        int[] selected = {Color.parseColor("#cc5d35"), Color.parseColor("#cc5d35"), Color.parseColor("#cc5d35"), Color.parseColor("#cc5d35")};
        ((FlatButton)findViewById(R.id.count_time_1)).getAttributes().setColors(selected);

        EditText et1 =(EditText) findViewById(R.id.et_bid_price);

        et1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(((EditText)findViewById(R.id.et_bid_price)).getText().toString().equals("") || ((EditText)findViewById(R.id.et_bid_price)).getText().toString().equals("0"))
                    findViewById(R.id.startAuctionButton).setEnabled(false);
                else
                    findViewById(R.id.startAuctionButton).setEnabled(true);
            }
        });

        if(MainActivity.job.price>0)
            et1.setText(""+MainActivity.job.price);

    }

    public void changeCountTime(View v){
        int[] selected = {Color.parseColor("#cc5d35"), Color.parseColor("#cc5d35"), Color.parseColor("#cc5d35"), Color.parseColor("#cc5d35")};
        int[] unselected = {Color.parseColor("#994628"), Color.parseColor("#cc5d35"), Color.parseColor("#ff7342"), Color.parseColor("#ffbfa8")};
        switch (v.getId()){
            case R.id.count_time_1:
                ((FlatButton)findViewById(v.getId())).getAttributes().setColors(selected);
                ((FlatButton)findViewById(R.id.count_time_2)).getAttributes().setColors(unselected);
                ((FlatButton)findViewById(R.id.count_time_3)).getAttributes().setColors(unselected);
                count_time = 120;
                break;
            case R.id.count_time_2:
                ((FlatButton)findViewById(v.getId())).getAttributes().setColors(selected);
                ((FlatButton)findViewById(R.id.count_time_1)).getAttributes().setColors(unselected);
                ((FlatButton)findViewById(R.id.count_time_3)).getAttributes().setColors(unselected);
                count_time = 240;
                break;
            case R.id.count_time_3:
                ((FlatButton)findViewById(v.getId())).getAttributes().setColors(selected);
                ((FlatButton)findViewById(R.id.count_time_2)).getAttributes().setColors(unselected);
                ((FlatButton)findViewById(R.id.count_time_1)).getAttributes().setColors(unselected);
                count_time = 360;
                break;
        }
    }

    public void bookTruck(View v){

        EditText et1 =(EditText) findViewById(R.id.et_bid_price);
        bid_price = Integer.parseInt(et1.getText().toString());
        MainActivity.job.price = bid_price;
        MainActivity.job.datetime = (int)(MainActivity.pickupDate.getTimeInMillis()/1000);

        new AsyncTask<String, Void, Boolean>() {
            String errorMsg = "",line ="";
            JSONObject json;
            StringBuilder buffer = new StringBuilder();
            protected void onPreExecute() {
                loadingDialog.show();
            }

            @Override
            protected Boolean doInBackground(String... params) {

                List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();

                SharedPreferences preferences = getSharedPreferences("UserInfo",MODE_PRIVATE);
                String tel = preferences.getString("tel","");
                String customer_name = preferences.getString("firstname","");

                JSONArray jobLocations = new JSONArray();

                for(int i=0;i<MainActivity.job.jobLocations.size();i++){
                    JobLocation the_location = MainActivity.job.jobLocations.get(i);
                    JSONObject jobLocation = new JSONObject();
                    try {
                        jobLocation.put("address",the_location.desc);
                        jobLocation.put("name",the_location.contact_name);
                        jobLocation.put("tel",the_location.contact_tel);
                        jobLocation.put("gps_coor",the_location.lat + "," + the_location.lng);
                        jobLocation.put("province",the_location.province);
                        jobLocation.put("sub1",the_location.sub1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    jobLocations.put(jobLocation);
                }

                JSONObject jobObject = new JSONObject();
                try {

                    jobObject.put("pickup_datetime",""+MainActivity.job.datetime);
                    jobObject.put("truck_type",""+MainActivity.job.truck_type);
                    jobObject.put("roof_type",""+MainActivity.job.roof_type);
                    jobObject.put("trunk_width",""+MainActivity.job.trunk_width);
                    jobObject.put("extra",""+MainActivity.job.extra);
                    jobObject.put("truck_count",""+MainActivity.job.truck_count);
                    jobObject.put("helper_count",""+MainActivity.job.helper_count);
                    jobObject.put("current_bid",""+MainActivity.job.price);
                    jobObject.put("remark",""+MainActivity.job.remark);
                    jobObject.put("customer_name",customer_name);
                    jobObject.put("customer_tel",tel);
                    jobObject.put("auction_time",count_time);

                    jobObject.put("locations",jobLocations);
                    nameValuePair.add(new BasicNameValuePair("info",jobObject.toString()));

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                HttpClient httpClient = new DefaultHttpClient();
                // replace with your url
                HttpPost httpPost = new HttpPost(getString(R.string.website_url) + "/book_truck.php");

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

                    Log.e("RETURN",buffer.toString());

                    json = new JSONObject(buffer.toString());

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
                    try {
                        JSONArray job_codes = json.getJSONArray("job_codes");
                        for(int i=0;i<job_codes.length();i++) {
                            MainActivity.job.job_code = job_codes.getString(i);
                            MainActivity.job_codes.add(job_codes.getString(i));
                            MainActivity.insertJob();
                            Log.e("INSERT ",MainActivity.job.job_code);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    startAuction();
                } else {
                    MainActivity.makeToast(errorMsg,getApplicationContext());
                }

                loadingDialog.dismiss();

            }
        }.execute();

    }

    public void startAuction(){

        Intent intent = new Intent(this,InAuctionActivity.class);
        intent.putExtra("time",count_time);
        intent.putExtra("bid_price",bid_price);
        startActivityForResult(intent,5000);
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == 5000) {
            Intent res = new Intent();
            setResult(RESULT_OK, res);
            finish();
        }
    }
}
