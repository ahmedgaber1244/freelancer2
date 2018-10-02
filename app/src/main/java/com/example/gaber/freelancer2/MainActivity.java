package com.example.gaber.freelancer2;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.devlomi.record_view.OnBasketAnimationEnd;
import com.devlomi.record_view.OnRecordClickListener;
import com.devlomi.record_view.OnRecordListener;
import com.devlomi.record_view.RecordButton;
import com.devlomi.record_view.RecordView;
import com.fxn.pix.Pix;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity  {
    DrawerLayout fullView;
    Toolbar toolbarTop ;
    private friends_list_adapter data_adapter;
    public List<friend_data_model> friends_list = new ArrayList<>();
    public RecyclerView friends_recyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



    }
    @Override
    public void setContentView(int layoutResID) {

        fullView = (DrawerLayout) getLayoutInflater().inflate(R.layout.activity_main, null);
        FrameLayout activityContainer = (FrameLayout) fullView.findViewById(R.id.content_frame);
        getLayoutInflater().inflate(layoutResID, activityContainer, true);
        toolbarTop = (Toolbar) fullView.findViewById(R.id.toolbar_top);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, fullView, toolbarTop, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        fullView.addDrawerListener(toggle);
        toggle.syncState();

        super.setContentView(fullView);


        friends_recyclerView = findViewById(R.id.friends_recycler);
        data_adapter = new friends_list_adapter(this, friends_list);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        friends_recyclerView.setLayoutManager(mLayoutManager);
        friends_recyclerView.setItemAnimator(new DefaultItemAnimator());
        friends_recyclerView.addItemDecoration(new MyDividerItemDecoration(this, LinearLayoutManager.VERTICAL, 5));
        friends_recyclerView.setAdapter(data_adapter);


        find_friends(getSharedPreferences("logged_in",MODE_PRIVATE).getString("name",""));

        Thread t = new Thread() {

            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(5000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                            data_adapter.notifyDataSetChanged();
                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };

        t.start();





    }


    private void log_out()
    {
        SharedPreferences.Editor editor = getSharedPreferences("logged_in", MODE_PRIVATE).edit();
        editor.putBoolean("state",false);
        editor.apply();
        finish();
    }


    public void logout(View view) {
        log_out();
    }
    private void find_friends(String s)
    {

         FirebaseDatabase.getInstance().getReference().child("friends_list").child(s)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        friends_list.clear();
                        if (dataSnapshot.hasChildren()) {

                            for (DataSnapshot sub_type : dataSnapshot.getChildren()) {
                                String name=sub_type.child("name").getValue(String.class);
                                String image=sub_type.child("image_url").getValue(String.class);
                                String token=sub_type.child("token").getValue(String.class);
                                friends_list.add(new friend_data_model(name,image,token));

                            }
                            data_adapter.notifyDataSetChanged();
                        }else {
                            Toast.makeText(getApplicationContext(),"no such user",Toast.LENGTH_LONG).show();

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


    }

    public void gotosettings(View view) {
        Intent settings=new Intent(this, settings.class);
        finish();
        startActivity(settings);
    }

    public void goto_add_friend(View view) {
        Intent add_friend=new Intent(this, add_friend.class);
        finish();
        startActivity(add_friend);
    }
}
