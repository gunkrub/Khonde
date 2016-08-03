package com.tny.khonde;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.theartofdev.edmodo.cropper.CropImageView;

public class SelectCropImageActivity extends AppCompatActivity {

    static Uri imageToCrop;

    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_crop_image);

        CropImageView c1 = (CropImageView)findViewById(R.id.cropImageView);
        c1.setImageUriAsync(imageToCrop);
        c1.setFixedAspectRatio(true);
        c1.setAspectRatio(1, 1);

    }

    public void gotoConfirmImage(View view){
        CropImageView c1 = (CropImageView)findViewById(R.id.cropImageView);
        ConfirmCropImage.croppedImage = c1.getCroppedImage();

        Intent intent = new Intent(this,ConfirmCropImage.class);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode==1 && resultCode==RESULT_OK){
            Intent res = new Intent();
            setResult(RESULT_OK,res);
            finish();
        }
    }
}
