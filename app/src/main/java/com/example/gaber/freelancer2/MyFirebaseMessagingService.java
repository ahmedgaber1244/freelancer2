package com.example.gaber.freelancer2;

import android.app.ActivityManager;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.spec.ECField;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Random;

import static android.content.ContentValues.TAG;

/**
 * Created by gaber on 26/08/2018.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private NotificationManager notifManager;
    private NotificationChannel mChannel;
    private database_operations db;
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            db = new database_operations(getApplicationContext());

                find_user(remoteMessage);


        }
    }


    private void notification(String message,String from_user_token){

        Intent intent;
        PendingIntent pendingIntent;
        NotificationCompat.Builder builder;
        if (notifManager == null) {
            notifManager = (NotificationManager) getSystemService
                    (Context.NOTIFICATION_SERVICE);
        }

        intent = new Intent (this, MainActivity.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            if (mChannel == null) {
                NotificationChannel mChannel = new NotificationChannel
                        ("0",from_user_token,importance);
                mChannel.setDescription (message);
                mChannel.enableVibration (true);
                mChannel.setVibrationPattern (new long[]
                        {100, 200, 300, 400, 500, 400, 300, 200, 400});
                notifManager.createNotificationChannel (mChannel);
            }
            builder = new NotificationCompat.Builder (this,"0");

            intent.setFlags (Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_SINGLE_TOP);
            pendingIntent = PendingIntent.getActivity (this, 0, intent, 0);
            builder.setContentTitle (from_user_token)  // flare_icon_30
                    .setSmallIcon (R.drawable.common_google_signin_btn_icon_light) // required
                    .setContentText (message)  // required
                    .setDefaults (Notification.DEFAULT_ALL)
                    .setAutoCancel (true)
                    .setContentIntent (pendingIntent)
                    .setSound (RingtoneManager.getDefaultUri
                            (RingtoneManager.TYPE_NOTIFICATION))
                    .setVibrate (new long[]{100, 200, 300, 400,
                            500, 400, 300, 200, 400});
        } else {

            builder = new NotificationCompat.Builder (this);


            Uri sound= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            builder.setSmallIcon(R.drawable.common_google_signin_btn_icon_light);
            builder.setContentTitle(from_user_token);
            builder.setContentText(message);
            builder.setColor((getResources().getColor(R.color.white)));
            builder.setSound(sound);
            builder.setVibrate (new long[]{100, 200, 300, 400,
                    500, 400, 300, 200, 400});
            Intent resultIntent = new Intent(this, MainActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addParentStack(MainActivity.class);

// Adds the Intent that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(resultPendingIntent);

// notificationID allows you to update the notification later on.


        } // else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Notification notification = builder.build ();
        int id = (int) System.currentTimeMillis();
        notifManager.notify (id, notification);

    }
    private void find_user(RemoteMessage remoteMessage){
        Map<String,String> data=remoteMessage.getData();
        final String from_user_token =  data.get("from_user_token");
        final String message =  data.get("message");
        final String storage_url =  data.get("storage_url");
        final String type =  data.get("type");
        final String time =  data.get("time");
        final String refreshedtoken= FirebaseInstanceId.getInstance().getToken();

        DatabaseReference mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference("users");
        Query query = mFirebaseDatabaseReference.orderByChild("token").equalTo(from_user_token);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                    String from_name=dataSnapshot1.getValue(user_data_model.class).name;
                    notification(message,from_name);
                    db.insert_data_model(from_user_token,refreshedtoken,message,type,time,storage_url);

                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}

