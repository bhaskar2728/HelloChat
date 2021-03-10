package com.devdroid.hellochat.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.devdroid.hellochat.Model.Chats;
import com.devdroid.hellochat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class DisplayChatsAdapter extends RecyclerView.Adapter<DisplayChatsAdapter.ViewHolder> {

    public static final int MSG_LEFT = 0;
    public static final int MSG_RIGHT = 1;

    private Context context;
    private ArrayList<Chats> chatsArrayList;
    FirebaseUser firebaseUser;

    public DisplayChatsAdapter(Context context,ArrayList<Chats> arrayList){
        this.context = context;
        this.chatsArrayList = arrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == MSG_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.right_custom_row, parent, false);
            return new ViewHolder(view);
        }
        else{
            View view = LayoutInflater.from(context).inflate(R.layout.left_custom_row, parent, false);
            return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Chats chat = chatsArrayList.get(position);
        holder.message.setText(chat.getMessage());

        java.util.Date date=new java.util.Date();
        String date_str = date.toString().split(" ")[2] + " " + date.toString().split(" ")[1];


            holder.time.setText(chat.getTime());
            if(chat.isIsseen()){
                holder.txtSeen.setText("Seen");
            }
            else{
                holder.txtSeen.setText("Delivered");
            }



    }

    @Override
    public int getItemCount() {
        return chatsArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView message;
        TextView time;
        TextView txtSeen;

        public ViewHolder(@NonNull View itemView) {

            super(itemView);
            message = itemView.findViewById(R.id.message);
            time = itemView.findViewById(R.id.time);
            txtSeen = itemView.findViewById(R.id.txtSeen);
        }
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(chatsArrayList.get(position).getSender().equals(firebaseUser.getUid())){

            return MSG_RIGHT;
        }
        else {

            return MSG_LEFT;
        }
    }
}
