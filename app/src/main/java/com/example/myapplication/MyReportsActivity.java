package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MyReportsActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {
    BottomNavigationView bottomBar;
    private FirebaseAuth auth;
    private GoogleMap mMap;
    TextView hellouser,locRep,descrRep,dateRep;
    ImageView imgRep;
    private Report rep;
    private LatLng current_loc;
    private FirebaseUser currentUser;
    private FirebaseDatabase db;
    private DatabaseReference ref;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private FirebaseRemoteConfig remoteConfig;
    private boolean gps_enabled = false;

    private LocationManager locationManager;
    private static final int  reqCode = 786;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); //night mode ui is not supported
        hellouser = findViewById(R.id.hellouser);

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


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE); //location services initialization

        //boolean network_enabled = false;

        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

//        try {
//            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
//        } catch(Exception ex) {}


        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, reqCode);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, reqCode);
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

        List<Report> reports = new ArrayList<>();
        ref.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot myreport : snapshot.child(currentUser.getUid()).getChildren()) {
                    String id = String.valueOf(myreport.child("id").getValue());
                    String location = String.valueOf(myreport.child("location").getValue());
                    Double lat = Double.valueOf(String.valueOf(myreport.child("latitude").getValue()));
                    Double lon = Double.valueOf(String.valueOf(myreport.child("longitude").getValue()));
                    String description = String.valueOf(myreport.child("description").getValue());
                    String timestamp = String.valueOf(myreport.child("timestamp").getValue());

                    Report report = new Report(id);
                    report.setLocation(location);
                    report.setLatitude(lat);
                    report.setLongitude(lon);
                    report.setDescription(description);
                    report.setTimestamp(timestamp);

                    reports.add(report);


                    if (mMap != null) {
                        LatLng loc = new LatLng(lat, lon);
                        MarkerOptions my_options = new MarkerOptions().position(loc).title(location);
                        my_options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                        mMap.addMarker(my_options);
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 15));
                    }else Toast.makeText(mapFragment.getActivity(), R.string.map_not_ready, Toast.LENGTH_LONG).show();

            }

                assert mMap != null;
//                mMap.setOnMapLongClickListener((GoogleMap.OnMapLongClickListener) marker ->{
//
//
//                });
                mMap.setOnMarkerClickListener((GoogleMap.OnMarkerClickListener) marker -> {
                    List<Report> reps = reports.stream().filter(x -> new LatLng(x.getLatitude(), x.getLongitude()).equals(marker.getPosition())).collect(Collectors.toList());
                    Report report = reps.get(reps.size()-1);
                    startActivity(new Intent(getApplicationContext(), SingleReportActivity.class)
                            .putExtra("location",report.getLocation())
                            .putExtra("description",report.getDescription())
                            .putExtra("datetime",report.getTimestamp())
                            .putExtra("reportID",report.getId()));

                    return true;
                    });
                }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i("fire_error",error.getMessage());
            }
        });


        hellouser.setText("Hello "+ currentUser.getDisplayName());
        //currentUser = getIntent().get("currentUser",false);
        bottomBar=findViewById(R.id.bottombar2);
        bottomBar.getMenu().getItem(0).setChecked(true);
        bottomBar.setOnNavigationItemSelectedListener(item -> {
            if(item.getItemId()==R.id.new_report){
                startActivity(new Intent(getApplicationContext(), NewReportActivity.class));
                overridePendingTransition(0,0);
                return true;
            }
            if(item.getItemId()==R.id.Logout){
                startActivity(new Intent(getApplicationContext(), Login.class).putExtra("logout",true));
                overridePendingTransition(0,0);
                return true;
            }if(item.getItemId()==R.id.all_reports){
                startActivity(new Intent(getApplicationContext(), AllReportsActivity.class));
                overridePendingTransition(0,0);
                return true;
            }
            return item.getItemId() == R.id.my_reports;
        });

    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        if (location!=null){
            current_loc = new LatLng(location.getLatitude(),location.getLongitude());
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        if(!gps_enabled) {
            // notify user
            new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.DialogTheme))
                    .setMessage(R.string.gps_network_not_enabled)
                    .setPositiveButton(R.string.open_location_settings, (paramDialogInterface, paramInt) -> startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                    .setNegativeButton(R.string.cancel,null)
                    .show();
        }
        mMap = googleMap;
        if (mMap==null){
            Toast.makeText(this,R.string.map_not_ready,Toast.LENGTH_LONG).show();
        }else{
            if(current_loc != null){
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
                mMap.addCircle(new CircleOptions()
                        .center(current_loc)
                        .radius(10000)
                        .strokeColor(Color.RED)
                        .fillColor(Color.BLUE));
            }

        }
    }
}