package com.example.gaber.freelancer2;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.percent.PercentLayoutHelper;
import android.support.percent.PercentRelativeLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.fxn.pix.Pix;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
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

import java.io.File;
import java.util.ArrayList;


/**
 * Created by gaber on 13/08/2018.
 */

public class login extends AppCompatActivity {
    EditText user_name,password,sigin_user_name,sigin_pass;
    SharedPreferences prefs ;
    int RC_SIGN_IN=111;
    GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private TextView tvSignupInvoker;
    private LinearLayout llSignup;
    private TextView tvSigninInvoker;
    private LinearLayout llSignin;
    private Button btnSignup,btnipload,btnSignin;
    private StorageReference mStorageRef;
    int PICK_IMAGE_MULTIPLE = 00;
    boolean image_uploaded=false;
    String image_url;

    @Override
        protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);
        tvSigninInvoker=(TextView) findViewById(R.id.tvSigninInvoker);
        tvSignupInvoker=(TextView) findViewById(R.id.tvSignupInvoker);

        btnSignin=(Button) findViewById(R.id.btnSignin);
        btnSignup=(Button) findViewById(R.id.btnSignup);

        llSignin=(LinearLayout) findViewById(R.id.llSignin);
        llSignup=(LinearLayout) findViewById(R.id.llSignup);

        user_name=(EditText)findViewById(R.id.user_name);
        password=(EditText)findViewById(R.id.password);


        sigin_user_name=(EditText)findViewById(R.id.sign_in_user_name);
        sigin_pass=(EditText)findViewById(R.id.sign_in_pass);
        btnipload=(Button) findViewById(R.id.btnipload);


        mAuth = FirebaseAuth.getInstance();

        mStorageRef = FirebaseStorage.getInstance().getReference();



        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient= GoogleSignIn.getClient(this,gso);

         //check if user logged in before
        prefs = getSharedPreferences("logged_in", MODE_PRIVATE);
        boolean state = prefs.getBoolean("state", false);
          if (state!=false){
            Intent main=new Intent(login.this,chat.class);
            startActivity(main);
            finish();
        }
        //check user
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name=user_name.getText().toString();
                String pass=password.getText().toString();
                String refreshedtoken= FirebaseInstanceId.getInstance().getToken();
                if (name.length()>0&&pass.length()>0)
                {
                    check_user(refreshedtoken,name,pass);
                }else {
                    Toast.makeText(getApplicationContext(),"please fill fields",Toast.LENGTH_LONG).show();

                }

            }
        });


        btnSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name=sigin_user_name.getText().toString();
                String pass=sigin_pass.getText().toString();
                if (name.length()>0&&pass.length()>0)
                {
                    sign_in(name,pass);
                }else {
                    Toast.makeText(getApplicationContext(),"please fill fields",Toast.LENGTH_LONG).show();

                }

            }
        });






        tvSigninInvoker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSigninForm();
            }
        });

        tvSignupInvoker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSignupForm();
            }
        });


        btnipload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                select_images();
            }
        });
    }

    private void sign_up(String user_token,String name,String pass,String image_url)
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users");
        user_data_model user=new user_data_model(name,user_token,pass,image_url,"Online");
        myRef.push().setValue(user);
        SharedPreferences.Editor editor = getSharedPreferences("logged_in", MODE_PRIVATE).edit();
        editor.putBoolean("state",true);
        editor.putString("name",name);
        editor.putString("pass",pass);
        editor.apply();
        Intent main=new Intent(login.this,chat.class);
        startActivity(main);
        finish();

    }
    private void signIn_gmail() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
                // ...
            }
        }
        if (resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGE_MULTIPLE) {
            ArrayList<String> returnValue = data.getStringArrayListExtra(Pix.IMAGE_RESULTS);
            for (String uri:returnValue){
                upload_image(uri);
            }
        }
    }
    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String refreshedtoken= FirebaseInstanceId.getInstance().getToken();
                            check_user(refreshedtoken,acct.getEmail(),acct.getIdToken());
                        } else {

                        }

                        // ...
                    }
                });
    }
    private void showSignupForm()
    {
        PercentRelativeLayout.LayoutParams paramsLogin = (PercentRelativeLayout.LayoutParams) llSignin.getLayoutParams();
        PercentLayoutHelper.PercentLayoutInfo infoLogin = paramsLogin.getPercentLayoutInfo();
        infoLogin.widthPercent = 0.15f;
        llSignin.requestLayout();


        PercentRelativeLayout.LayoutParams paramsSignup = (PercentRelativeLayout.LayoutParams) llSignup.getLayoutParams();
        PercentLayoutHelper.PercentLayoutInfo infoSignup = paramsSignup.getPercentLayoutInfo();
        infoSignup.widthPercent = 0.85f;
        llSignup.requestLayout();

        tvSignupInvoker.setVisibility(View.GONE);
        tvSigninInvoker.setVisibility(View.VISIBLE);
        Animation translate= AnimationUtils.loadAnimation(getApplicationContext(),R.anim.translate_right_to_left);
        llSignup.startAnimation(translate);

        Animation clockwise= AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_right_to_left);
        btnSignup.startAnimation(clockwise);

    }
    private void showSigninForm()
    {
        PercentRelativeLayout.LayoutParams paramsLogin = (PercentRelativeLayout.LayoutParams) llSignin.getLayoutParams();
        PercentLayoutHelper.PercentLayoutInfo infoLogin = paramsLogin.getPercentLayoutInfo();
        infoLogin.widthPercent = 0.85f;
        llSignin.requestLayout();


        PercentRelativeLayout.LayoutParams paramsSignup = (PercentRelativeLayout.LayoutParams) llSignup.getLayoutParams();
        PercentLayoutHelper.PercentLayoutInfo infoSignup = paramsSignup.getPercentLayoutInfo();
        infoSignup.widthPercent = 0.15f;
        llSignup.requestLayout();

        Animation translate= AnimationUtils.loadAnimation(getApplicationContext(),R.anim.translate_left_to_right);
        llSignin.startAnimation(translate);

        tvSignupInvoker.setVisibility(View.VISIBLE);
        tvSigninInvoker.setVisibility(View.GONE);
        Animation clockwise= AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_left_to_right);
        btnSignin.startAnimation(clockwise);
    }
    private void check_user(final String user_token, final String name, final String pass)
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("users");
        Query query = reference.orderByChild("name").equalTo(name);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    for (DataSnapshot sub_type : dataSnapshot.getChildren()) {

                        Log.w("sdada", String.valueOf(sub_type.child("pass").getValue()));
                        Toast.makeText(getApplicationContext(),"this user name is already used",Toast.LENGTH_LONG).show();


                    }
                }else {
                    if (image_uploaded) {
                        sign_up(user_token, name, pass,image_url);
                        Toast.makeText(getApplicationContext(), "welcome", Toast.LENGTH_LONG).show();
                    }else {
                        Toast.makeText(getApplicationContext(), "please select image", Toast.LENGTH_LONG).show();

                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
    private void sign_in(final String name, final String pass)
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("users");
        Query query = reference.orderByChild("name").equalTo(name);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    for (DataSnapshot sub_type : dataSnapshot.getChildren()) {

                        if (pass.equals(sub_type.child("pass").getValue())){
                            DatabaseReference myRef = sub_type.getRef();
                            myRef.child("status").setValue("Online");

                            if (!FirebaseInstanceId.getInstance().getToken().equals(sub_type.child("token").getValue())){
                                myRef.child("token").setValue(FirebaseInstanceId.getInstance().getToken());

                            }
                            SharedPreferences.Editor editor = getSharedPreferences("logged_in", MODE_PRIVATE).edit();
                            editor.putBoolean("state",true);
                            editor.putString("name",name);
                            editor.putString("pass",pass);
                            editor.apply();
                            Intent main=new Intent(login.this,chat.class);
                            startActivity(main);
                            finish();
                            Toast.makeText(getApplicationContext(),"welcome",Toast.LENGTH_LONG).show();

                        }else {
                            Toast.makeText(getApplicationContext(),"wrong username or password" ,Toast.LENGTH_LONG).show();
                        }


                    }
                }else {
                    Toast.makeText(getApplicationContext(),"there is no such a user",Toast.LENGTH_LONG).show();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
    public void goto_gmail_verify(View view)
    {
        signIn_gmail();
    }
    public void goto_mobile_verify(View view)
    {
        Intent goto_mobile_verify=new Intent(this,mobile_authentication.class);
        startActivity(goto_mobile_verify);
    }
    private void upload_image(String audioFilePath)
    {
        Uri file = Uri.fromFile(new File(audioFilePath));
        final ProgressDialog progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Uploading");
        progressDialog.show();
        final StorageReference ref = mStorageRef.child("profile image").child(audioFilePath);
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
                    image_uploaded=true;
                    image_url=downloadUri.toString();
                    progressDialog.dismiss();

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
    private void select_images()
    {
        Pix.start(this,
                PICK_IMAGE_MULTIPLE,1);
    }

}
