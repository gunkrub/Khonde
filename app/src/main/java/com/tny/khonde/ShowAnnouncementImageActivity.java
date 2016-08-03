package com.tny.khonde;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

public class ShowAnnouncementImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_announcement_image);

        ((TouchImageView)findViewById(R.id.shownImage)).setImageBitmap(AnnouncementActivity.currentImage);
    }
}
