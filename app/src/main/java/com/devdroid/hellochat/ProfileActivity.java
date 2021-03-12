package com.devdroid.hellochat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    CircleImageView imgEdit,imgView;
    ProgressDialog progressDialog;
    StorageReference storageReference;
    FirebaseUser firebaseUser;
    Intent intent;
    TextView edtUsername, txtHeading,txtEP;
    ProgressBar progressBar;
    DatabaseReference databaseReference;
    ImageView imgPhone,imgEmail;
    LinearLayout llName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.child(firebaseUser.getUid()).child("profile").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()!=null)
                    Picasso.get().load(dataSnapshot.getValue().toString()).placeholder(R.drawable.ic_account_circle).into(imgView);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        imgEdit = findViewById(R.id.imgEdit);
        imgView = findViewById(R.id.imgView);
        txtEP = findViewById(R.id.txtEP);
        txtHeading = findViewById(R.id.txtHeading);
        edtUsername = findViewById(R.id.edtUsername);
        progressBar = findViewById(R.id.progressBar);
        imgEmail = findViewById(R.id.imgEmail);
        imgPhone = findViewById(R.id.imgPhone);
        imgEdit = findViewById(R.id.imgEdit);
        llName = findViewById(R.id.llName);

        llName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(ProfileActivity.this,R.style.BottomSheetDialogTheme);
                View bottomSheetDialogView = LayoutInflater.from(ProfileActivity.this).inflate(R.layout.bottom_dialog_edit,(LinearLayout)findViewById(R.id.bottomSheetEditContainer));

                TextView txtSave = bottomSheetDialogView.findViewById(R.id.txtSave);
                TextView txtCancel = bottomSheetDialogView.findViewById(R.id.txtCancel);
                final EditText edtName = bottomSheetDialogView.findViewById(R.id.edtName);

                edtName.setText(edtUsername.getText().toString());

                txtCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bottomSheetDialog.dismiss();
                    }
                });

                txtSave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if(!TextUtils.isEmpty(edtName.getText().toString())) {
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("username", edtName.getText().toString());
                            reference.updateChildren(map);
                            edtUsername.setText(edtName.getText().toString());
                        }
                        else{
                            Toast.makeText(ProfileActivity.this, "Please enter valid name", Toast.LENGTH_SHORT).show();
                        }
                        bottomSheetDialog.dismiss();
                    }
                });

                bottomSheetDialog.setContentView(bottomSheetDialogView);
                bottomSheetDialog.show();
            }
        });

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading");

        intent = getIntent();
        edtUsername.setText(intent.getStringExtra("username"));
        if(intent.getStringExtra("email")!=null) {
            txtHeading.setText("Email");
            txtEP.setText(intent.getStringExtra("email"));
        }
        else if(intent.getStringExtra("phone")!=null){
            imgEmail.setVisibility(View.GONE);
            imgPhone.setVisibility(View.VISIBLE);
            txtHeading.setText("Phone");
            txtEP.setText(intent.getStringExtra("phone"));
        }

        imgEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent,1000);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1000){
            if(resultCode == Activity.RESULT_OK){
                progressDialog.show();
                Uri imageUri = data.getData();
                uploadImage(imageUri);
            }
        }
    }

    private void uploadImage(final Uri uri) {

        final StorageReference fileRef = storageReference.child("users/" + firebaseUser.getUid() + ".jpg");
        fileRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Toast.makeText(ProfileActivity.this, "Image successfully uploaded", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
                imgView.setImageURI(uri);

                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
                        HashMap<String,Object> map = new HashMap<>();
                        map.put("profile",uri.toString());
                        reference.updateChildren(map);

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });



            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(ProfileActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();

            }
        });
    }
}