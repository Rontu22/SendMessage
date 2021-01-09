package com.example.sendmessage;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sendmessage.adapters.AdapterUser;
import com.example.sendmessage.models.ModelUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class UsersFragment extends Fragment {

    RecyclerView recyclerView;
    AdapterUser adapterUser;
//
    ArrayList<ModelUser> userList;

    // firebase auth
    FirebaseAuth firebaseAuth;




    public UsersFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_users, container, false);


        //init
        firebaseAuth = FirebaseAuth.getInstance();

        //init recyclerview
        recyclerView = view.findViewById(R.id.users_recyclerView);

        // set it's properties
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
//
//
        // init user list
        userList = new ArrayList<>();
//
//
//        // get all users
        getAllUsers();

        return view;
    }

    private void getAllUsers() {
        // get current user
        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();

        //get path of database named "Users" containing users info
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");

        // get all data from path
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();

                for (DataSnapshot ds: snapshot.getChildren())
                {
                    ModelUser modelUser = ds.getValue(ModelUser.class);

                    // get all users except currently signed in user
                    if (!modelUser.getUid().equals(fUser.getUid()))
                    {
                        userList.add(modelUser);
                    }

                    // adapter
                    adapterUser = new AdapterUser(getActivity(),userList);

                    // set adapter to recyclerview
                    recyclerView.setAdapter(adapterUser);

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    private void searchUsers(String query) {

        // get current user
        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();

        //get path of database named "Users" containing users info
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");

        // get all data from path
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();

                for (DataSnapshot ds: snapshot.getChildren())
                {
                    ModelUser modelUser = ds.getValue(ModelUser.class);


                    /*
                    Conditions to fulfll search :
                        1. User not current user
                        2. The user name or email contains text entered in
                            SearchView (case Insensitive)
                     */

                    // get all searched users except currently signed in user
                    if (!modelUser.getUid().equals(fUser.getUid()))
                    {
                        if (modelUser.getName().toLowerCase().contains(query.toLowerCase())
                        || modelUser.getEmail().toLowerCase().contains(query.toLowerCase()))
                        {
                            userList.add(modelUser);
                        }

                    }

                    // adapter
                    adapterUser = new AdapterUser(getActivity(),userList);


                    // refresh adapter
                    adapterUser.notifyDataSetChanged();

                    // set adapter to recyclerview
                    recyclerView.setAdapter(adapterUser);

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }


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
            startActivity(new Intent(getActivity(),MainActivity.class));
            getActivity().finish();
        }
    }



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true); // to show menu option in fragment

        super.onCreate(savedInstanceState);
    }

    /*inflate options menu */

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //inflating menu
        inflater.inflate(R.menu.menu_main,menu);

        // SearchView
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView =(SearchView) MenuItemCompat.getActionView(item);

        // search listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                // Called when user press search
                if (!TextUtils.isEmpty(s.trim()))
                {
                    // search text contains text, search it
                    searchUsers(s);

                }
                else
                {
                    // search text empty , get all users
                    getAllUsers();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                // called when user press any single letter
                // if search query is not empty , then search
                if (!TextUtils.isEmpty(s.trim()))
                {
                    // search text contains text, search it
                    searchUsers(s);

                }
                else
                {
                    // search text empty , get all users
                    getAllUsers();
                }
                return false;
            }
        });


        super.onCreateOptionsMenu(menu,inflater);
    }


    /* handle menu item clicks */

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

//