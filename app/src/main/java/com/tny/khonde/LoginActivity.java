package com.tny.khonde;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

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

public class LoginActivity extends AppCompatActivity {
    ProgressDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ((EditText)findViewById(R.id.et_tel)).addTextChangedListener(new TextWatcher() {
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
        ((EditText)findViewById(R.id.et_password)).addTextChangedListener(new TextWatcher() {
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


        loadingDialog = new ProgressDialog(this);
        loadingDialog.setMessage("Please wait...");
        loadingDialog.setCancelable(false);
    }

    public void login(View v){
        final String password = ((EditText)findViewById(R.id.et_password)).getText().toString();
        final String tel = ((EditText)findViewById(R.id.et_tel)).getText().toString();

        new AsyncTask<String, Void, Boolean>() {
            String errorMsg = "";
            String line;
            StringBuilder buffer = new StringBuilder();
            protected void onPreExecute() {
                loadingDialog.show();
            }

            @Override
            protected Boolean doInBackground(String... params) {

                List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
                nameValuePair.add(new BasicNameValuePair("tel",tel));
                nameValuePair.add(new BasicNameValuePair("password",password));

                HttpClient httpClient = new DefaultHttpClient();
                // replace with your url
                HttpPost httpPost = new HttpPost(getString(R.string.website_url) + "/login.php");

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
                    editor.putString("tel",tel);
                    editor.commit();

                    Tab4.updatedImage = true;
                    Intent res = new Intent();
                    setResult(RESULT_OK, res);
                    finish();
                } else {
                    MainActivity.makeToast("เบอร์มือถือ/รหัสผ่านไม่ถูกต้อง",getApplicationContext());
                }
                loadingDialog.hide();

            }
        }.execute();

    }

    public void validateInputs(){
        if( ((EditText)findViewById(R.id.et_tel)).getText().toString().equals("") || ((EditText)findViewById(R.id.et_password)).getText().toString().equals("")){
            findViewById(R.id.loginButton).setEnabled(false);
        } else {
            findViewById(R.id.loginButton).setEnabled(true);
        }
    }
}
