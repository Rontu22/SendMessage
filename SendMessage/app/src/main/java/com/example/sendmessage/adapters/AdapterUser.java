package com.example.sendmessage.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sendmessage.ChatActivity;
import com.example.sendmessage.R;
import com.example.sendmessage.models.ModelUser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterUser extends RecyclerView.Adapter<AdapterUser.MyHolder>{

    Context context;
    ArrayList<ModelUser> userList;

    // constructor
    public AdapterUser(Context context, ArrayList<ModelUser> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // inflate layout(row_user.xml)
        View view = LayoutInflater.from(context).inflate(R.layout.row_users,parent,false);


        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        //get data
        final String hisUID = userList.get(position).getUid();
        String userImage = userList.get(position).getImage();
        String userName = userList.get(position).getName();
        final String userEmail = userList.get(position).getEmail();

        // set data
        holder.mNameTV.setText(userName);
        holder.mEmailTV.setText(userEmail);



        try {
            Picasso.get().load(userImage)
                    .placeholder(R.drawable.ic_default_img)
                    .into(holder.mAvatarIV);
//            Picasso.get().load(userImage)
//                    .into(holder.mAvatarIV);
        }
        catch (Exception e)
        {
            Picasso.get().load(R.drawable.ic_add_image).into(holder.mAvatarIV);

        }


        //handle item click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("hisUid",hisUID);
                context.startActivity(intent);
            }
        });


//        holder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Toast.makeText(context,""+userEmail,Toast.LENGTH_SHORT).show();
//
//            }
//        });




    }

    @Override
    public int getItemCount() {

        return userList.size();
    }

    // view holder class
    class MyHolder extends RecyclerView.ViewHolder
    {
        ImageView mAvatarIV;
        TextView mNameTV,mEmailTV;





        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //init views
            mAvatarIV = itemView.findViewById(R.id.avatarIV1);
            mNameTV = itemView.findViewById(R.id.nameTV1);
            mEmailTV = itemView.findViewById(R.id.emailTV1);



        }
    }


}
