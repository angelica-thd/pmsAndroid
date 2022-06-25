package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.utils.GoogleUtils;
import com.example.myapplication.utils.Report;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ReportsActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {
    BottomNavigationView bottomBar;
    Spinner dropdown;
    private FirebaseAuth auth;
    private GoogleMap mMap;
    TextView hellouser;
    ImageView imgRep;
    private String category;
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

    @SuppressLint("SetTextI18n")
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
        hellouser.setText("Γειά σου "+ currentUser.getDisplayName());



        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE); //location services initialization
        gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(!gps_enabled) {
            // notify user
            new android.app.AlertDialog.Builder(new ContextThemeWrapper(this, R.style.DialogTheme))
                    .setMessage(R.string.gps_network_not_enabled)
                    .setPositiveButton(R.string.open_location_settings, (paramDialogInterface, paramInt) -> startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                    .setNegativeButton(R.string.cancel,null)
                    .show();
        }
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, reqCode);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, reqCode);
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);



        //DROPDOWN
        //get the spinner from the xml.
        dropdown = findViewById(R.id.filter);
        //create a list of items for the spinner.
        String[] items = new String[]{"Ελλειπής Συντήρηση Δρόμων", "Ελλειπής Συντήρηση Παλαιών Κτιρίων", "Έλλειψη Μέσων Μαζικής Μεταφοράς στην Περιοχή", "Βλάβες/Καταστροφές", "Έλλειψη Θέσεις Πάρκινγκ", "Ρύπανση", "Ελλειπής Απομάκρυνση Απορριμάτων", "Εγκληματικότητα", "Βανδαλισμοί", "Άλλο"};
        //create an adapter to describe how the items are displayed, adapters are used in several places in android.
        //There are multiple variations of this, but this is the basic variant.
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item ,items);
        //set the spinners adapter to the previously created one.
        adapter.setDropDownViewResource(R.layout.spinner_list);
        dropdown.setAdapter(adapter);
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                category = parent.getItemAtPosition(pos).toString();

                List<Report> reports = new ArrayList<>();
                ref.addValueEventListener(new ValueEventListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot user : snapshot.getChildren()) {
                            Log.i("report", user.getKey());
                            for (DataSnapshot report : user.getChildren()) {
                                Report rep = new Report(String.valueOf(report.child("id").getValue()));
                                rep.setLocation(String.valueOf(report.child("location").getValue()));
                                rep.setLatitude(Double.parseDouble(String.valueOf(report.child("latitude").getValue())));
                                rep.setLongitude(Double.parseDouble(String.valueOf(report.child("longitude").getValue())));
                                rep.setType(String.valueOf(report.child("type").getValue()));
                                rep.setDescription(String.valueOf(report.child("description").getValue()));
                                rep.setTimestamp(String.valueOf(report.child("timestamp").getValue()));

                                reports.add(rep);
                                List<Report> reportsByCategory = reports.stream().filter(x -> x.getType().equals(category)).collect(Collectors.toList());

                                if (mMap != null) {
                                    new GoogleUtils().addMarkerFromReport(mMap, reportsByCategory, user, currentUser);
                                } else
                                    Toast.makeText(mapFragment.getActivity(), R.string.map_not_ready, Toast.LENGTH_LONG).show();
                            }
                        }
                        mMap.setOnMarkerClickListener((GoogleMap.OnMarkerClickListener) marker -> {
                            List<Report> reps = reports.stream().filter(x -> new LatLng(x.getLatitude(), x.getLongitude()).equals(marker.getPosition())).collect(Collectors.toList());
                            Report report = reps.get(reps.size() - 1);
                            startActivity(new Intent(getApplicationContext(), SingleReportActivity.class)
                                    .putExtra("location", report.getLocation())
                                    .putExtra("description", report.getDescription())
                                    .putExtra("datetime", report.getTimestamp())
                                    .putExtra("category", report.getType())
                                    .putExtra("reportID", report.getId()));

                            return true;
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.i("fire_error", error.getMessage());
                    }
                });

            }

            public void onNothingSelected(AdapterView parent) {
                List<Report> reports = new ArrayList<>();
                ref.addValueEventListener(new ValueEventListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot user : snapshot.getChildren()) {
                            Log.i("report", user.getKey());
                            for (DataSnapshot report : user.getChildren()) {
                                Report rep = new Report(String.valueOf(report.child("id").getValue()));
                                rep.setLocation(String.valueOf(report.child("location").getValue()));
                                rep.setLatitude(Double.parseDouble(String.valueOf(report.child("latitude").getValue())));
                                rep.setLongitude(Double.parseDouble(String.valueOf(report.child("longitude").getValue())));
                                rep.setType(String.valueOf(report.child("type").getValue()));
                                rep.setDescription(String.valueOf(report.child("description").getValue()));
                                rep.setTimestamp(String.valueOf(report.child("timestamp").getValue()));

                                reports.add(rep);

                                if (mMap != null) {
                                    new GoogleUtils().addMarkerFromReport(mMap, reports, user, currentUser);
                                } else
                                    Toast.makeText(mapFragment.getActivity(), R.string.map_not_ready, Toast.LENGTH_LONG).show();
                            }
                        }
                        assert mMap != null;
                        mMap.setOnMapClickListener(latLng -> {
                            startActivity(new Intent(getApplicationContext(), NewReportActivity.class).putExtra("mapclick", latLng));
                        });
                        mMap.setOnMarkerClickListener((GoogleMap.OnMarkerClickListener) marker -> {
                            List<Report> reps = reports.stream().filter(x -> new LatLng(x.getLatitude(), x.getLongitude()).equals(marker.getPosition())).collect(Collectors.toList());
                            Report report = reps.get(reps.size() - 1);
                            startActivity(new Intent(getApplicationContext(), SingleReportActivity.class)
                                    .putExtra("location", report.getLocation())
                                    .putExtra("description", report.getDescription())
                                    .putExtra("datetime", report.getTimestamp())
                                    .putExtra("category", report.getType())
                                    .putExtra("reportID", report.getId()));
                            return true;
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.i("fire_error", error.getMessage());
                    }
                });
            }
        });


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
            }
            return item.getItemId() == R.id.all_reports;
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
            mMap.setOnMapClickListener(latLng -> {
                startActivity(new Intent(getApplicationContext(), NewReportActivity.class)
                        .putExtra("mapclick", latLng)
                        .putExtra("from","maps"));
            });

        }
    }
}