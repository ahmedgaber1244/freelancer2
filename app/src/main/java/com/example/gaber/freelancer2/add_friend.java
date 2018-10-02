package com.example.gaber.freelancer2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class add_friend extends MainActivity {

    EditText text;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);
        text=(EditText)findViewById(R.id.name);
    }

    private void find_user(final String s)
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("users");
        Query query = reference.orderByChild("name").equalTo(s);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {


                    for (DataSnapshot sub_type : dataSnapshot.getChildren()) {
                        String name=sub_type.child("name").getValue().toString();
                        String image=sub_type.child("image_url").getValue().toString();
                        String token=sub_type.child("token").getValue().toString();

                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference myRef = database.getReference("friends_list").child(getSharedPreferences("logged_in",MODE_PRIVATE).getString("name","")).child(s);
                        myRef.child("name").setValue(name);
                        myRef.child("image_url").setValue(image);
                        myRef.child("token").setValue(token);

                    }
                }else {
                    Toast.makeText(getApplicationContext(),"no such user",Toast.LENGTH_LONG).show();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void find_friend(View view) {
        find_user(text.getText().toString());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent back=new Intent(this,MainActivity.class);
        finish();
        startActivity(back);
    }
}
