package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AllReportsActivity extends AppCompatActivity {
    BottomNavigationView bottomBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_reports);
        //currentUser = getIntent().get("currentUser",false);
        bottomBar=findViewById(R.id.bottombarall);
        bottomBar.getMenu().getItem(1).setChecked(true);
        bottomBar.setOnNavigationItemSelectedListener(item -> {
            if(item.getItemId()==R.id.new_report){
                startActivity(new Intent(getApplicationContext(), NewReportActivity.class));
                overridePendingTransition(0,0);
                return true;
            }
            if(item.getItemId()==R.id.my_reports){
                startActivity(new Intent(getApplicationContext(), MyReportsActivity.class));
                overridePendingTransition(0,0);
                return true;
            }
            if(item.getItemId()==R.id.Logout){
                startActivity(new Intent(getApplicationContext(), Login.class).putExtra("logout",true));
                overridePendingTransition(0,0);
                return true;
            }
            return item.getItemId() == R.id.all_reports;
        });
    }


}