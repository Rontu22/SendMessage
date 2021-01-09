package com.example.sendmessage;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sendmessage.adapters.AdapterChat;
import com.example.sendmessage.models.ModelChat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;


public class ChatActivity extends AppCompatActivity {

    //views from xml
    Toolbar toolbar;
    RecyclerView recyclerView;
    ImageView profileIv;
    TextView nameTv,userStatusTv;
    EditText messageEt;
    ImageButton sendBtn;



    // add firebase auth
    FirebaseAuth firebaseAuth;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference usersDbRef;


    // for checking if use has seen message or not
    ValueEventListener seenListener;
    DatabaseReference userRefForSeen;

    ArrayList<ModelChat> chatArrayList;
    AdapterChat adapterChat;




    String hisUid;
    String myUid;
    String hisImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //init views
        toolbar = findViewById(R.id.toolbarChatActivity);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        recyclerView = findViewById(R.id.chat_recyclerView);
        profileIv =findViewById(R.id.profileIv);
        nameTv = findViewById(R.id.nameTv);
        //userStatusTv = findViewById(R.id.userStatusTv);
        messageEt = findViewById(R.id.messageEt);
        sendBtn = findViewById(R.id.sendBtn);


        // Layout for recyclerview
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);


        /*
        On clicking user form users list we have passed that user's UID using intent.
        So get that uid here to get the profile picture , name and start chat wiht that user
         */

        Intent intent = getIntent();
        hisUid = intent.getStringExtra("hisUid");


        // firebase auth instance
        firebaseAuth = FirebaseAuth.getInstance();


        // firebase database
        firebaseDatabase = FirebaseDatabase.getInstance();
        usersDbRef = firebaseDatabase.getReference("Users");


        // search user to get that user's info
        Query userQuery = usersDbRef.orderByChild("uid").equalTo(hisUid);

        // get user picture and name
        userQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // check untill required info is received
                for (DataSnapshot ds:snapshot.getChildren())
                {
                    String name = ""+ds.child("name").getValue();
                    hisImage = ""+ds.child("image").getValue();

                    // set data
                    nameTv.setText(name);
                    try {
                        // image received , set it to imageview in toolbar
                        Picasso.get().load(hisImage).placeholder(R.drawable.ic_default_img).into(profileIv);
                    }
                    catch (Exception e)
                    {
                        // there is exception getting picture, set default picture
                        Picasso.get().load(R.drawable.ic_default_img_profile).into(profileIv);
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        //click button to send message
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get text from editText
                String message = messageEt.getText().toString().trim();
                // check if text is empty or not
                if (TextUtils.isEmpty(message))
                {
                    // text is empty
                    Toast.makeText(com.example.sendmessage.ChatActivity.this,"Can't send empty message...",Toast.LENGTH_SHORT).show();

                }
                else
                {
                    // text is not empty
                    sendMessage(message);
                }

            }
        });



        readMessages();
        seenMessage();



        



    }

    private void seenMessage() {
        userRefForSeen = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = userRefForSeen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot ds:snapshot.getChildren())
                {
                    ModelChat chat = ds.getValue(ModelChat.class);

                    if (chat.getReceiver().equals(myUid) && chat.getSender().equals(hisUid))
                    {
                        HashMap<String, Object> hasSeenHashMap = new HashMap<>();
                        hasSeenHashMap.put("isSeen",true);
                        ds.getRef().updateChildren(hasSeenHashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void readMessages() {
        chatArrayList = new ArrayList<>();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Chats");
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatArrayList.clear();
                for (DataSnapshot ds: snapshot.getChildren())
                {
                    ModelChat chat = ds.getValue(ModelChat.class);
                    if (chat.getReceiver().equals(myUid) && chat.getSender().equals(hisUid))
                    {
                        chatArrayList.add(chat);
                    }
                    else if (chat.getReceiver().equals(hisUid) && chat.getSender().equals(myUid))
                    {
                        chatArrayList.add(chat);
                    }

                    // adapter
                    adapterChat = new AdapterChat(com.example.sendmessage.ChatActivity.this,chatArrayList,hisImage);
                    adapterChat.notifyDataSetChanged();

                    // set adapter to recyclerview
                    recyclerView.setAdapter(adapterChat);


                }



            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendMessage(String message) {
        /**Chats node will be created that will contain all chats
         * Whenever user sends message it will create new child in "Chats" node that child will
         * contain the following key values :
         * sender : UID of sender
         * receiver : UID of receiver
         * message : The actual message
         */


        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        String timestamp = String.valueOf((System.currentTimeMillis()));


        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("sender",myUid);
        hashMap.put("receiver",hisUid);
        hashMap.put("message",message);
        hashMap.put("timestamp",timestamp);
        hashMap.put("isSeen",false);

        databaseReference.child("Chats").push().setValue(hashMap);

        //reset editText after sending message
        messageEt.setText("");




    }

    private void checkUserStatus()
    {
        // get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null)
        {
            // user is signed in so stay here
            //mProfileTv.setText(user.getEmail());
            myUid = user.getUid(); // currently signed in user's uid


        }
        else
        {
            // user not signed in, go to MainActivity
            startActivity(new Intent(com.example.sendmessage.ChatActivity.this,MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onStart() {
        checkUserStatus();

        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        userRefForSeen.removeEventListener(seenListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);

        //hide searchview as it is not needed here
        menu.findItem(R.id.action_search).setVisible(false);



        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        //getitem id
        int id = item.getItemId();
        if (id == R.id.action_logout)
        {
            firebaseAuth.signOut();
            checkUserStatus();
        }


        return super.onOptionsItemSelected(item);
    }
}