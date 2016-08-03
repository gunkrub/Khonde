package com.tny.khonde;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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

public class AnnouncementActivity extends AppCompatActivity {

    Context context;
    static Bitmap currentImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announcement);

        context = this;

        new AsyncTask<String, Void, Boolean>() {
            String errorMsg = "";
            JSONObject announcement;

            @Override
            protected void onPreExecute() {

            }

            @Override
            protected Boolean doInBackground(String... params) {
                BufferedReader reader;
                StringBuilder buffer = new StringBuilder();
                String line;
                try {
                    URL u = new URL(getString(R.string.website_url) + "get_announcement_customer.php");
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

                        Log.e("RETURN",buffer.toString());
                        announcement = new JSONObject(buffer.toString());

                        boolean emptyResult = announcement.getBoolean("emptyResult");

                        if(emptyResult) {
                            errorMsg = "ไม่มีประกาศ";
                            return false;
                        }

                        return true;

                    }
                    else {
                        Log.e("", "HTTP Error");
                    }
                } catch (MalformedURLException e) {
                    Log.e("", "URL Error");
                } catch (IOException e) {
                    Log.e("", "กรุณาเชื่อมต่ออินเตอร์เน็ต");
                    errorMsg = "กรุณาเชื่อมต่ออินเตอร์เน็ต";
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if(result) {
                    try {

                        LinearLayout layout = (LinearLayout) findViewById(R.id.announcement_layout);

                        JSONArray announces = announcement.getJSONArray("announcement");

                    /* Announcements                                          */

                        for(int i=0;i<announces.length();i++){
                            final JSONObject announce_detail = announces.getJSONObject(i);

                            View announce = View.inflate(context, R.layout.announcement_card, null);

                            TextView tv2 = (TextView) announce.findViewById(R.id.tv_content);
                            tv2.setText(announce_detail.getString("content"));

                            TextView tv1 = (TextView) announce.findViewById(R.id.tv_title);
                            tv1.setText(announce_detail.getString("title"));

                            if(announce_detail.getBoolean("has_picture")) {

                                Picasso.with(getApplicationContext()).load(getString(R.string.website_url) + "get_announcement_picture_customer.php?announce_id=" + announce_detail.getString("announce_id")).into((ImageView) announce.findViewById(R.id.announcement_image));
                                final View announce_final = announce;
                                (announce.findViewById(R.id.announcement_image)).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        ((ImageView) announce_final.findViewById(R.id.announcement_image)).buildDrawingCache();
                                        currentImage = ((ImageView) announce_final.findViewById(R.id.announcement_image)).getDrawingCache();
                                        Intent intent = new Intent(getApplicationContext(),ShowAnnouncementImageActivity.class);
                                        startActivity(intent);

                                    }
                                });
                            }

                            layout.addView(announce);

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {

                    makeToast(errorMsg);
                }
                findViewById(R.id.announcementLoading).setVisibility(View.GONE);


            }
        }.execute();
    }



    public void makeToast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

}
