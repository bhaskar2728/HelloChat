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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.view.View.GONE;

public class AfterLogin extends AppCompatActivity {

    Button btnNewChat;
    TextView username;
    DatabaseReference reference;
    FirebaseUser firebaseUser;
    RecyclerView chatList;
    Toolbar toolbar, toolbarSearch;
    DisplayUsersAdapter usersAdapter;
    ArrayList<User> userArrayList;
    ArrayList<String> users;
    ProgressDialog progressDialog;
    EditText edtSearch;
    CircleImageView imgBack, imgProfile;
    String emailStr, usernameStr, profileUrl, newUser, phone, phoneStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_after_login);
        Firebase.setAndroidContext(this);


        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading");
        progressDialog.show();


        btnNewChat = findViewById(R.id.btnNewChat);
        chatList = findViewById(R.id.chatList);
        edtSearch = findViewById(R.id.edtSearch);
        imgBack = findViewById(R.id.imgBack);
        imgProfile = findViewById(R.id.profile_img);
        toolbar = findViewById(R.id.toolbar);
        toolbarSearch = findViewById(R.id.toolbarSearch);
        username = findViewById(R.id.username);

        chatList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        chatList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        chatList.setHasFixedSize(true);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        if (getIntent() != null) {
            if (getIntent().getStringExtra("new") != null) {
                newUser = getIntent().getStringExtra("new");
                phone = getIntent().getStringExtra("phone");
            }
        }

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        imgProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, 1000);

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
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(edtSearch.getWindowToken(), 0);
                usersAdapter.getFilter().filter("");

            }
        });

        btnNewChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(AfterLogin.this, UsersActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList("friendsList",userArrayList);
                intent.putExtras(bundle);
                startActivity(intent);

            }
        });


        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    username.setText(user.getUsername());
                    usernameStr = user.getUsername();
                    emailStr = user.getEmail();
                    phoneStr = user.getPhone();
                }

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
                if (!activity.isFinishing()) {
                    progressDialog.show();
                }
                users.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    Chats chats = snapshot.getValue(Chats.class);

                    Log.d("Order", chats.getMessage());


                    if (chats.getSender().equals(firebaseUser.getUid())) {

                        if (users.contains(chats.getReceiver())) {
                            users.remove(chats.getReceiver());
                        }
                        users.add(chats.getReceiver());

                    } else if (chats.getReceiver().equals(firebaseUser.getUid())) {


                        if (users.contains(chats.getSender())) {
                            users.remove(chats.getSender());
                        }
                        users.add(chats.getSender());

                    }
                }
                for (String id : users) {
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(id);

                    reference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            User user = dataSnapshot.getValue(User.class);
                            Log.d("Check:", user.getUsername());

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
                Collections.reverse(users);
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


                for (String id : users) {
                    Log.d("Users", id);
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User user = snapshot.getValue(User.class);
                        Log.d("ID:", "Yes");
                        if (user.getID() != null && user.getID().equals(id)) {

                            userArrayList.add(user);
                            Log.d("Add:", user.getUsername());

                        }
                    }
                }

                usersAdapter = new DisplayUsersAdapter(AfterLogin.this, userArrayList);
                chatList.setAdapter(usersAdapter);
                progressDialog.dismiss();

                if (newUser != null && newUser.equals("yes")) {
                    newUser = null;
                    Intent intent = new Intent(AfterLogin.this, ProfileActivity.class);
                    intent.putExtra("username", phone);
                    intent.putExtra("phone", phone);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
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
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(edtSearch, InputMethodManager.SHOW_IMPLICIT);
                break;

            case R.id.profile:
                Intent intent = new Intent(this, ProfileActivity.class);
                intent.putExtra("username", usernameStr);

                if (emailStr != null)
                    intent.putExtra("email", emailStr);
                else
                    intent.putExtra("phone", phoneStr);

                intent.putExtra("profile", profileUrl);
                startActivity(intent);
                break;

        }
        return false;
    }

    private void status(String status) {
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        HashMap<String, Object> map = new HashMap<>();
        map.put("status", status);
        reference.updateChildren(map);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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
