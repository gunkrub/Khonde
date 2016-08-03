package com.tny.khonde;

/**
 * Created by Thitiphat on 9/17/2015.
 */
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;

/**
 * Created by hp1 on 21-01-2015.
 */
public class Tab4 extends Fragment {
    static Bitmap croppedImage;
    static boolean updatedImage = false;
    View v;
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v =inflater.inflate(R.layout.tab_4,container,false);

        SharedPreferences preferences = getContext().getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        String tel = preferences.getString("tel","");

        final View v2 = v;
        Picasso.with(getContext()).load(getString(R.string.website_url) + "get_picture_customer.php?tel=" + tel).transform(new CircleTransform()).into((ImageView) v.findViewById(R.id.user_image),
                new Callback() {
                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onError() {

                    }
                });
        return v;
    }

    @Override
    public void onResume(){
        super.onResume();

        SharedPreferences preferences = getContext().getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        String tel = preferences.getString("tel","");

        if(updatedImage){
            Picasso.with(getContext())
                    .load(getString(R.string.website_url) + "get_picture_customer.php?tel=" + tel)
                    .memoryPolicy(MemoryPolicy.NO_CACHE )
                    .networkPolicy(NetworkPolicy.NO_CACHE)
                    .transform(new CircleTransform())
                    .into((ImageView) v.findViewById(R.id.user_image),
                    new Callback() {
                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onError() {

                        }
                    });
            ((ImageView)v.findViewById(R.id.user_image)).setImageBitmap(croppedImage);
            updatedImage = false;
        }

    }

}