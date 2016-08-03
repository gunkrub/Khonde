package com.tny.khonde;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;

public class ConfirmCropImage extends AppCompatActivity {
    static Bitmap croppedImage;
    static Uri croppedImageUri;
    Bitmap returnbitmap;
    BitmapFactory.Options options = null;
    ByteArrayOutputStream stream;
    ProgressDialog prgDialog;
    byte[] byte_arr = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_crop_image);

        ((ImageView) findViewById(R.id.image_croppedImage)).setImageBitmap(croppedImage);

        prgDialog = new ProgressDialog(this);
        prgDialog.setCancelable(false);
        prgDialog.setMessage("Please Wait...");

    }

    public void confirmCroppedImage(View v){
        new AsyncTask<Void, Void, String>() {
            protected void onPreExecute() {}

            @Override
            protected String doInBackground(Void... params) {

                stream = new ByteArrayOutputStream();
                Tab4.croppedImage = compressImage(300.0);
                byte_arr = stream.toByteArray();
                EditProfileActivity.encodedImage = Base64.encodeToString(byte_arr, 0);

                return "";
            }

            @Override
            protected void onPostExecute(String msg) {
                prgDialog.dismiss();
                Intent res = new Intent();
                setResult(RESULT_OK, res);
                finish();

            }
        }.execute(null, null, null);
    }


    public Bitmap compressImage(double maxLength){
        options = new BitmapFactory.Options();
        options.inSampleSize = 3;

        int width = croppedImage.getWidth();
        int height = croppedImage.getHeight();
        float ratio;

        if(width > height) {
            if(croppedImage.getWidth()<maxLength){
                maxLength = croppedImage.getWidth();
            }
            ratio = (float) (width / maxLength);
            height = Math.round(height / ratio);
            width = (int)maxLength;
        } else {
            if(croppedImage.getHeight()<maxLength){
                maxLength = croppedImage.getHeight();
            }
            ratio = (float) (height /maxLength);
            width = Math.round(width / ratio);
            height = (int)maxLength;
        }

        returnbitmap = Bitmap.createScaledBitmap(croppedImage,width,height,false);
        returnbitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);

        return returnbitmap;
    }
}
