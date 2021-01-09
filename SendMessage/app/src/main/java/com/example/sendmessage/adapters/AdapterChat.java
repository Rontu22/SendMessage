package com.example.sendmessage.adapters;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sendmessage.R;
import com.example.sendmessage.models.ModelChat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class AdapterChat extends RecyclerView.Adapter<AdapterChat.MyHolder> {


    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;
    Context context;
    ArrayList<ModelChat> chatArrayList;
    String imageUrl;

    FirebaseUser fUser;



    public AdapterChat(Context context, ArrayList<ModelChat> chatArrayList, String imageUrl) {
        this.context = context;
        this.chatArrayList = chatArrayList;
        this.imageUrl = imageUrl;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layouts : row_chat.xml for receier, row_chat_right.xml for sender
        if (viewType == MSG_TYPE_RIGHT)
        {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_right,parent,false);
            return new MyHolder(view);
        }
        else
        {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_left,parent,false);
            return new MyHolder(view);
        }


    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        // get data
        String message = chatArrayList.get(position).getMessage();
        String timeStamp = chatArrayList.get(position).getTimestamp();

        // convert time stamp to dd/mm/yyyy hh:mm am/pm
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(Long.parseLong(timeStamp));
        String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa",cal).toString();

        //set data
        holder.messageTv.setText(message);
        holder.timeTv.setText(dateTime);

        try {
            Picasso.get().load(imageUrl).into(holder.profileIv);

        }
        catch (Exception e)
        {

        }
        // set seen/delivered status of message
        if (position == chatArrayList.size()-1)
        {
            if (chatArrayList.get(position).isSeen())
            {
                holder.isSeenTv.setText("Seen");
            }
            else
            {
                holder.isSeenTv.setText("Delivered");
            }
        }
        else
        {
            holder.isSeenTv.setVisibility(View.GONE);
        }




    }

    @Override
    public int getItemCount() {
        return chatArrayList.size();
    }


    @Override
    public int getItemViewType(int position) {
        // get current user
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        if (chatArrayList.get(position).getSender().equals(fUser.getUid()))
        {
            return MSG_TYPE_RIGHT;
        }
        else
        {
            return MSG_TYPE_LEFT;
        }

    }

    // view holder class
    class MyHolder extends RecyclerView.ViewHolder{

        //views
        ImageView profileIv;
        TextView messageTv,timeTv,isSeenTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);


            //init views
            profileIv = itemView.findViewById(R.id.profileIv);
            messageTv = itemView.findViewById(R.id.messageTv);
            timeTv = itemView.findViewById(R.id.timeTv);
            isSeenTv = itemView.findViewById(R.id.isSeenTv);


        }
    }
}



