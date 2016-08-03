package com.tny.khonde;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {
    BroadcastReceiver myReceiver;
    IntentFilter intentfilter;
    static Bitmap user_image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        SharedPreferences preferences = getSharedPreferences("UserInfo",MODE_PRIVATE);
        String tel = preferences.getString("tel","");

        Picasso.with(this)
                .load(getString(R.string.website_url) + "get_picture_customer.php?tel=" + tel)
                .transform(new CircleTransform())
                .into((ImageView) findViewById(R.id.user_image),
                new Callback() {
                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onError() {

                    }
                });

    }

    @Override
    public void onResume(){
        super.onResume();

        SharedPreferences preferences = getSharedPreferences("UserInfo",MODE_PRIVATE);
        String tel = preferences.getString("tel","");

        ((TextView)findViewById(R.id.tv_tel)).setText(tel);
        ((TextView)findViewById(R.id.tv_firstname)).setText(preferences.getString("firstname",""));
        ((TextView)findViewById(R.id.tv_lastname)).setText(preferences.getString("lastname",""));
        ((TextView)findViewById(R.id.tv_email)).setText(preferences.getString("email",""));

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==1 && resultCode==RESULT_OK) {
            if(!Tab4.updatedImage) {
                Tab4.updatedImage = true;
                ((ImageView) findViewById(R.id.user_image)).setImageBitmap(Tab4.croppedImage);
                user_image = Tab4.croppedImage;
            }
        }
    }

    @Override
    public void onPause(){
        super.onPause();
    }

    public void startEditProfile(View v){
        Intent intent = new Intent(this,EditProfileActivity.class);
        (findViewById(R.id.user_image)).buildDrawingCache();
        user_image = (findViewById(R.id.user_image)).getDrawingCache();
        startActivityForResult(intent,1);
    }


    public void startEditPassword(View v){
        Intent intent = new Intent(this,EditPasswordActivity.class);
        startActivity(intent);
    }

    public void logout(View v){
        SharedPreferences preferences = getSharedPreferences("UserInfo",MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("tel","");
        editor.commit();

        Intent res = getIntent();
        setResult(RESULT_OK,res);
        finish();
    }
}
