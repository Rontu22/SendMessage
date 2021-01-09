package com.example.sendmessage;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    // views
    Button mRegisterBtn,mLoginBtn;
//    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


//        actionBar = getSupportActionBar();
//        //actionBar.hide();
//        actionBar.setTitle("Send Message");






        // init views
        mRegisterBtn = findViewById(R.id.register_btn);
        mLoginBtn = findViewById(R.id.login_btn);

        // handle register btn click
        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // start register activity
                startActivity(new Intent(getApplicationContext(),RegisterActivity.class));

            }
        });

        // handle login button click
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // start login activity
                startActivity(new Intent(getApplicationContext(),LoginActivity.class));
            }
        });


    }
}