package com.example.gaber.freelancer2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class settings extends MainActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent back=new Intent(this,MainActivity.class);
        finish();
        startActivity(back);
    }
}
