package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;

public class SingleReportActivity extends AppCompatActivity {
    TextView locView,descrView,dateView,categoryView;
    ImageView imgRep;
    ProgressBar progressBar;
    private String location,description,date,reportID,category;
    private FirebaseUser currentUser;
    private FirebaseDatabase db;
    private FirebaseAuth auth;
    private DatabaseReference ref;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private FirebaseRemoteConfig remoteConfig;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_report);
        imgRep = findViewById(R.id.imgViewIncident);
        locView = findViewById(R.id.locationvalue);
        descrView = findViewById(R.id.descriptionvalue);
        dateView = findViewById(R.id.datetimevalue);
        progressBar = findViewById(R.id.progressbar);
        categoryView = findViewById(R.id.categoryvalue);

        db = FirebaseDatabase.getInstance("https://course9-b6dac-default-rtdb.europe-west1.firebasedatabase.app/");
        ref = db.getReference("reports");
        // get the Firebase  storage reference
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        auth = FirebaseAuth.getInstance();      //firebase authentication initialization
        currentUser = auth.getCurrentUser();

        remoteConfig = FirebaseRemoteConfig.getInstance();
        remoteConfig.setDefaultsAsync(R.xml.config_settings);
        remoteConfig.fetch(3).addOnCompleteListener(task -> remoteConfig.fetchAndActivate());

        location = getIntent().getStringExtra("location");
        date = getIntent().getStringExtra("datetime");
        description = getIntent().getStringExtra("description");
        reportID = getIntent().getStringExtra("reportID");
        category = getIntent().getStringExtra("category");

        try{
            locView.setText(location);
            dateView.setText(date);
            descrView.setText(description);
            categoryView.setText(category);
            File localfile = File.createTempFile("tmp","jpg") ;
            Log.e("ref",currentUser.getUid()+"/"+reportID);
            //TODO fix getting the right file
            StorageReference imgref = storageReference.child(currentUser.getUid()+"/"+reportID);
            imgref.getFile(localfile).addOnSuccessListener(taskSnapshot -> {
                imgRep.setVisibility(View.VISIBLE);
                imgRep.setImageBitmap(BitmapFactory.decodeFile(localfile.getAbsolutePath()));
            }).addOnProgressListener(
                    taskSnapshot -> {
                        double progress= (100.0* taskSnapshot.getBytesTransferred()/ taskSnapshot.getTotalByteCount());
                        Log.i("reportprogress", String.valueOf(progress));
                        if (progress<100) {
                            progressBar.setVisibility(View.VISIBLE);
                            progressBar.setProgress((int) progress);

                        } else {
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    });;
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    //progress bar sthn eikona, ftiakse ligo to layout na einai ola pio megala kai pio konta metaksu tous, top back button pio megalo kai pio visible h pio mesa isws kalytera
    public void back(View view){
        startActivity(new Intent(getApplicationContext(), MyReportsActivity.class));
    }
}