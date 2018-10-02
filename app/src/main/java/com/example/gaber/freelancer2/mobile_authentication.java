package com.example.gaber.freelancer2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.concurrent.TimeUnit;


public class mobile_authentication extends AppCompatActivity {

    FirebaseAuth auth;
    String vervication_id,mResendToken;
    EditText enter_mobile,mcode;
    Button check_code;
    LinearLayout sms_code;
    ProgressBar progress1,progress2;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_authentication);
        auth=FirebaseAuth.getInstance();
        enter_mobile=(EditText)findViewById(R.id.phone_number);
        mcode=(EditText)findViewById(R.id.code);
        check_code=(Button)findViewById(R.id.check_code);
        sms_code=(LinearLayout)findViewById(R.id.sms_code);
        progress1=(ProgressBar)findViewById(R.id.progress1);
        progress2=(ProgressBar)findViewById(R.id.progress2);

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                String code=credential.getSmsCode();
                mcode.setText(code);



            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w("dada", "onVerificationFailed", e);

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // ...
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // ...
                }

                // Show a message and update the UI
                // ...
            }

            @Override
            public void onCodeSent(String mverificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d("dasda", "onCodeSent:" + mverificationId);

                // Save verification ID and resending token so we can use them later
                vervication_id = mverificationId;

                // ...
            }
        };

    }

    private void get_verfiy_code(String phone_number){

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phone_number,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks

    }

    private void sign_in(String vervication_id,String code){
        PhoneAuthCredential authCredential= PhoneAuthProvider.getCredential(vervication_id,code);
        signInWithPhoneAuthCredential(authCredential);
    }

    private void signInWithPhoneAuthCredential(final PhoneAuthCredential credential) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                       if (task.isSuccessful()){
                           String refreshedtoken= FirebaseInstanceId.getInstance().getToken();

                           check_user(refreshedtoken,enter_mobile.getText().toString(),credential.getSmsCode());

                       }else {
                           Toast.makeText(mobile_authentication.this,"Login unsuccessful", Toast.LENGTH_LONG).show();

                       }
                    }
                });
    }


    public void verfiy(View view) {
        if (enter_mobile.getText().toString().length()==13) {
            get_verfiy_code(enter_mobile.getText().toString());
            sms_code.setVisibility(View.VISIBLE);
            check_code.setVisibility(View.VISIBLE);
            progress1.setVisibility(View.VISIBLE);
        }else {
            Toast.makeText(this,"please put your phone number",Toast.LENGTH_LONG).show();
        }

    }
    private void sign_up(String user_token,String name,String pass)
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users");
        user_data_model user=new user_data_model(name,user_token,pass,"","Online");
        myRef.push().setValue(user);
        SharedPreferences.Editor editor = getSharedPreferences("logged_in", MODE_PRIVATE).edit();
        editor.putBoolean("state",true);
        editor.putString("name",name);
        editor.putString("pass",pass);
        editor.apply();
        Intent main=new Intent(mobile_authentication.this,MainActivity.class);
        startActivity(main);
        finish();

    }

    private void check_user(final String user_token, final String name, final String pass) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("users");
        Query query = reference.orderByChild("name").equalTo(name);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    for (DataSnapshot sub_type : dataSnapshot.getChildren()) {
                        if (!FirebaseInstanceId.getInstance().getToken().equals(sub_type.child("token").getValue())){
                            DatabaseReference myRef = sub_type.getRef();
                            myRef.child("token").setValue(FirebaseInstanceId.getInstance().getToken());

                        }
                        SharedPreferences.Editor editor = getSharedPreferences("logged_in", MODE_PRIVATE).edit();
                        editor.putBoolean("state",true);
                        editor.putString("name",name);
                        editor.putString("pass",pass);
                        editor.apply();
                        Intent main=new Intent(mobile_authentication.this,MainActivity.class);
                        startActivity(main);
                        finish();
                        Toast.makeText(getApplicationContext(),"welcome",Toast.LENGTH_LONG).show();


                    }
                }else {
                    sign_up(user_token,name,pass);
                    Toast.makeText(getApplicationContext(),"welcome",Toast.LENGTH_LONG).show();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void check(View view) {
        if (mcode.getText().toString().length()==6) {
            sign_in(vervication_id, mcode.getText().toString());
            progress2.setVisibility(View.VISIBLE);
        }else {
            Toast.makeText(this,"please put your vervication code",Toast.LENGTH_LONG).show();

        }
    }
}


