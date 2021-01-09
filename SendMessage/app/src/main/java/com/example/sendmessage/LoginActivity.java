package com.example.sendmessage;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN =100 ;
    GoogleSignInClient mGoogleSignInClient;
    //views
    EditText mEmailEt,mPasswordEt;
    TextView not_Have_Account_TV,mRecoverPasswordTV;
    Button mLoginBtn;
    //SignInButton mGoogleSignInBtn;


    // Declare an instance of the FirebaseAuth
    private FirebaseAuth mAuth;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        // Actionbar and its title
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Login");

        //enable back button
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);



        // before mAuth
        // Configure Google Sign In
//        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestIdToken(getString(R.string.default_web_client_id))
//                .requestEmail()
//                .build();

        //mGoogleSignInClient = GoogleSignIn.getClient(this,gso);

        //init
        mAuth = FirebaseAuth.getInstance();


        //init
        mEmailEt = findViewById(R.id.emailEt);
        mPasswordEt = findViewById(R.id.passwordEt);
        not_Have_Account_TV = findViewById(R.id.not_have_account_TV);
        mLoginBtn = findViewById(R.id.login_Btn);
        mRecoverPasswordTV = findViewById(R.id.recoverPassTV);
        //mGoogleSignInBtn = findViewById(R.id.googleLoginBtn);

        // login button click
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //input data
                String email = mEmailEt.getText().toString();
                String password = mPasswordEt.getText().toString().trim();

                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
                {
                    // invalid email pattern
                    mEmailEt.setError("Invalid Email");
                    mEmailEt.setFocusable(true);
                }
                else
                {
                    //valid email pattern
                    loginUser(email,password);
                }

            }
        });
        //not have account textview click
        not_Have_Account_TV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(com.example.sendmessage.LoginActivity.this, com.example.sendmessage.RegisterActivity.class));
                finish();
            }
        });
        // recover password textview click
        mRecoverPasswordTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRecoverPasswordDialog();
            }
        });

        //handle google login btn click
//        mGoogleSignInBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                //begin google login process
//                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
//                startActivityForResult(signInIntent, RC_SIGN_IN);
//
//            }
//        });



        //init progress dialog
        progressDialog = new ProgressDialog(com.example.sendmessage.LoginActivity.this);



    }

    private void showRecoverPasswordDialog() {
        //AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Recover Password ?");


        //set layout linear layout
        LinearLayout linearLayout = new LinearLayout(this);
        // views to set in dialog
        final EditText emailEt = new EditText(this);
        emailEt.setHint("Email ");
        emailEt.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        /*Sets the min width of the EditView to fit a text of n 'M' letters
        * regardless of the actual text extension and text size*/
        emailEt.setMinEms(16);



        linearLayout.addView(emailEt);
        linearLayout.setPadding(10,10,10,10);


        builder.setView(linearLayout);


        //buttons recover
        builder.setPositiveButton("Recover", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // input email
                String email = emailEt.getText().toString().trim();
                beginRecovery(email);

            }
        });
        //buttons cancel
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //dismiss dialog
                dialogInterface.dismiss();
            }
        });


        // show dialog
        builder.create().show();






    }

    private void beginRecovery(String email) {

        // show progress dialog
        progressDialog.setMessage("Sending email...");
        progressDialog.show();

    mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
        @Override
        public void onComplete(@NonNull Task<Void> task) {
            progressDialog.dismiss();
            if (task.isSuccessful())
            {
                Toast.makeText(com.example.sendmessage.LoginActivity.this,"Email sent successfully",Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(com.example.sendmessage.LoginActivity.this, "Failed...", Toast.LENGTH_SHORT).show();
            }
        }
    }).addOnFailureListener(new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
            progressDialog.dismiss();
            // show proper error message
            Toast.makeText(com.example.sendmessage.LoginActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();


        }
    });

    }

    private void loginUser(String email, String password) {

        // show progress dialog
        progressDialog.setMessage("Logging In...");
        progressDialog.show();



        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            progressDialog.dismiss();
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();


                            //if user is signing in first time then get and show info from
                            // google account
                            if (task.getResult().getAdditionalUserInfo().isNewUser())
                            {
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
                            }



                            startActivity(new Intent(com.example.sendmessage.LoginActivity.this, com.example.sendmessage.ProfileActivity.class));
                            finish();
                        } else {
                            progressDialog.dismiss();
                            // If sign in fails, display a message to the user.
                            Toast.makeText(com.example.sendmessage.LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(com.example.sendmessage.LoginActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                //Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(com.example.sendmessage.LoginActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            //Log.d(TAG, "signInWithCredential:success");
                            //Show user email in the toast.


                            FirebaseUser user = mAuth.getCurrentUser();
                            // show the user email in the toast
                            Toast.makeText(com.example.sendmessage.LoginActivity.this, user.getEmail(), Toast.LENGTH_SHORT).show();
                            //go to profileactivity
                            startActivity(new Intent(com.example.sendmessage.LoginActivity.this, com.example.sendmessage.ProfileActivity.class));
                            finish();
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(com.example.sendmessage.LoginActivity.this, "Login Failed...", Toast.LENGTH_SHORT).show();
                            //Log.w(TAG, "signInWithCredential:failure", task.getException());
                            //updateUI(null);
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // get and show error message
                Toast.makeText(com.example.sendmessage.LoginActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();

            }
        });
    }


}