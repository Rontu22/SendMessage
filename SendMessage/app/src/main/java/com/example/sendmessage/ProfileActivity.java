package com.example.sendmessage;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {
    // firebase auth
    FirebaseAuth firebaseAuth;


    //views
    //TextView mProfileTv;
    ActionBar actionBar;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


//        menuItem = menu.findItem(R.id.action_search);
//        menuItem.setVisible(true);

        //Actionbar and its title.
        actionBar = getSupportActionBar();
        //actionBar.hide();
        actionBar.setTitle("ProfileActivity");




//        //enable back button
//        actionBar.setDisplayHomeAsUpEnabled(true);
//        actionBar.setDisplayShowHomeEnabled(true);


        //inti firebaseauth
        firebaseAuth = FirebaseAuth.getInstance();

        // init
        //mProfileTv = findViewById(R.id.profileTV);

        BottomNavigationView navigationView = findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(selectedListener);




        //home fragment transition (default, on start)
//        actionBar.setTitle("Home");
//        HomeFragment homeFragment = new HomeFragment();
//        FragmentTransaction fragmentTransactionHome = getSupportFragmentManager().beginTransaction();
//        fragmentTransactionHome.replace(R.id.content,homeFragment,"Transaction to home fragment");
//        fragmentTransactionHome.commit();


        // users fragment (default, on start)
        actionBar.setTitle("Users");
        com.example.sendmessage.UsersFragment usersFragment = new com.example.sendmessage.UsersFragment();
        FragmentTransaction fragmentTransactionUsers = getSupportFragmentManager().beginTransaction();
        fragmentTransactionUsers.replace(R.id.content,usersFragment,"Transaction to Users fragment");
        fragmentTransactionUsers.commit();

    }

//    @Override
//    public boolean onSupportNavigateUp() {
//        onBackPressed();
//        return super.onSupportNavigateUp();
//    }

    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    //handle item clicks
                    switch (item.getItemId())
                    {
//                        case R.id.nav_home:
//                            //home fragment transition
//                            actionBar.setTitle("Home");
//                            HomeFragment homeFragment = new HomeFragment();
//                            FragmentTransaction fragmentTransactionHome = getSupportFragmentManager().beginTransaction();
//                            fragmentTransactionHome.replace(R.id.content,homeFragment,"Transaction to home fragment");
//                            fragmentTransactionHome.commit();
//
//                            return true;
                        case R.id.nav_profile:
                            //profile fragment transition
                            actionBar.setTitle("Profile");
                            com.example.sendmessage.ProfileFragment profileFragment = new com.example.sendmessage.ProfileFragment();
                            FragmentTransaction fragmentTransactionProfile = getSupportFragmentManager().beginTransaction();
                            fragmentTransactionProfile.replace(R.id.content,profileFragment,"Transaction to profile fragment");
                            fragmentTransactionProfile.commit();
                            return true;
                        case R.id.nav_users:
                            //users fragment transition
                            actionBar.setTitle("Users");
                            com.example.sendmessage.UsersFragment usersFragment = new com.example.sendmessage.UsersFragment();
                            FragmentTransaction fragmentTransactionUsers = getSupportFragmentManager().beginTransaction();
                            fragmentTransactionUsers.replace(R.id.content,usersFragment,"Transaction to Users fragment");
                            fragmentTransactionUsers.commit();
                            return true;
//                        case R.id.nav_chat:
//                            //users fragment transition
//                            actionBar.setTitle("Chats");
//                            ChatListFragment chatFragment = new ChatListFragment();
//                            FragmentTransaction fragmentTransactionChats = getSupportFragmentManager().beginTransaction();
//                            fragmentTransactionChats.replace(R.id.content,chatFragment,"Transaction to Users fragment");
//                            fragmentTransactionChats.commit();
//                            return true;



                    }

                    return false;
                }
            };

    private void checkUserStatus()
    {
        // get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null)
        {
            // user is signed in so stay here
            //mProfileTv.setText(user.getEmail());

        }
        else
        {
            // user not signed in, go to MainActivity
            startActivity(new Intent(com.example.sendmessage.ProfileActivity.this,MainActivity.class));
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onStart() {
        // check on start of the app
        checkUserStatus();

        super.onStart();
    }

}