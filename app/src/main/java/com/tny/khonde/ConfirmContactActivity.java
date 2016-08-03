package com.tny.khonde;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.Preference;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.ClientProtocolException;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.message.BasicNameValuePair;
import cz.msebera.android.httpclient.protocol.HTTP;

public class ConfirmContactActivity extends AppCompatActivity {
    static String remark = "";
    int current_contact_index=0;

    @Override
    public void onResume(){
        super.onResume();
        if(MainActivity.job==null)
            finish();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_contact);

        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy kk:mm");

        ((TextView)findViewById(R.id.textview_pickup_datetime)).setText("วันที่รับของ " + dateFormatter.format(MainActivity.pickupDate.getTime()));
        ((EditText)findViewById(R.id.edittext_remark)).setText(MainActivity.job.remark);

        LinearLayout layout = (LinearLayout) findViewById(R.id.layout_contacts);

        //CONTACTS
        for(int i = 0; i< MainActivity.job.jobLocations.size(); i++){
            View location1 = View.inflate(this,R.layout.layout_contact,null);

            if(i>0){
                if(i< MainActivity.job.jobLocations.size()-1){
                    ((TextView)location1.findViewById(R.id.location_type_label)).setText("");
                    ((ImageView)location1.findViewById(R.id.location_type_image)).setImageDrawable(getResources().getDrawable(R.drawable.circle_dark_orange));
                } else {
                    ((TextView)location1.findViewById(R.id.location_type_label)).setText("ส่ง");
                    ((ImageView)location1.findViewById(R.id.location_type_image)).setImageDrawable(getResources().getDrawable(R.drawable.circle_blue));
                }
            }

            ((TextView)location1.findViewById(R.id.address_label)).setText(MainActivity.job.jobLocations.get(i).desc);
            ((EditText)location1.findViewById(R.id.edittext_name)).setText(MainActivity.job.jobLocations.get(i).contact_name);
            ((EditText) location1.findViewById(R.id.edittext_name)).addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    checkAllContacts();
                }
            });

            ((EditText)location1.findViewById(R.id.edittext_tel)).addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    checkAllContacts();
                }
            });
            ((EditText)location1.findViewById(R.id.edittext_tel)).setText(MainActivity.job.jobLocations.get(i).contact_tel);

            final int index2 = i;
            location1.findViewById(R.id.my_info_layout).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    useMyInfo(index2);
                }
            });

            location1.findViewById(R.id.from_contact_layout).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startContactIntent(index2);
                }
            });
            layout.addView(location1);
        }
    }

    public void startAuctionActivity(View v){
        LinearLayout layout = (LinearLayout) findViewById(R.id.layout_contacts);

        for(int i=0;i<layout.getChildCount();i++){
            EditText et1 = ((EditText) layout.getChildAt(i).findViewById(R.id.edittext_name));
            EditText et2 = ((EditText) layout.getChildAt(i).findViewById(R.id.edittext_tel));

            String contact_name = et1.getText().toString();
            String contact_tel = et2.getText().toString();

            MainActivity.job.jobLocations.get(i).contact_name = contact_name;
            MainActivity.job.jobLocations.get(i).contact_tel = contact_tel;
        }

        remark = ((EditText)findViewById(R.id.edittext_remark)).getText().toString();
        MainActivity.job.remark = remark;

        Intent intent = new Intent(this,StartAuctionActivity.class);
        startActivityForResult(intent,5000);
    }

    public void startContactIntent(int index){
        current_contact_index = index;
        Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 1337);

        startActivityForResult(contactPickerIntent, 1);
    }

    public void useMyInfo(int index){
        current_contact_index = index;

        LinearLayout layout = (LinearLayout) findViewById(R.id.layout_contacts);

        EditText et1 = ((EditText) layout.getChildAt(current_contact_index).findViewById(R.id.edittext_name));
        EditText et2 = ((EditText) layout.getChildAt(current_contact_index).findViewById(R.id.edittext_tel));

        SharedPreferences preferences = getSharedPreferences("UserInfo",MODE_PRIVATE);
        String nameContact = preferences.getString("firstname","");
        String cNumber = preferences.getString("tel","");
        et1.setText(nameContact);
        et2.setText(cNumber);
    }

    public void checkAllContacts(){
        LinearLayout layout = (LinearLayout)findViewById(R.id.layout_contacts);

        for(int i=0;i<layout.getChildCount();i++){
            if(((EditText)layout.getChildAt(i).findViewById(R.id.edittext_name)).getText().toString().equals("")||
                    ((EditText)layout.getChildAt(i).findViewById(R.id.edittext_tel)).getText().toString().equals("")){
                findViewById(R.id.startAuctionButton).setEnabled(false);
                return;
            }
        }

        findViewById(R.id.startAuctionButton).setEnabled(true);
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data){
        super.onActivityResult(reqCode, resultCode, data);

         if (resultCode == RESULT_OK && reqCode == 5000) {
                Intent res = new Intent();
                setResult(RESULT_OK, res);
                finish();
            }

        switch(reqCode)
        {
            case (1):
                if (resultCode == Activity.RESULT_OK)
                {
                    Uri contactData = data.getData();
                    Cursor c = managedQuery(contactData, null, null, null, null);
                    if (c.moveToFirst())
                    {
                        String id = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));

                        String hasPhone = c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

                        if (hasPhone.equalsIgnoreCase("1"))
                        {
                            Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ id,null, null);
                            phones.moveToFirst();
                            String cNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            String nameContact = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));

                            LinearLayout layout = (LinearLayout) findViewById(R.id.layout_contacts);

                            EditText et1 = ((EditText) layout.getChildAt(current_contact_index).findViewById(R.id.edittext_name));
                            EditText et2 = ((EditText) layout.getChildAt(current_contact_index).findViewById(R.id.edittext_tel));

                            et1.setText(nameContact);
                            et2.setText(cNumber);
                            //Log.e("NUMBER",cNumber);
                            //Log.e("NAME",nameContact);
                        }
                    }
                }
        }
    }


}
