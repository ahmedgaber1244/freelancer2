package com.example.gaber.freelancer2;

        import android.app.Service;
        import android.util.Log;
        import android.widget.Toast;

        import com.google.firebase.FirebaseApp;
        import com.google.firebase.database.DataSnapshot;
        import com.google.firebase.database.DatabaseError;
        import com.google.firebase.database.DatabaseReference;
        import com.google.firebase.database.FirebaseDatabase;
        import com.google.firebase.database.Query;
        import com.google.firebase.database.ValueEventListener;
        import com.google.firebase.iid.FirebaseInstanceId;
        import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by gaber on 26/08/2018.
 */

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        String refreshedtoken= FirebaseInstanceId.getInstance().getToken();


    }




}
