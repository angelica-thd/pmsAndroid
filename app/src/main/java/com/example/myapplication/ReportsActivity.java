package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ReportsActivity extends AppCompatActivity {
    BottomNavigationView bottomBar;
    private FirebaseAuth auth;
    TextView hellouser;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); //night mode ui is not supported
        hellouser = findViewById(R.id.hellouser);
        auth = FirebaseAuth.getInstance();


        currentUser = auth.getCurrentUser();
        hellouser.setText("Hello "+ currentUser.getDisplayName());
        //currentUser = getIntent().get("currentUser",false);
        bottomBar=findViewById(R.id.bottombar2);
        bottomBar.getMenu().getItem(2).setChecked(true);
        bottomBar.setOnNavigationItemSelectedListener(item -> {
            if(item.getItemId()==R.id.home){
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                overridePendingTransition(0,0);
                return true;
            }
            if(item.getItemId()==R.id.search){
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
                overridePendingTransition(0,0);
                return true;
            }
            return item.getItemId() == R.id.report;
        });

    }
}