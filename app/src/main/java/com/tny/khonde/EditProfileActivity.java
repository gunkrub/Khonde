package com.tny.khonde;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;
import com.theartofdev.edmodo.cropper.CropImageHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
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

public class EditProfileActivity extends AppCompatActivity {
    static String encodedImage = "";
    String tel;
    ProgressDialog loadingDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        encodedImage = "";

        loadingDialog = new ProgressDialog(this);
        loadingDialog.setMessage("Please wait...");
        loadingDialog.setCancelable(false);

        SharedPreferences preferences = getSharedPreferences("UserInfo",MODE_PRIVATE);
        tel = preferences.getString("tel","");
        ((ImageView)findViewById(R.id.user_image)).setImageBitmap(ProfileActivity.user_image);

        ((TextView)findViewById(R.id.et_tel)).setText(preferences.getString("tel",""));
        ((TextView)findViewById(R.id.et_firstname)).setText(preferences.getString("firstname",""));
        ((TextView)findViewById(R.id.et_lastname)).setText(preferences.getString("lastname",""));
        ((TextView)findViewById(R.id.et_email)).setText(preferences.getString("email",""));

        ((TextView)findViewById(R.id.et_tel)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                validateInputs();
            }
        });
        ((TextView)findViewById(R.id.et_firstname)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                validateInputs();
            }
        });
        ((TextView)findViewById(R.id.et_lastname)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                validateInputs();
            }
        });
    }

    public void validateInputs(){
        if(((EditText)findViewById(R.id.et_firstname)).getText().toString().equals("") || ((EditText)findViewById(R.id.et_lastname)).getText().toString().equals("") || ((EditText)findViewById(R.id.et_tel)).getText().toString().equals("")){
            findViewById(R.id.edit_profileButton).setEnabled(false);
        } else {
            findViewById(R.id.edit_profileButton).setEnabled(true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImageHelper.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri imageUri = CropImageHelper.getPickImageResultUri(this, data);

            SelectCropImageActivity.imageToCrop = imageUri;

                Intent cropAct = new Intent(this, SelectCropImageActivity.class);
                startActivityForResult(cropAct, 1);


        } else if(requestCode == 1 && resultCode == RESULT_OK) {
            if(!encodedImage.equals("")) {
                CircleTransform circleTransform = new CircleTransform();
                Tab4.croppedImage = circleTransform.transform(Tab4.croppedImage);
                ((ImageView) findViewById(R.id.user_image)).setImageBitmap(Tab4.croppedImage);
            }
        }
    }

    public void loadImagefromGallery(View v){
        CropImageHelper.startPickImageActivity(this);
    }

    public void saveProfile(View v){
        new AsyncTask<String, Void, Boolean>() {
            String errorMsg = "";
            String line,firstname,lastname,email,new_tel;
            StringBuilder buffer = new StringBuilder();
            protected void onPreExecute() {
                loadingDialog.show();
                new_tel = ((TextView)findViewById(R.id.et_tel)).getText().toString();
                firstname = ((TextView)findViewById(R.id.et_firstname)).getText().toString();
                lastname = ((TextView)findViewById(R.id.et_lastname)).getText().toString();
                email = ((TextView)findViewById(R.id.et_email)).getText().toString();
            }

            @Override
            protected Boolean doInBackground(String... params) {

                List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
                nameValuePair.add(new BasicNameValuePair("firstname",firstname));
                nameValuePair.add(new BasicNameValuePair("lastname",lastname));
                nameValuePair.add(new BasicNameValuePair("email",email));
                nameValuePair.add(new BasicNameValuePair("new_tel",new_tel));
                nameValuePair.add(new BasicNameValuePair("picture",encodedImage));

                HttpClient httpClient = new DefaultHttpClient();
                // replace with your url
                HttpPost httpPost = new HttpPost(getString(R.string.website_url) + "/edit_profile.php?tel=" + tel);

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
                    SharedPreferences preferences = getSharedPreferences("UserInfo",MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("tel",new_tel);
                    editor.putString("email",email);
                    editor.putString("firstname",firstname);
                    editor.putString("lastname",lastname);
                    editor.commit();

                    MainActivity.makeToast("แก้ไขข้อมูลสำเร็จ",getApplicationContext());
                    Intent res = new Intent();
                    setResult(RESULT_OK, res);
                    finish();
                } else {
                    if(errorMsg.equals("tel_exist"))
                        errorMsg = "เบอร์มือถือซ้ำกับในระบบ";
                    MainActivity.makeToast(errorMsg,getApplicationContext());
                }
                loadingDialog.hide();

            }
        }.execute();


    }
}
