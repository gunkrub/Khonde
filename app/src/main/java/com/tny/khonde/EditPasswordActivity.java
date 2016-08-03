package com.tny.khonde;

import android.app.ProgressDialog;
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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class EditPasswordActivity extends AppCompatActivity {
    ProgressDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_password);

        loadingDialog = new ProgressDialog(this);
        loadingDialog.setMessage("Please wait...");
        loadingDialog.setCancelable(false);

        ((EditText)findViewById(R.id.et_old_password))
                .addTextChangedListener(
                        new TextWatcher() {
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
                        }
                );

        ((EditText)findViewById(R.id.et_password))
                .addTextChangedListener(
                        new TextWatcher() {
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
                        }
                );

        ((EditText)findViewById(R.id.et_password2))
                .addTextChangedListener(
                        new TextWatcher() {
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
                        }
                );
    }

    public void validateInputs(){
        if(((EditText)findViewById(R.id.et_old_password)).getText().toString().equals("") || ((EditText)findViewById(R.id.et_password)).getText().toString().equals("") || ((EditText)findViewById(R.id.et_password2)).getText().toString().equals("")){
            findViewById(R.id.savePasswordButton).setEnabled(false);
        } else {
            findViewById(R.id.savePasswordButton).setEnabled(true);
        }
    }

    public void savePassword(View v){
        final String old_password = ((TextView)findViewById(R.id.et_old_password)).getText().toString();
        final String password = ((TextView)findViewById(R.id.et_password)).getText().toString();
        String password2 = ((TextView)findViewById(R.id.et_password2)).getText().toString();

        if(!password.equals(password2)){
            MainActivity.makeToast("รหัสผ่านไม่ตรงกัน",getApplicationContext());
            return;
        }

        loadingDialog.show();
        new AsyncTask<String, Void, Boolean>() {
            String errorMsg="";
            JSONObject return1;
            @Override
            protected Boolean doInBackground(String... params) {
                BufferedReader reader;
                StringBuilder buffer = new StringBuilder();
                String line;
                SharedPreferences preferences = getSharedPreferences("UserInfo",MODE_PRIVATE);
                try {
                    URL u = new URL(getString(R.string.website_url) + "/edit_password.php?tel="+ preferences.getString("tel","") + "&old_password=" + old_password + "&password=" + password);
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

                        if(return1.getBoolean("success")){
                            return true;
                        } else {
                            errorMsg = return1.getString("errorMsg");
                            return false;
                        }

                    }
                    else {
                        errorMsg = "HTTP Error";
                    }
                } catch (MalformedURLException e) {
                    Log.e("UpdateLocTask", "URL Error");
                    errorMsg = "URL Error";
                } catch (IOException e) {
                    Log.e("UpdateLocTask", getString(R.string.no_internet));
                    errorMsg = getString(R.string.no_internet);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                loadingDialog.hide();
                if(result){
                    MainActivity.makeToast("เปลี่ยนรหัสผ่านสำเร็จ",getApplicationContext());
                    finish();
                }else {
                    if(errorMsg.equals("incorrect_password"))
                        errorMsg = "รหัสผ่านปัจจุบันไม่ถูกต้อง";
                        MainActivity.makeToast(errorMsg,getApplicationContext());
                }
            }
        }.execute();
    }
}
