package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener, LocationListener {
    int day = 0, month = 0, year = 0, hour = 0, minute = 0, savedday = 0, savedmonth = 0, savedyear = 0, savedhour = 0, savedminute = 0;
    ImageView datetimePicker,locationView,incidentPhoto;
    TextView datetimeView,coord;
    private EditText description;
    private Context main;
    BottomNavigationView bottomBar;
    private LatLng current_loc;
    private FirebaseDatabase db;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private DatabaseReference ref;
    private LocationManager locationManager;
    private Uri filePath;
    private final int PICK_IMAGE_REQUEST = 22;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int  reqCode = 786;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); //night mode ui is not supported
        datetimePicker = findViewById(R.id.datetime);
        datetimeView = findViewById(R.id.datetimeview);
        locationView = findViewById(R.id.locationButton);
        description = findViewById(R.id.description);
        incidentPhoto = findViewById(R.id.incidentPhoto);
        main = this;

        db = FirebaseDatabase.getInstance("https://course9-b6dac-default-rtdb.europe-west1.firebasedatabase.app/");
        ref = db.getReference("reports");
        auth = FirebaseAuth.getInstance();      //firebase authentication initialization
        currentUser = auth.getCurrentUser();

        bottomBar=findViewById(R.id.bottombar);
        bottomBar.getMenu().getItem(1).setChecked(true);
        bottomBar.setOnNavigationItemSelectedListener(item -> {
            if(item.getItemId()==R.id.report){
                startActivity(new Intent(getApplicationContext(), ReportsActivity.class));
                overridePendingTransition(0,0);
                return true;
            }
            if(item.getItemId()==R.id.home){
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
                overridePendingTransition(0,0);
                return true;
            }
            return item.getItemId() == R.id.search;
        });

        coord = findViewById(R.id.locationtxt);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE); //location services initialization
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, reqCode);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, reqCode);
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        locationView.setOnLongClickListener(view -> {
            Toast.makeText(getApplicationContext(), R.string.ownLoc, Toast.LENGTH_LONG).show();
            return true;
        });
        locationView.setOnClickListener(view ->{
            StringBuilder gps_address = new StringBuilder();
            try {
                if (current_loc != null){
                    Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                    List<Address> addresses = geocoder.getFromLocation(current_loc.latitude, current_loc.longitude, 1);
                    if (addresses.size() > 0) {
                        Address address = addresses.get(0);
                        gps_address.append(address.getAddressLine(0));
                        coord.setText(gps_address.toString());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }

    private void getDateTimeCalendar(){
        final Calendar cal = Calendar.getInstance();
        day = cal.get(Calendar.DAY_OF_MONTH);
        month = cal.get(Calendar.MONTH);
        year = cal.get(Calendar.YEAR);
        hour = cal.get(Calendar.HOUR);
        minute = cal.get(Calendar.MINUTE);
    }

    public void pickDate(View view){
        getDateTimeCalendar();
        new DatePickerDialog(main, R.style.DialogTheme,this, year, month, day).show();

    }
    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
        savedday = dayOfMonth;
        savedmonth = month;
        savedyear = year;
        getDateTimeCalendar();
        new TimePickerDialog(main,R.style.DialogTheme, this, hour,minute,true).show();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
        savedhour = hourOfDay;
        savedminute = minute;
        Log.i("DATEPICKER",savedday+"."+savedhour+"."+savedminute);
        datetimeView.setText(savedday+"/" + savedmonth + "/" + savedyear +", " + savedhour + ":" + savedminute);
    }


    public void dispatchTakePictureIntent(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } catch (ActivityNotFoundException e) {
            // display error state to the user
        }
    }


    public void selectImg(View view)
    {
        // Defining Implicit Intent to mobile gallery
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser( intent,  "Select Image from here..."),PICK_IMAGE_REQUEST);
    }
    // Override onActivityResult method
    @Override
    protected void onActivityResult(int requestCode,int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        // checking request code and result code
        // if request code is PICK_IMAGE_REQUEST and
        // resultCode is RESULT_OK
        // then set image in the image view
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            // Get the Uri of data
            filePath = data.getData();
            try {
                // Setting image on image view using Bitmap
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),filePath);
                incidentPhoto.setImageBitmap(bitmap);
            }
            catch (IOException e) {
                // Log the exception
                e.printStackTrace();
            }
        }
        else  if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            incidentPhoto.setImageBitmap(imageBitmap);
        }
    }

    // UploadImage method
    private void uploadImage()
    {
        if (filePath != null) {

            // Code for showing progressDialog while uploading
            ProgressDialog progressDialog  = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();


        }
    }

    public void report(View view){
        Report report = new Report();
        report.setTimestamp(datetimeView.getText().toString());
        report.setDescription(description.getText().toString());
        report.setLocation(coord.getText().toString());

        ref.child(currentUser.getUid()).push().setValue(report).addOnCompleteListener(task -> {
                                    if(task.isSuccessful()){
                                        Log.i("click","ref");
                                        startActivity(new Intent(this,MainActivity.class));
                                    }
                                    else  Log.i("click",task.getException().getMessage());

                                }).addOnFailureListener(e -> Log.i("click",e.getMessage()));
    }
    @Override
    public void onLocationChanged(@NonNull Location location) {
        if (location!=null){
            current_loc = new LatLng(location.getLatitude(),location.getLongitude());
        }
    }

    @Override
    public void onLocationChanged(@NonNull List<Location> locations) {
    }

    @Override
    public void onFlushComplete(int requestCode) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
    }
}

