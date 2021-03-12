package com.devdroid.hellochat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.devdroid.hellochat.Model.User;
import com.firebase.client.Firebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    LottieAnimationView lottieAnimation;
    Button btnLogin, btnOTP, btnReset;
    EditText edtEmail, edtPassword, edtEmailReset;
    TextView txtSignup, fgtPassword;
    ImageView img1, img2;
    FirebaseAuth mAuth;
    FirebaseUser firebaseUser;
    String verificationId, mobileNo;
    ProgressDialog progressDialog;
    int flag;

    protected void onStart() {
        super.onStart();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {

            startActivity(new Intent(LoginActivity.this, AfterLogin.class));
            finish();

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        Firebase.setAndroidContext(this);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setMessage("Loading");

        btnLogin = findViewById(R.id.btnLogin);
        btnOTP = findViewById(R.id.btnOTP);
        lottieAnimation = findViewById(R.id.lottieAnimation);
        img1 = findViewById(R.id.img1);
        img2 = findViewById(R.id.img2);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        txtSignup = findViewById(R.id.txtSignup);
        fgtPassword = findViewById(R.id.fgtPassword);

        lottieAnimation.setAnimation(R.raw.login_animation);
        lottieAnimation.playAnimation();

        txtSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            }
        });

        btnOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OTPLogin();
            }
        });

        edtEmail.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                edtEmail.setTextColor(getResources().getColor(R.color.colorPrimary));
                edtEmail.getBackground().mutate().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
                edtPassword.setTextColor(getResources().getColor(R.color.Black));
                edtPassword.getBackground().mutate().setColorFilter(getResources().getColor(R.color.Black), PorterDuff.Mode.SRC_ATOP);
                img1.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
                img2.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.Black), PorterDuff.Mode.SRC_IN);
                return false;
            }
        });


        edtPassword.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                edtPassword.setTextColor(getResources().getColor(R.color.colorPrimary));
                edtPassword.getBackground().mutate().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
                edtEmail.setTextColor(getResources().getColor(R.color.Black));
                edtEmail.getBackground().mutate().setColorFilter(getResources().getColor(R.color.Black), PorterDuff.Mode.SRC_ATOP);
                img2.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
                img1.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.Black), PorterDuff.Mode.SRC_IN);
                return false;
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startSignin(edtEmail.getText().toString(), edtPassword.getText().toString());
            }
        });

        fgtPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ForgotPassword();
            }
        });


    }

    private void OTPLogin() {

        final Dialog dialog = new Dialog(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.otp_dialog, (LinearLayout) findViewById(R.id.otpDialog));

        final TextView txtBefore1 = dialogView.findViewById(R.id.txtBefore1);
        final TextView txtAfter1 = dialogView.findViewById(R.id.txtAfter1);
        final EditText edtOtp = dialogView.findViewById(R.id.edtOTP);
        final EditText edtMobileNo = dialogView.findViewById(R.id.edtMobileNo);
        final TextView txtAfter3 = dialogView.findViewById(R.id.txtAfter3);
        final TextView txtAfter4 = dialogView.findViewById(R.id.txtAfter4);
        final Button btnGetOtp = dialogView.findViewById(R.id.btnGetOtp);
        final Button btnVerify = dialogView.findViewById(R.id.btnVerify);

        btnGetOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(edtMobileNo.getText().toString()) ||
                        edtMobileNo.getText().toString().length() != 10) {

                    Toast.makeText(LoginActivity.this, "Please enter a valid mobile number", Toast.LENGTH_SHORT).show();

                } else {

                    progressDialog.show();

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            "+91" + edtMobileNo.getText().toString(),
                            60,
                            TimeUnit.SECONDS,
                            LoginActivity.this,
                            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                                @Override
                                public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                                    UpdateInfo(phoneAuthCredential,edtMobileNo.getText().toString(),dialog);


                                }

                                @Override
                                public void onVerificationFailed(@NonNull FirebaseException e) {

                                    Log.d("Error:", e.toString());
                                    progressDialog.dismiss();

                                }

                                @Override
                                public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                    super.onCodeSent(s, forceResendingToken);

                                    verificationId = s;
                                    mobileNo = edtMobileNo.getText().toString();

                                    progressDialog.dismiss();

                                    txtBefore1.setVisibility(View.GONE);
                                    txtAfter1.setVisibility(View.VISIBLE);
                                    txtAfter3.setVisibility(View.VISIBLE);
                                    txtAfter4.setVisibility(View.VISIBLE);
                                    edtMobileNo.setVisibility(View.GONE);
                                    edtOtp.setVisibility(View.VISIBLE);
                                    btnGetOtp.setVisibility(View.GONE);
                                    btnVerify.setVisibility(View.VISIBLE);
                                }
                            }
                    );

                }
            }
        });

        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (TextUtils.isEmpty(edtOtp.getText().toString())) {

                    Toast.makeText(LoginActivity.this, "Please enter valid OTP", Toast.LENGTH_SHORT).show();
                    return;

                }

                if (verificationId != null) {

                    progressDialog.show();

                    PhoneAuthCredential phoneAuthCredential = PhoneAuthProvider.getCredential(
                            verificationId,
                            edtOtp.getText().toString()
                    );

                    UpdateInfo(phoneAuthCredential,mobileNo,dialog);
                }

            }
        });

        dialog.setContentView(dialogView);
        dialog.show();
        dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

    }

    public void ForgotPassword() {

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        final View bottomSheetView = LayoutInflater.from(this).inflate(R.layout.bottom_dialog, (LinearLayout) findViewById(R.id.bottomSheetContainer));

        edtEmailReset = bottomSheetView.findViewById(R.id.edtEmailReset);
        btnReset = bottomSheetView.findViewById(R.id.btnReset);

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (TextUtils.isEmpty(edtEmailReset.getText().toString())) {

                    Toast.makeText(LoginActivity.this, "Please check your details...", Toast.LENGTH_SHORT).show();

                }

                else {

                    progressDialog.show();

                    final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");

                    reference.orderByChild("email").equalTo(edtEmailReset.getText().toString())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    if(dataSnapshot.exists()) {

                                        mAuth.sendPasswordResetEmail(edtEmailReset.getText().toString())
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        if (task.isSuccessful()) {

                                                            Toast.makeText(LoginActivity.this, "Password Reset Link sent to email", Toast.LENGTH_SHORT).show();
                                                            bottomSheetDialog.dismiss();
                                                            progressDialog.dismiss();

                                                        } else {

                                                            Toast.makeText(LoginActivity.this, "Cant process the request now...", Toast.LENGTH_SHORT).show();
                                                            Log.d("Email:", task.getException().toString());
                                                            Log.d("Email:", task.getResult().toString());
                                                            bottomSheetDialog.dismiss();
                                                            progressDialog.dismiss();

                                                        }

                                                    }
                                                });
                                    }

                                    else {

                                        Log.d("Check:", "NO");
                                        Toast.makeText(LoginActivity.this, "Looks like you are not registerd, Sign up ", Toast.LENGTH_SHORT).show();
                                        bottomSheetDialog.dismiss();
                                        progressDialog.dismiss();
                                    }
                                    progressDialog.dismiss();
                                    bottomSheetDialog.dismiss();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                }
            }
        });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();

    }

    public void startSignin(String email, String password) {

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {

            Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show();

        }

        else {

            progressDialog.setMessage("Logging in");
            progressDialog.show();

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (task.isSuccessful()) {

                        progressDialog.dismiss();
                        Intent intent = new Intent(LoginActivity.this, AfterLogin.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();

                    } else {

                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, "Invalid Credentials or Server Problem", Toast.LENGTH_SHORT).show();

                    }

                }
            });
        }
    }

    public void UpdateInfo(PhoneAuthCredential phoneAuthCredential, final String mobileNo, final Dialog dialog){

        FirebaseAuth.getInstance().signInWithCredential(phoneAuthCredential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        dialog.dismiss();

                        if (task.isSuccessful()) {

                            progressDialog.dismiss();

                            Toast.makeText(LoginActivity.this, "Logged in Successfully", Toast.LENGTH_SHORT).show();

                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            final String userid = firebaseUser.getUid();
                            DatabaseReference reference;
                            reference = FirebaseDatabase.getInstance().getReference("Users");

                            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    if (dataSnapshot.hasChild(userid)) {

                                        Intent intent = new Intent(getApplicationContext(), AfterLogin.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();

                                    } else {

                                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);
                                        HashMap<String, String> map = new HashMap<>();
                                        map.put("id", userid);
                                        map.put("username", mobileNo);
                                        map.put("phone", mobileNo);

                                        reference.setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                Intent intent = new Intent(getApplicationContext(), AfterLogin.class);
                                                intent.putExtra("new", "yes");
                                                intent.putExtra("phone", mobileNo);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                startActivity(intent);
                                                finish();
                                            }
                                        });

                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });


                        }
                    }
                });
    }
}
