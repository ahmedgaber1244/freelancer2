package com.example.gaber.freelancer2;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by gaber on 12/08/2018.
 */

public class data_adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

private Context context;
private List<data_model> datalist;
private static MediaPlayer mediaPlayer;
public String my_user=null;


    public class MyViewHolder_message extends RecyclerView.ViewHolder {
        public TextView name,message_data;
        public LinearLayout message_layout;
        public MyViewHolder_message(View view) {
        super(view);
        name=(TextView) view.findViewById(R.id.name);
        message_data =(TextView) view.findViewById(R.id.message_context);
            message_layout =(LinearLayout) view.findViewById(R.id.message_data);


    }
}

    public class MyViewHolder_record extends RecyclerView.ViewHolder {
        public TextView name;
        public SeekBar progressBar;
        public ImageView play;
        public LinearLayout record_layout;
        public MyViewHolder_record(View view) {
            super(view);
            name=(TextView) view.findViewById(R.id.name);
            progressBar =(SeekBar) view.findViewById(R.id.progressBar);
            play =(ImageView) view.findViewById(R.id.play);
            record_layout =(LinearLayout) view.findViewById(R.id.record_data);



        }
    }

    public class MyViewHolder_image extends RecyclerView.ViewHolder {
        public TextView name;
        public ImageView imageView;
        public LinearLayout image_layout;
        public MyViewHolder_image(View view) {
            super(view);
            name=(TextView) view.findViewById(R.id.name);
            imageView =(ImageView) view.findViewById(R.id.sended_image);
            image_layout =(LinearLayout) view.findViewById(R.id.image_data);


        }
    }

    public data_adapter(Context context, List<data_model> datalist) {
        this.context = context;
        this.datalist = datalist;
        my_user= FirebaseInstanceId.getInstance().getToken();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType==0) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.message_item, parent, false);
            return new MyViewHolder_message(itemView);
        }else if (viewType==1){
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.message_item_right, parent, false);
            return new MyViewHolder_message(itemView);
        }else if (viewType==2){
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.record_item, parent, false);
            return new MyViewHolder_record(itemView);
        }else if (viewType==3){
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.record_item_righ, parent, false);
            return new MyViewHolder_record(itemView);
        }else if (viewType==4){
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.image_item, parent, false);
            return new MyViewHolder_image(itemView);
        }else if (viewType==5){
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.image_item_right, parent, false);
            return new MyViewHolder_image(itemView);
        }
        return null;

    }



    @Override
    public int getItemViewType(int position) {
        if (datalist.get(position).type.contains("text")){
             if (datalist.get(position).from.equals(my_user)) {
                 return 0;

            }else {
                return 1;
            }
        }else if (datalist.get(position).type.contains("record")){
            if (datalist.get(position).from.equals(my_user)) {

                return 2;

            }else {

                return 3;
            }
        }else if (datalist.get(position).type.contains("image")){
            if (datalist.get(position).from.equals(my_user)) {
                  return 4;

            }else {
                return 5;
            }
        }else {
            return 0;
        }

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final data_model data = datalist.get(position);
        find_user(data.from,holder,data);


    }

    @Override
    public int getItemCount() {
        return datalist.size();
    }




    private void find_user(String s, final RecyclerView.ViewHolder holder, final data_model data){
        DatabaseReference mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference("users");
        Query query = mFirebaseDatabaseReference.orderByChild("token").equalTo(s.toString());

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                    String from_name=dataSnapshot1.getValue(user_data_model.class).name;
                    set_view(holder,data,from_name);

                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private void set_view(final RecyclerView.ViewHolder holder, final data_model data,String from_name){
        if (holder.getItemViewType()==0||holder.getItemViewType()==1){
            MyViewHolder_message message=(MyViewHolder_message)holder;
            message.name.setText(from_name);
            message.message_data.setText(data.message);

        }else if(holder.getItemViewType()==2||holder.getItemViewType()==3){
            final MyViewHolder_record message=(MyViewHolder_record)holder;
            message.name.setText(from_name);
            message.play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    try {
                        mediaPlayer = new MediaPlayer();
                        mediaPlayer.setDataSource(data.storage_url);
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                        message.progressBar.setMax(mediaPlayer.getDuration());
                        message.play.setImageResource(R.drawable.ic_pause_circle_outline_black_24dp);


                        Timer timer = new Timer();
                        timer.scheduleAtFixedRate(new TimerTask() {
                            @Override
                            public void run() {
                               message.progressBar.setProgress(mediaPlayer.getCurrentPosition());


                            }
                        },0,1000);
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (mediaPlayer.isPlaying()){
                                    message.play.setImageResource(R.drawable.ic_pause_circle_outline_black_24dp);
                                }else {
                                    message.play.setImageResource(R.drawable.ic_play_circle_outline_black_24dp);
                                }
                            }
                        }, mediaPlayer.getDuration());

                    }catch (Exception e){
                        e.printStackTrace();
                    }


                }
            });
            message.progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if(mediaPlayer != null && fromUser){
                        mediaPlayer.seekTo(progress * 1000);
                    }
                }
            });
        }else if (holder.getItemViewType()==4||holder.getItemViewType()==5){
            MyViewHolder_image message=(MyViewHolder_image)holder;
            message.name.setText(from_name);
            Picasso.with(context)
                    .load(data.storage_url)
                    .placeholder(R.mipmap.ic_launcher).fit().into(message.imageView, new Callback() {
                @Override
                public void onSuccess() {}
                @Override public void onError() {
                    Toast.makeText(context,"error loading image",Toast.LENGTH_LONG).show();
                }
            });
        }
    }


}


