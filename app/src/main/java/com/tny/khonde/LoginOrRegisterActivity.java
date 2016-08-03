package com.tny.khonde;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class LoginOrRegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_or_register);
    }

    public void startRegister(View v){
        Intent intent = new Intent(this,RegisterActivity.class);
        startActivityForResult(intent,2);
    }

    public void startLogin(View v){
        Intent intent = new Intent(this,LoginActivity.class);
        startActivityForResult(intent,2);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==2 && resultCode==RESULT_OK) {
            Intent res = new Intent();
            setResult(RESULT_OK,res);
            finish();
        }
    }

}
