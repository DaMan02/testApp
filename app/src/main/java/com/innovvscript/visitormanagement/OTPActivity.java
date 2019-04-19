package com.innovvscript.visitormanagement;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import id.zelory.compressor.Compressor;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class OTPActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Boolean verifying = false;
    private String phoneNo;
    private StorageReference storageRef;
    private String verificationId;
    private File compressedImage;
    private DatabaseReference userRef;
    private TextView textView;
    private TextInputEditText inputEditText;
    private CustomDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);
        mAuth = FirebaseAuth.getInstance();

        phoneNo = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("phone","");

        textView = findViewById(R.id.text);
        textView.setVisibility(View.GONE);
        inputEditText = findViewById(R.id.otp_input);
        dialog = new CustomDialog(this);

        final MaterialButton verify = findViewById(R.id.verify_btn);
        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onVerifyClick();
            }
        });

        Intent intent = getIntent();
        storageRef = FirebaseStorage.getInstance().getReference();
        userRef = FirebaseDatabase.getInstance().getReference();
        sendVerificationCode();
        compressImage(intent.getStringExtra("path"));

    }

    private void createDB(String url,String suspicious) {
        DatabaseReference mRef = userRef.child(suspicious + "users").push();
        mRef.child("mobile").setValue(phoneNo);
        mRef.child("url").setValue(url);

        if(suspicious.isEmpty()) {
            mRef.child("visit_count").setValue(1);
            userRef.child("phone_index").child(phoneNo);

            dialog.setTitle("Welcome");
            dialog.setMessage("New Visitor Saved");
            dialog.setPositiveBtnNull("Ok");
            dialog.show();
        }
    }


    private void compressImage(String path) {
        File savedImage = new File(path);
        Disposable disposable = new Compressor(this)
                .compressToFileAsFlowable(savedImage)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<File>() {
                    @Override
                    public void accept(File file) {
                        compressedImage = file;
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        throwable.printStackTrace();
                        Log.e("tag",throwable.getMessage());
                    }
                });
    }

    private void uploadImageToDB(final String suspicious) {
        Uri file = Uri.fromFile(new File(compressedImage.getAbsolutePath()));

        final StorageReference photoRef = storageRef.child(suspicious + "users_images")
                .child(compressedImage.getPath() + ".jpg");

        photoRef.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        photoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Log.w("uri",uri.toString() + "sus:" + suspicious);
                                    createDB(uri.toString(),suspicious);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(OTPActivity.this,"Upload failed !",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        Log.e("upload",exception.getMessage());
                    }
                });
    }



    private void sendVerificationCode() {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91" + phoneNo,
                30,
                TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                mCallBack
        );

    }

    public void onVerifyClick(){
        try {
            verifyOTP(inputEditText.getText().toString());
        }catch (IllegalArgumentException e){
            Log.w("e",e.getMessage());
        }
    }

    private void verifyOTP(String entered) throws IllegalArgumentException {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, entered);
        Log.w("entered",entered);
        signInWithCredential(credential);
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                                     uploadImageToDB("");
                        } else {
                            Toast.makeText(OTPActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks
            mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            Log.w("verificationId",s);
            textView.setVisibility(View.VISIBLE);
            verificationId = s;
        }

        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
//            String code = phoneAuthCredential.getSmsCode();
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(OTPActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            uploadImageToDB("suspicious_");
            showErrorMsg();
        }
    };

    private void showErrorMsg() {
     dialog.setTitle("Invalid");
     dialog.setMessage("Verification failed !");
     dialog.setPositiveBtnNull("Dismiss");
     dialog.show();
    }


}
