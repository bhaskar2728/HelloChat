package com.devdroid.hellochat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.devdroid.hellochat.Adapters.DisplayUsersAdapter;
import com.devdroid.hellochat.Model.Chats;
import com.devdroid.hellochat.Model.User;
import com.firebase.client.Firebase;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.view.View.GONE;

public class AfterLogin extends AppCompatActivity {

    Button btnNewChat;
    TextView username;
    DatabaseReference reference;
    FirebaseUser firebaseUser;
    RecyclerView chatList;
    Toolbar toolbar,toolbarSearch;
    DisplayUsersAdapter usersAdapter;
    ArrayList<User> userArrayList;
    ArrayList<String> users;
    ProgressDialog progressDialog;
    EditText edtSearch;
    CircleImageView imgBack,imgProfile;
    StorageReference storageReference;
    String emailStr,usernameStr,profileUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_after_login);

        Firebase.setAndroidContext(this);

        storageReference = FirebaseStorage.getInstance().getReference();



        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading");
        progressDialog.show();


        btnNewChat = findViewById(R.id.btnNewChat);

        chatList = findViewById(R.id.chatList);
        chatList.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        chatList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        chatList.setHasFixedSize(true);

        edtSearch = findViewById(R.id.edtSearch);
        imgBack = findViewById(R.id.imgBack);
        imgProfile = findViewById(R.id.profile_img);

        toolbar = findViewById(R.id.toolbar);
        toolbarSearch = findViewById(R.id.toolbarSearch);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

//        setSupportActionBar(toolbarSearch);
//        getSupportActionBar().setTitle("");


        username = findViewById(R.id.username);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        imgProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent,1000);

            }
        });

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                usersAdapter.getFilter().filter(s);

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toolbarSearch.setVisibility(GONE);
                toolbar.setVisibility(View.VISIBLE);
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(edtSearch.getWindowToken(), 0);
                usersAdapter.getFilter().filter("");
            }
        });

        btnNewChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AfterLogin.this,UsersActivity.class));
            }
        });

        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                username.setText(user.getUsername());
                usernameStr = user.getUsername();
                emailStr = user.getEmail();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        users = new ArrayList<>();
        DisplayUsers();
    }

    public void DisplayUsers() {


        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Messages");

        reference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Activity activity = (Activity) AfterLogin.this;
                if(!activity.isFinishing()){
                    progressDialog.show();
                }
                users.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chats chats = snapshot.getValue(Chats.class);

                    if (!users.contains(chats.getSender()) && !users.contains(chats.getReceiver())) {

                        if (chats.getSender().equals(firebaseUser.getUid())) {
                            users.add(chats.getReceiver());
                        } else if (chats.getReceiver().equals(firebaseUser.getUid())) {
                            users.add(chats.getSender());
                        }
                    }
                }
                for (String id : users) {
                    Log.i("UserID", id);
                }
                DisplayUsers2();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void DisplayUsers2() {


        userArrayList = new ArrayList<>();
        DatabaseReference reference2;

        reference2 = FirebaseDatabase.getInstance().getReference("Users");

        reference2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userArrayList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    User user = snapshot.getValue(User.class);
                    for (String id : users) { //all those users chatting with current user
                        if (user.getID().equals(id)) {
                            if (userArrayList.size() != 0) {
                                int flag = 0;
                                for (User user1 : userArrayList) { //check if user there in list
                                    if (!user1.getID().equals(user.getID())) {
//                                        flag = 1;
                                        Log.d("id", user1.getID());
                                        userArrayList.add(user);
                                        break;
                                    }

                                }
//                                if (flag == 1)
//                                    userArrayList.add(user);
                            } else {
                                userArrayList.add(user);
                            }
                        }
                    }

                }

                usersAdapter = new DisplayUsersAdapter(AfterLogin.this, userArrayList);
                chatList.setAdapter(usersAdapter);
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

//        MenuItem menuItem = menu.findItem(R.id.action_search);
//        SearchView searchView = (SearchView) menuItem.getActionView();
//
//        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//               // toolbar.setVisibility(View.VISIBLE);
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                //toolbar.setVisibility(GONE);
//                usersAdapter.getFilter().filter(newText);
//                return false;
//            }
//        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.logout:
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setIcon(R.drawable.dialog_logout_icon);
                builder.setTitle("Logout");
                builder.setMessage("Are you sure you want to Logout?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(AfterLogin.this, LoginActivity.class));
                        finish();
                        FirebaseAuth.getInstance().signOut();
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.show();
                return true;

            case R.id.action_search:
                toolbar.setVisibility(GONE);
                toolbarSearch.setVisibility(View.VISIBLE);
                edtSearch.requestFocus();
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(edtSearch, InputMethodManager.SHOW_IMPLICIT);
                break;

            case R.id.profile:
                Intent intent = new Intent(this,ProfileActivity.class);
                intent.putExtra("username",usernameStr);
                intent.putExtra("email",emailStr);
                intent.putExtra("profile",profileUrl);
                startActivity(intent);
                break;




        }
        return false;
    }

    private void status(String status){
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        HashMap<String,Object> map = new HashMap<>();
        map.put("status",status);
        reference.updateChildren(map);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void uploadImage(final Uri uri) {

        StorageReference fileRef = storageReference.child("users/" + firebaseUser.getUid() + ".jpg");
        fileRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Toast.makeText(AfterLogin.this, "Image successfully uploaded", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
                imgProfile.setImageURI(uri);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(AfterLogin.this, "Upload failed", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        status("offline");
    }


}
