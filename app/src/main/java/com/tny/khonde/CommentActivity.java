package com.tny.khonde;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.cengalabs.flatui.views.FlatButton;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CommentActivity extends AppCompatActivity {
    int rating=0;
    String job_code ="";
    String driver_tel="";
    ProgressDialog loadingDialog;
    DBHelper mydb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        Intent intent = getIntent();
        job_code = intent.getStringExtra("job_code");

        loadingDialog = new ProgressDialog(this);
        loadingDialog.setMessage("Loading..");
        loadingDialog.setCancelable(false);

        mydb = new DBHelper(this);
        Job the_job = mydb.getJob(job_code);
        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy kk:mm");
        DecimalFormat formatter = new DecimalFormat("#,###,###");


        Date date = new Date();
        date.setTime(((long)the_job.datetime)*1000);

        ((TextView)findViewById(R.id.tv_datetime)).setText(dateFormatter.format(date));
        ((TextView)findViewById(R.id.tv_job_code)).setText(job_code);
        ((TextView)findViewById(R.id.tv_price)).setText("฿" +formatter.format(the_job.price));
        ((TextView)findViewById(R.id.tv_from_to_province)).setText(the_job.jobLocations.get(0).province + " - " + the_job.jobLocations.get(the_job.jobLocations.size()-1).province);


        new AsyncTask<String,Void,Boolean>(){
            String errorMsg="";
            JSONObject return1;
            @Override
            protected Boolean doInBackground(String... params) {
                BufferedReader reader;
                StringBuilder buffer = new StringBuilder();
                String line;

                try {
                    URL u = new URL(getString(R.string.website_url) + "/track_driver.php?job_code=" +job_code);
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
                    ((TextView)findViewById(R.id.tv_driver_name)).setText(return1.getString("name"));
                    ((TextView)findViewById(R.id.tv_license)).setText(return1.getString("license"));
                    driver_tel = return1.getString("driver_tel");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Picasso.with(getApplicationContext()).load(getString(R.string.website_url) + "get_driver_pic.php?tel=" + driver_tel).transform(new CircleTransform()).into((ImageView) findViewById(R.id.driver_image));
                findViewById(R.id.driverLoading).setVisibility(View.INVISIBLE);

            }
        }.execute();

    }

    public void submitComment(View v){
        loadingDialog.show();

        final String comment = ((EditText)findViewById(R.id.et_comment)).getText().toString();

        new AsyncTask<String,Void,Boolean>(){
            String errorMsg="";
            @Override
            protected Boolean doInBackground(String... params) {

                try {
                    URL u = new URL(getString(R.string.website_url) + "/submit_rating.php?job_code=" +job_code + "&rating=" +rating + "&comment=" + comment);
                    HttpURLConnection h = (HttpURLConnection)u.openConnection();
                    h.setRequestMethod("GET");
                    h.setDoOutput(true);
                    h.connect();

                    int response = h.getResponseCode();
                    if (response == 200) {
                        return true;
                    }

                } catch (MalformedURLException e) {
                    Log.e("UpdateLocTask", "URL Error");
                    errorMsg = "URL Error";
                } catch (IOException e) {
                    Log.e("UpdateLocTask", "No internet connection, Retry soon");
                    MainActivity.makeToast("กรุณาเชื่อมต่ออินเตอร์เน็ต",getApplicationContext());
                }

                return false;
            }

            @Override
            protected void onPostExecute(Boolean result){
                loadingDialog.dismiss();

                if(result) {
                    mydb.doneComment(job_code);
                    MainActivity.comment_success = true;
                    finish();
                }
            }
        }.execute();

    }

    public void giveStar(View v){
        ((ImageView)findViewById(R.id.star1)).setImageDrawable(getResources().getDrawable(R.drawable.star_blank));
        ((ImageView)findViewById(R.id.star2)).setImageDrawable(getResources().getDrawable(R.drawable.star_blank));
        ((ImageView)findViewById(R.id.star3)).setImageDrawable(getResources().getDrawable(R.drawable.star_blank));
        ((ImageView)findViewById(R.id.star4)).setImageDrawable(getResources().getDrawable(R.drawable.star_blank));
        ((ImageView)findViewById(R.id.star5)).setImageDrawable(getResources().getDrawable(R.drawable.star_blank));
        rating = 0;
        switch (v.getId()){
            case R.id.star5:
                ((ImageView)findViewById(R.id.star5)).setImageDrawable(getResources().getDrawable(R.drawable.star));
                rating++;
            case R.id.star4:
                ((ImageView)findViewById(R.id.star4)).setImageDrawable(getResources().getDrawable(R.drawable.star));
                rating++;
            case R.id.star3:
                ((ImageView)findViewById(R.id.star3)).setImageDrawable(getResources().getDrawable(R.drawable.star));
                rating++;
            case R.id.star2:
                ((ImageView)findViewById(R.id.star2)).setImageDrawable(getResources().getDrawable(R.drawable.star));
                rating++;
            case R.id.star1:
                ((ImageView)findViewById(R.id.star1)).setImageDrawable(getResources().getDrawable(R.drawable.star));
                rating++;
                break;
        }

        findViewById(R.id.submitComment).setEnabled(true);
    }

    @Override

    public void onBackPressed() {
        return;
    }

    @Override
    public void onPause(){
        super.onPause();
        finish();
    }
}
