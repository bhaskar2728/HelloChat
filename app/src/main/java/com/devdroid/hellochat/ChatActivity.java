package com.devdroid.hellochat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.devdroid.hellochat.Adapters.DisplayChatsAdapter;
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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    Intent intent;
    Toolbar toolbar;
    TextView username,txtStatus;
    FirebaseUser firebaseUser;
    DatabaseReference reference;
    ImageView btnSend;
    LinearLayout llBack;
    EditText edtMessage;
    ArrayList<Chats> chatsArrayList;
    RecyclerView messagesView;
    DisplayChatsAdapter chatsAdapter;
    ValueEventListener seenListener;
    CircleImageView imgProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Firebase.setAndroidContext(this);

        btnSend = findViewById(R.id.btnSend);
        llBack = findViewById(R.id.llBack);
        edtMessage = findViewById(R.id.edtMessage);
        imgProfile = findViewById(R.id.profile_img);
        toolbar = findViewById(R.id.toolbar);
        username  = findViewById(R.id.username);
        txtStatus = findViewById(R.id.txtStatus);
        messagesView = findViewById(R.id.messagesView);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");


        intent = getIntent();
        final String userid = intent.getStringExtra("userid");

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

        FirebaseDatabase.getInstance().getReference("Users").child(userid).child("profile").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()!=null)
                    Picasso.get().load(dataSnapshot.getValue().toString()).placeholder(R.drawable.ic_account_circle).into(imgProfile);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        messagesView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        messagesView.setLayoutManager(linearLayoutManager);

        llBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sender = firebaseUser.getUid();
                String receiver = userid;
                String message = edtMessage.getText().toString();
                edtMessage.setText("");

                if(!TextUtils.isEmpty(message)) {
                    CreateChat(sender, receiver,message);
                }
                else{
                    Toast.makeText(ChatActivity.this, "Cannot send empty message", Toast.LENGTH_SHORT).show();
                }
            }
        });

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                username.setText(user.getUsername());
                txtStatus.setText(user.getStatus());
                readMessage(firebaseUser.getUid(),user.getID());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        seenMessage(userid);
    }

    public void CreateChat(String sender, String receiver, String message) {


        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("sender",sender);
        hashMap.put("receiver",receiver);
        hashMap.put("message",message);
        hashMap.put("isseen",false);

        java.util.Date date=new java.util.Date();
        long time = System.currentTimeMillis();
        //String time = date.toString().split(" ")[3].substring(0,5) ;
        hashMap.put("time",time);

        reference.child("Messages").push().setValue(hashMap);

    }
    public void readMessage(final String myid, final String userid){

        chatsArrayList = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Messages");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatsArrayList.clear();
                for(DataSnapshot snapshot:dataSnapshot.getChildren()){

                    Chats chats = snapshot.getValue(Chats.class);
                    if((chats.getSender().equals(myid) && chats.getReceiver().equals(userid))||(chats.getSender().equals(userid) && chats.getReceiver().equals(myid))){
                        chatsArrayList.add(chats);
                    }

                }
                chatsAdapter = new DisplayChatsAdapter(getApplicationContext(),chatsArrayList);
                messagesView.setAdapter(chatsAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void status(String status){
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        HashMap<String,Object> map = new HashMap<>();
        map.put("status",status);
        reference.updateChildren(map);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        reference.removeEventListener(seenListener);
        status("offline");
    }

    public void seenMessage(final String userid){
        reference = FirebaseDatabase.getInstance().getReference("Messages");
        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                    Chats chats = snapshot.getValue(Chats.class);
                    if(chats.getReceiver().equals(firebaseUser.getUid()) && chats.getSender().equals(userid)){
                        HashMap<String,Object> map = new HashMap<>();
                        map.put("isseen",true);
                        snapshot.getRef().updateChildren(map);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
