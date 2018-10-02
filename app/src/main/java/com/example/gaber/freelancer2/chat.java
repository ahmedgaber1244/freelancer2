package com.example.gaber.freelancer2;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.devlomi.record_view.OnRecordClickListener;
import com.devlomi.record_view.OnRecordListener;
import com.devlomi.record_view.RecordButton;
import com.devlomi.record_view.RecordView;
import com.fxn.pix.Pix;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class chat extends MainActivity  {
    private static MediaRecorder mediaRecorder;
    private static MediaPlayer mediaPlayer;
    private static String audioFilePath;
    private boolean isRecording = false;
    private TextView user_id ;
    private EditText textmessage ;
    private ImageView choose_images,Choose_files,user_image ;
    private data_adapter data_adapter;
    private List<data_model> data_model_list = new ArrayList<>();
    private RecyclerView data_recyclerView;
    private StorageReference mStorageRef;
    private RequestQueue queue;
    String my_user_id,to_user_id;
    FirebaseDatabase database;
    database_operations db;
    int PICK_FILE = 1;
    int PICK_IMAGE_MULTIPLE = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        user_id=(TextView)findViewById(R.id.user_id);
        textmessage=(EditText)findViewById(R.id.textmessage);
        choose_images=(ImageView) findViewById(R.id.camera);
        Choose_files=(ImageView)findViewById(R.id.attachfiles);
        user_image=(ImageView)findViewById(R.id.user_image);
        final RecordView recordView = (RecordView) findViewById(R.id.record_view);
        final RecordButton recordButton = (RecordButton) findViewById(R.id.record_button);
        db=new database_operations(getApplicationContext());
        my_user_id= FirebaseInstanceId.getInstance().getToken();
        data_recyclerView = findViewById(R.id.main_recycler);
        data_adapter = new data_adapter(this, data_model_list);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        data_recyclerView.setLayoutManager(mLayoutManager);
        data_recyclerView.setItemAnimator(new DefaultItemAnimator());
        data_recyclerView.addItemDecoration(new MyDividerItemDecoration(this, LinearLayoutManager.VERTICAL, 5));
        data_recyclerView.setAdapter(data_adapter);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        database = FirebaseDatabase.getInstance();
        queue = Volley.newRequestQueue(this);
        friends_recyclerView.addOnItemTouchListener(new RecyclerTouchListener(this, data_recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                user_id.setText(friends_list.get(position).name);
                to_user_id=friends_list.get(position).token;
                Picasso.with(chat.this)
                        .load(friends_list.get(position).image)
                        .placeholder(R.mipmap.ic_launcher).transform(new PicassoCircleTransformation()).into(user_image, new Callback() {
                    @Override
                    public void onSuccess() {}
                    @Override public void onError() {
                        Toast.makeText(chat.this,"error loading image",Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onLongClick(View view, int position) {
            }
        }));



        //IMPORTANT
        recordButton.setRecordView(recordView);
        recordView.setOnRecordListener(new OnRecordListener() {
            @Override
            public void onStart() {
                //Start Recording..
                textmessage.setVisibility(View.GONE);
                choose_images.setVisibility(View.GONE);
                Choose_files.setVisibility(View.GONE);
                recordView.setVisibility(View.VISIBLE);
                audioFilePath = Environment.getExternalStorageDirectory().getAbsolutePath()
                        + "/"+rand_file_name();
                record();
                Log.d("RecordView", "onStart");
            }

            @Override
            public void onCancel() {
                //On Swipe To Cancel
                stopAudio();
                delete_record();
                textmessage.setVisibility(View.VISIBLE);
                choose_images.setVisibility(View.VISIBLE);
                Choose_files.setVisibility(View.VISIBLE);
                recordView.setVisibility(View.GONE);
                Log.d("RecordView", "onCancel");

            }

            @Override
            public void onFinish(long recordTime) {
                //Stop Recording..
                stopAudio();
                textmessage.setVisibility(View.VISIBLE);
                choose_images.setVisibility(View.VISIBLE);
                Choose_files.setVisibility(View.VISIBLE);
                recordView.setVisibility(View.GONE);
                upload_record(audioFilePath);
                Log.d("RecordView", "onFinish");

            }

            @Override
            public void onLessThanSecond() {
                //When the record time is less than One Second
                stopAudio();
                delete_record();
                textmessage.setVisibility(View.VISIBLE);
                choose_images.setVisibility(View.VISIBLE);
                Choose_files.setVisibility(View.VISIBLE);
                recordView.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(),"Record is too short",Toast.LENGTH_LONG).show();
                Log.d("RecordView", "onLessThanSecond");
            }
        });

        //ListenForRecord must be false ,otherwise onClick will not be called
        recordButton.setOnRecordClickListener(new OnRecordClickListener() {
            @Override
            public void onClick(View v) {

                send_message(my_user_id, to_user_id, textmessage.getText().toString(), "", "text");
                textmessage.setText("");

            }
        });




        textmessage.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if (s.length()>0) {
                    recordButton.setListenForRecord(false);

                }else {
                    recordButton.setListenForRecord(true);
                }
            }
        });
        choose_images.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                select_images();
            }
        });
        Choose_files.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                select_Documents();
            }
        });


    }

    private void record()
    {
        try {
            isRecording = true;
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setOutputFile(audioFilePath);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mediaRecorder.start();
    }
    private void stopAudio ( )
    {


        if (isRecording)
        {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
            isRecording = false;
        } else {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
    private void delete_record()
    {
        File file = new File(audioFilePath);
        boolean deleted = file.delete();
        if (deleted)
            getApplicationContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));

    }
    private String rand_file_name()
    {
        String alphabet= "abcdefghijklmnopqrstuvwxyz";
        String s = "";
        Random random = new Random();
        int randomLen = 1+random.nextInt(9);
        for (int i = 0; i < randomLen; i++) {
            char c = alphabet.charAt(random.nextInt(26));
            s+=c;
        }
        return s+".mp3";
    }
    private void upload_record(String audioFilePath)
    {
        Uri file = Uri.fromFile(new File(audioFilePath));
        final StorageReference ref = mStorageRef.child("record"+my_user_id+"/"+audioFilePath);
        final ProgressDialog progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Uploading");
        progressDialog.show();
        UploadTask uploadTask = ref.putFile(file);

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return ref.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    progressDialog.dismiss();
                    send_message(my_user_id,to_user_id,"", String.valueOf(downloadUri),"record");

                } else {
                    progressDialog.dismiss();
                }
            }

        });
        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress=(100*taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();
                progressDialog.setMessage(String.valueOf(progress)+"% Uploaded");
            }
        });



    }
    private void upload_image(String audioFilePath)
    {
        Uri file = Uri.fromFile(new File(audioFilePath));
        final ProgressDialog progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Uploading");
        progressDialog.show();
        final StorageReference ref = mStorageRef.child("image/"+my_user_id+"/"+audioFilePath);
        UploadTask uploadTask = ref.putFile(file);

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return ref.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    progressDialog.dismiss();
                    send_message(my_user_id,to_user_id,"", String.valueOf(downloadUri),"image");

                } else {
                    // Handle failures
                    // ...
                    progressDialog.dismiss();

                }
            }

        });
        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress=(100*taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();
                progressDialog.setMessage(String.valueOf(progress)+"% Uploaded");
            }
        });



    }
    private void upload_file(String audioFilePath)
    {
        Uri file = Uri.fromFile(new File(audioFilePath));
        final StorageReference ref = mStorageRef.child("file"+my_user_id+"/"+audioFilePath);
        final ProgressDialog progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Uploading");
        progressDialog.show();
        UploadTask uploadTask = ref.putFile(file);

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return ref.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    progressDialog.dismiss();
                    send_message(my_user_id,to_user_id,"", String.valueOf(downloadUri),"image");

                } else {
                    // Handle failures
                    // ...
                    progressDialog.dismiss();

                }
            }

        });
        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress=(100*taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();
                progressDialog.setMessage(String.valueOf(progress)+"% Uploaded");
            }
        });
    }
    private void send_message(final String from_user_token, final String to_user_token, final String message, final String storage_url, final String type) {

            final String date = new SimpleDateFormat("yyyy/MM/dd HH:mm aa", Locale.getDefault()).format(new Date());
            db.insert_data_model(from_user_token, to_user_token, message, type, date, storage_url);
            refresh();
            try {
                JSONObject main = new JSONObject();
                JSONObject data = new JSONObject();
                data.put("from_user_token", from_user_token);
                data.put("message", message);
                data.put("storage_url", storage_url);
                data.put("type", type);
                data.put("time", date);
                main.put("data", data);
                main.put("to", to_user_token);
                String url = "https://fcm.googleapis.com/fcm/send";
                if (queue == null) {
                    queue = Volley.newRequestQueue(this);
                }
                // Request a string response from the provided URL.
                JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, url, main,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {


                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                    }
                }) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("Content-Type", "application/json");
                        params.put("Authorization", "key=AAAA8WARYks:APA91bFTef8NfeRe5ZHwpndFqM1I9_w1RJi8AdA48UZnfJGHzARVbM2qd5x2URSp1CaUvk1mavbT3bXrlzUIh6eRso4XjRIU4PbS7TWAe0RYLo7e4OVc8dsQ602Nz7jzmL5l4eKGbxYT");

                        return params;
                    }
                };
                // Add the request to the RequestQueue.
                queue.add(stringRequest);


            } catch (Exception e) {

            }


    }
    private void select_Documents()
    {

        String[] mimeTypes =
                {"application/msword","application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .doc & .docx
                        "application/vnd.ms-powerpoint","application/vnd.openxmlformats-officedocument.presentationml.presentation", // .ppt & .pptx
                        "application/vnd.ms-excel","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xls & .xlsx
                        "text/plain",
                        "application/pdf",
                        "application/zip"};

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intent.setType(mimeTypes.length == 1 ? mimeTypes[0] : "*/*");
            if (mimeTypes.length > 0) {
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            }
        } else {
            String mimeTypesStr = "";
            for (String mimeType : mimeTypes) {
                mimeTypesStr += mimeType + "|";
            }
            intent.setType(mimeTypesStr.substring(0,mimeTypesStr.length() - 1));
        }
        startActivityForResult(Intent.createChooser(intent,"ChooseFile"), PICK_FILE);

    }
    private void select_images()
    {
        Pix.start(this,
                PICK_IMAGE_MULTIPLE,20);
    }


    private void reload()
    {
        data_model_list.clear();
        data_model_list.addAll(db.getAll_notification_model(my_user_id,to_user_id));
        data_adapter.notifyDataSetChanged();
    }
    private void refresh()
    {
        final Handler handler = new Handler();
        final int delay = 1000; //milliseconds
        handler.postDelayed(new Runnable(){
            public void run(){

                if (user_id.getText().toString().length()>0){
                    int size=db.getmessagesCount();
                    if (data_model_list.size()!=size) {
                        reload();
                    }
                }
                handler.postDelayed(this, delay);
            }
        }, delay);

    }

    @SuppressLint("NewApi")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGE_MULTIPLE) {
            ArrayList<String> returnValue = data.getStringArrayListExtra(Pix.IMAGE_RESULTS);
            for (String uri:returnValue){
                upload_image(uri);
            }
        }else if (resultCode == Activity.RESULT_OK && requestCode ==PICK_FILE){
            Uri uri = data.getData();
            upload_file(String.valueOf(uri));
        }
    }
}
