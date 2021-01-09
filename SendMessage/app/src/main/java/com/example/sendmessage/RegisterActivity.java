package com.example.sendmessage;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    //views
    EditText mEmailEt,mPasswordEt;
    Button mRegisterBtn;
    TextView mHaveAccountTv;

    // progressBar to dispaly while registering users
    ProgressDialog progressDialog;


    //Declare an instance of FirebaseAuth
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Actionbar and its title
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Create Account");

        //enable back button
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        // init
        mEmailEt = findViewById(R.id.emailEt);
        mPasswordEt = findViewById(R.id.passwordEt);
        mRegisterBtn = findViewById(R.id.register_Btn);
        mHaveAccountTv = findViewById(R.id.have_account_TV);



        //In the onCreate() method, initialize the FirebaseAuth instance.
        mAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registering user...");



        //handle register btn clicked
        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //input email and password
                String email = mEmailEt.getText().toString().trim();
                String password = mPasswordEt.getText().toString();


                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
                {
                    //set error and focus to email EditText
                    mEmailEt.setError("Invalid Email");
                    mEmailEt.setFocusable(true);
                }
                else if (password.length() < 6)
                {
                    mPasswordEt.setError("Password length must be at least 6 characters. ");
                    mPasswordEt.setFocusable(true);
                }
                else {
                    registerUser(email,password);
                }


            }
        });

        // handle login textview click listener
        mHaveAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(com.example.sendmessage.RegisterActivity.this,LoginActivity.class));
                finish();
            }
        });


    }

    private void registerUser(String email, String password) {
        // email and password patten is valid , show progress dialog
        // and start registering users
        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success , dismiss dialog and start registerActivity
                            progressDialog.dismiss();

                            FirebaseUser user = mAuth.getCurrentUser();

                            //get-user email and uid from auth
                            String email = user.getEmail().toString();
                            String uid = user.getUid();

                            //When user is registered store user info in the
                            //firebase realtime database too using HashMap
                            HashMap<Object,String> hashMap = new HashMap<>();
                            //put these info in HashMap
                            hashMap.put("email",email);
                            hashMap.put("uid",uid);
                            hashMap.put("name",""); // will add later (e.g. edit profile)
                            hashMap.put("phone","");// will add later (e.g. edit profile)
                            hashMap.put("image","");// will add later (e.g. edit profile)
                            hashMap.put("cover","");

                            // firebase database instance
                            FirebaseDatabase database = FirebaseDatabase.getInstance();

                            // path to store user data names "Users"
                            DatabaseReference reference = database.getReference("Users");

                            // put data within hashMap in database
                            reference.child(uid).setValue(hashMap);






                            Toast.makeText(com.example.sendmessage.RegisterActivity.this,"Registered...\n"+user.getEmail(),Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(com.example.sendmessage.RegisterActivity.this,ProfileActivity.class));
                            finish();



                        } else {
                            progressDialog.dismiss();
                            // If sign in fails, display a message to the user.
                            Toast.makeText(com.example.sendmessage.RegisterActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(com.example.sendmessage.RegisterActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });



    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // go to previous activity

        return super.onSupportNavigateUp();
    }
}