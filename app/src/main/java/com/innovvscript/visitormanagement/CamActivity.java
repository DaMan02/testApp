package com.innovvscript.visitormanagement;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import com.camerakit.CameraKitView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.io.File;
import java.io.FileOutputStream;


public class CamActivity extends AppCompatActivity {

    private CameraKitView cameraKitView;
    private FloatingActionButton cameraButton;
    private String phoneNo;
    private DatabaseReference ref;
    private int existingUser = 0;
    private CustomDialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cam);
        cameraKitView = findViewById(R.id.camera);
        cameraButton = findViewById(R.id.camera_btn);
        ref = FirebaseDatabase.getInstance().getReference("users");

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePic();
            }
        });
        dialog = new CustomDialog(this);

        phoneNo = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("phone","");
    }


    private void takePic() {
        cameraKitView.captureImage(new  CameraKitView.ImageCallback() {
            @Override
            public void onImage(CameraKitView cameraKitView, final byte[] capturedImage) {

                File savedPhoto = new File(Environment.getExternalStorageDirectory(), "my_photo.jpg");
                try {
                    FileOutputStream outputStream = new FileOutputStream(savedPhoto.getPath());
                    outputStream.write(capturedImage);
                    outputStream.close();
                    createObservable(savedPhoto.getPath());
//                    verify(savedPhoto.getPath());
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private int verify(final String path){

        Query phoneQuery = ref.orderByChild("mobile").equalTo(phoneNo);
        Log.w("query",phoneQuery.toString());
        phoneQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot mValue : dataSnapshot.getChildren()){

                        String visitData = mValue.child("visit_count").getValue().toString();
                        String uid = mValue.getKey();
                        int visitCount = Integer.parseInt(visitData);
                        Log.e("uid/count",uid + " / " + visitCount);
                        existingUser = 200;
                        mValue.getRef().child("visit_count").setValue(visitCount + 1);
                        dialog.setTitle("Welcome");
                        int count  = visitCount + 1;
                        dialog.setMessage("Welcome back for " + count + " time");
                        dialog.setPositiveBtnNull("Dismiss");
                        dialog.show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
              Log.e("err",databaseError.toString());

            }

        });
          Log.d("existing?",existingUser + "");
           return existingUser;
    }

    private void createObservable(final String path) {
        Observable<Integer> queryObservable = Observable.just(verify(path));

       queryObservable.subscribe(new Observer<Integer>() {
           @Override
           public void onSubscribe(Disposable d) {

           }
           @Override
           public void onNext(Integer anInt) {
               Log.w("onNext()","emitted:" + anInt);
           }

           @Override
           public void onError(Throwable e) {
               Log.e("expn",e.getMessage());
           }

           @Override
           public void onComplete() {
               Log.w("onComplete()",existingUser  + "");
               new Handler().postDelayed(new Runnable() {
                   @Override
                   public void run() {
                       if(existingUser != 200)
                           sendOTP(path);
                   }
               },3000);
           }
       });
    }

    private void sendOTP(String path) {
         startActivity(new Intent(CamActivity.this,OTPActivity.class).putExtra("path",path));
         finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        cameraKitView.onStart();
    }
    @Override
    protected void onResume() {
        super.onResume();
        cameraKitView.onResume();
    }
    @Override
    protected void onPause() {
        cameraKitView.onPause();
        super.onPause();
    }
    @Override
    protected void onStop() {
        cameraKitView.onStop();
        super.onStop();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        cameraKitView.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
