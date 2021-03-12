package com.devdroid.hellochat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.devdroid.hellochat.Adapters.DisplayUsersAdapter;
import com.devdroid.hellochat.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.view.View.GONE;

public class UsersActivity extends AppCompatActivity {

    RecyclerView usersView;
    DisplayUsersAdapter displayUsersAdapter;
    ArrayList<User> userArrayList,friendsList;
    ArrayList<String> friendsIdList;
    ProgressDialog progressDialog;
    Toolbar toolbar,toolbarSearch;
    EditText edtSearch;
    CircleImageView imgBack,imgBackFromSearch;
    int flag;
    TextView txtNoItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        toolbar = findViewById(R.id.toolbar);
        toolbarSearch = findViewById(R.id.toolbarSearch);
        edtSearch = findViewById(R.id.edtSearch);
        imgBack = findViewById(R.id.imgBack);
        imgBackFromSearch = findViewById(R.id.imgBackFromSearch);
        usersView = findViewById(R.id.usersView);
        txtNoItems = findViewById(R.id.txtNoItems);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        imgBackFromSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toolbar.setVisibility(View.VISIBLE);
                toolbarSearch.setVisibility(GONE);
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(edtSearch.getWindowToken(), 0);
                displayUsersAdapter.getFilter().filter("");
            }
        });

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                displayUsersAdapter.getFilter().filter(s);

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        usersView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        usersView.setHasFixedSize(true);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading");

        Intent intent = getIntent();
        friendsList = intent.getExtras().getParcelableArrayList("friendsList");

        friendsIdList = new ArrayList<>();
        for(User user: friendsList){
            friendsIdList.add(user.getID());
        }

        userArrayList = new ArrayList<>();
        DisplayUsers();
    }

    public void DisplayUsers() {


        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Activity activity = (Activity) UsersActivity.this;
                if(!activity.isFinishing()){
                    progressDialog.show();
                }
                userArrayList.clear();
                flag = 0;
                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    User user = snapshot.getValue(User.class);
                    if(!user.getID().equals(firebaseUser.getUid()) && !friendsIdList.contains(user.getID())){
                        userArrayList.add(user);
                        flag = 1;
                    }
                }
                if(flag == 0)
                    txtNoItems.setVisibility(View.VISIBLE);
                displayUsersAdapter = new DisplayUsersAdapter(UsersActivity.this,userArrayList);
                usersView.setAdapter(displayUsersAdapter);
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menusearch, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){

            case R.id.action_search:
                toolbar.setVisibility(GONE);
                toolbarSearch.setVisibility(View.VISIBLE);
                edtSearch.requestFocus();
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(edtSearch, InputMethodManager.SHOW_IMPLICIT);
        }
        return false;
    }

}
