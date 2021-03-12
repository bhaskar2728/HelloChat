package com.devdroid.hellochat.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.devdroid.hellochat.ChatActivity;
import com.devdroid.hellochat.Model.Chats;
import com.devdroid.hellochat.Model.User;
import com.devdroid.hellochat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class DisplayUsersAdapter extends RecyclerView.Adapter<DisplayUsersAdapter.ViewHolder> implements Filterable {

    private Context context;
    private ArrayList<User> userArrayList;
    private ArrayList<User> userArrayListFull;
    String theLastMessage,lastMessageTime;
    int isSeen,countNotSeen;

    public DisplayUsersAdapter(Context context,ArrayList<User> arrayList){
        this.context = context;
        this.userArrayList = arrayList;
        userArrayListFull = new ArrayList<User>(userArrayList);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.custom_row_user,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        final User user = userArrayList.get(position);

        holder.username.setText(user.getUsername());
        holder.email.setText(user.getEmail());

        lastMessage(user.getID(),holder.last_msg,holder.last_time,holder.txtNumUnseen,holder.rlUnseen);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("userid",user.getID());
                intent.putExtra("status",user.getStatus());
                context.startActivity(intent);
            }
        });


        FirebaseDatabase.getInstance().getReference("Users").child(user.getID()).child("profile").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()!=null)
                    Picasso.get().load(dataSnapshot.getValue().toString()).placeholder(R.drawable.ic_account_circle).into(holder.imgProfile);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    @Override
    public int getItemCount() {
        return userArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView username,last_msg,email,last_time,txtNumUnseen;
        CircleImageView imgProfile;
        RelativeLayout rlUnseen;

        public ViewHolder(@NonNull View itemView) {

            super(itemView);
            username = itemView.findViewById(R.id.username);
            email = itemView.findViewById(R.id.email);
            last_msg = itemView.findViewById(R.id.last_msg);
            imgProfile = itemView.findViewById(R.id.profile_img);
            last_time = itemView.findViewById(R.id.txtTime);
            txtNumUnseen = itemView.findViewById(R.id.txtNumUnseen);
            rlUnseen = itemView.findViewById(R.id.rlUnseen);
        }
    }

    @Override
    public Filter getFilter() {
        return UserFilter;
    }

    public Filter UserFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<User> filteredList = new ArrayList<>();
            if(constraint == null || constraint.length() == 0){
                filteredList.addAll(userArrayListFull);
            }
            else{
                String filterPattern= constraint.toString().toLowerCase().trim();

                for(User user: userArrayListFull){
                    if(user.getUsername().toLowerCase().contains(filterPattern)){
                        filteredList.add(user);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {

            userArrayList.clear();
            userArrayList.addAll((ArrayList<User>) results.values);
            notifyDataSetChanged();
        }
    };
    private void lastMessage(final String userid, final TextView last_msg, final TextView txtTime, final TextView txtNumUnseen, final RelativeLayout rlUnseen){

        countNotSeen = 0;
        theLastMessage = "default";
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Messages");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    Chats chats = snapshot.getValue(Chats.class);
                    if(firebaseUser!=null){
                        if(chats.getReceiver().equals(firebaseUser.getUid()) && chats.getSender().equals(userid) ||
                                chats.getReceiver().equals(userid) && chats.getSender().equals(firebaseUser.getUid())){
                            theLastMessage = chats.getMessage();


                            long chatTime =  chats.getTime();
                            long todayTime = System.currentTimeMillis();

                            Date dateObjectChat = new Date(chatTime);
                            Date dateObjectToday = new Date(todayTime);

                            String dateChatSplit[] = dateObjectChat.toString().split(" ");
                            String dateTodaySplit[] = dateObjectToday.toString().split(" ");

                            String dateChatToDisplay = dateChatSplit[1] + " " + dateChatSplit[2] + ", " + dateChatSplit[5];
                            String timeChatToDisplay = new SimpleDateFormat("h:mm a").format(dateObjectChat);

                            String dateTodayToDisplay = dateTodaySplit[1] + " " + dateTodaySplit[2] + ", " + dateTodaySplit[5];

                            if(chats.getReceiver().equals(firebaseUser.getUid()) && !chats.isIsseen()) {
                                isSeen = 0;
                                countNotSeen++;
                            }
                            else
                                isSeen = 1;

                            if(dateTodayToDisplay.equals(dateChatToDisplay)){
                                lastMessageTime = timeChatToDisplay;
                            }
                            else{
                                lastMessageTime = dateChatToDisplay;
                            }
                        }
                    }
                }

                switch (theLastMessage){
                    case "default":
                        last_msg.setText("No Message");

                        break;

                    default:
                        last_msg.setText(theLastMessage);

                        if(isSeen == 0) {
                            Log.d("Check:","" + countNotSeen);
                            rlUnseen.setVisibility(View.VISIBLE);
                            txtNumUnseen.setText("" + countNotSeen);
                            last_msg.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                            last_msg.setAlpha(1);
                        }
                        else{
                            rlUnseen.setVisibility(View.GONE);
                        }

                        txtTime.setText(lastMessageTime);
                        break;
                }
                theLastMessage = "default";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}