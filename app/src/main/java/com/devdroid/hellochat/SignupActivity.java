package com.devdroid.hellochat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.firebase.client.Firebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class SignupActivity extends AppCompatActivity {

    EditText txt1,txt2,txt3,txt4;
    ImageView img1,img2,img3,img4;
    FirebaseAuth mAuth;
    Button btnSignUp;
    ProgressDialog progressBar;
    DatabaseReference reference;
    LottieAnimationView lottieAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        Firebase.setAndroidContext(this);
        mAuth = FirebaseAuth.getInstance();

        progressBar = new ProgressDialog(this);

        btnSignUp = findViewById(R.id.btnSignUp);

        txt1 = findViewById(R.id.txt1);
        txt2 = findViewById(R.id.txt2);
        txt3 = findViewById(R.id.txt3);
        txt4 = findViewById(R.id.txt4);

        img1 = findViewById(R.id.img1);
        img2 = findViewById(R.id.img2);
        img3 = findViewById(R.id.img3);
        img4 = findViewById(R.id.img4);

        lottieAnimation = findViewById(R.id.lottieAnimation);
        lottieAnimation.setAnimation(R.raw.signup_animation);
        lottieAnimation.playAnimation();

        txt1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                setBlack(txt2,img2,txt3,img3,txt4,img4);
                setPrimary(txt1,img1);
                return false;

            }
        });
        txt2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                setBlack(txt1,img1,txt3,img3,txt4,img4);
                setPrimary(txt2,img2);
                return false;
            }
        });
        txt3.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                setBlack(txt1,img1,txt2,img2,txt4,img4);
                setPrimary(txt3,img3);
                return false;
            }
        });
        txt4.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                setBlack(txt1,img1,txt2,img2,txt3,img3);
                setPrimary(txt4,img4);
                return false;
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                createUser(txt3.getText().toString(),txt4.getText().toString(),txt1.getText().toString(),txt2.getText().toString());
            }
        });

    }

    public void setBlack(EditText t1,ImageView i1,EditText t2,ImageView i2,EditText t3,ImageView i3){
        t1.getBackground().mutate().setColorFilter(getResources().getColor(R.color.Black), PorterDuff.Mode.SRC_ATOP);
        t2.getBackground().mutate().setColorFilter(getResources().getColor(R.color.Black), PorterDuff.Mode.SRC_ATOP);
        t3.getBackground().mutate().setColorFilter(getResources().getColor(R.color.Black), PorterDuff.Mode.SRC_ATOP);
        t1.setTextColor(getResources().getColor(R.color.Black));
        t2.setTextColor(getResources().getColor(R.color.Black));
        t3.setTextColor(getResources().getColor(R.color.Black));
        i1.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.Black), android.graphics.PorterDuff.Mode.SRC_IN);
        i2.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.Black), android.graphics.PorterDuff.Mode.SRC_IN);
        i3.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.Black), android.graphics.PorterDuff.Mode.SRC_IN);
    }
    public void setPrimary(EditText t1,ImageView i1){
        t1.getBackground().setColorFilter(getResources().getColor(R.color.colorPrimary),PorterDuff.Mode.SRC_ATOP);
        t1.setTextColor(getResources().getColor(R.color.colorPrimary));
        i1.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN);
    }
    public void createUser(final String email, String password, final String Username, final String Phone) {

        if(TextUtils.isEmpty(email)||TextUtils.isEmpty(password)||TextUtils.isEmpty(Username)||TextUtils.isEmpty(Phone)){
            Toast.makeText(this, "Please fill all the details", Toast.LENGTH_SHORT).show();
        }
        else if(password.length()<6){
            Toast.makeText(this, "Password must be of minimum 6 characters", Toast.LENGTH_SHORT).show();
        }
        else if(Phone.length()!=10){
            Toast.makeText(this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show();
        }
        else{

            progressBar.setMessage("Signing Up");
            progressBar.show();

            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){

                        Toast.makeText(SignupActivity.this, "SignUp Successful", Toast.LENGTH_SHORT).show();

                        FirebaseUser firebaseUser = mAuth.getCurrentUser();

                        String userid = firebaseUser.getUid();
                        reference = FirebaseDatabase.getInstance().getReference("Users");
                        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

                        HashMap<String,String> map = new HashMap<>();
                        map.put("id",userid);
                        map.put("username",Username);
                        map.put("phone",Phone);
                        map.put("email",email);
                        reference.setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                progressBar.dismiss();
                                Intent intent = new Intent(getApplicationContext(),AfterLogin.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            }
                        });
                    }
                    else{
                        progressBar.dismiss();
                        if(task.getException().toString().contains("The email address is badly")){
                            Toast.makeText(SignupActivity.this, "Please enter a valid Email address...", Toast.LENGTH_SHORT).show();
                        }
                        else if(task.getException().toString().contains("The email address is already in use")){
                            Toast.makeText(SignupActivity.this, "This email address is already in use", Toast.LENGTH_SHORT).show();
                        }
                        else{

                            Toast.makeText(SignupActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

        }

    }
}
