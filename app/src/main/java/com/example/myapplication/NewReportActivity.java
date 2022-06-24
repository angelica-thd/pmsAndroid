package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class NewReportActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener, LocationListener {
    int day = 0, month = 0, year = 0, hour = 0, minute = 0, savedday = 0, savedmonth = 0, savedyear = 0, savedhour = 0, savedminute = 0;
    ImageView datetimePicker,locationView,incidentPhoto;
    TextView datetimeView,coord;
    FloatingActionButton fab;
    Spinner dropdown;
    ProgressBar progressBar;
    private EditText description;
    private Context main;
    BottomNavigationView bottomBar;
    private LatLng current_loc,user_input_loc;
    private double current_lat = -99, current_lon = -99;
    private String category;
    private FirebaseDatabase db;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private DatabaseReference ref;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private LocationManager locationManager;
    private Uri filePath;
    private boolean myloc = false;
    private final int PICK_IMAGE_REQUEST = 22;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int  reqCode = 786;
    private Context context;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); //night mode ui is not supported
        datetimePicker = findViewById(R.id.datetime);
        datetimeView = findViewById(R.id.datetimevalue);
        locationView = findViewById(R.id.locationButton);
        description = findViewById(R.id.descriptionvalue);
        incidentPhoto = findViewById(R.id.incidentPhoto);
        progressBar = findViewById(R.id.progress_view);
        fab = findViewById(R.id.report_fab);
        main = this;

        db = FirebaseDatabase.getInstance("https://course9-b6dac-default-rtdb.europe-west1.firebasedatabase.app/");
        ref = db.getReference("reports");
        // get the Firebase  storage reference
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        auth = FirebaseAuth.getInstance();      //firebase authentication initialization
        currentUser = auth.getCurrentUser();

        //get the spinner from the xml.
        dropdown = findViewById(R.id.category);
        //create a list of items for the spinner.
        String[] items = new String[]{"Ελλειπής Συντήρηση Δρόμων", "Ελλειπής Συντήρηση Παλαιών Κτιρίων", "Έλλειψη Μέσων Μαζικής Μεταφοράς στην Περιοχή", "Βλάβες/Καταστροφές", "Έλλειψη Θέσεις Πάρκινγκ", "Ρύπανση", "Ελλειπής Απομάκρυνση Απορριμάτων", "Εγκληματικότητα", "Βανδαλισμοί", "Άλλο"};

        //create an adapter to describe how the items are displayed, adapters are used in several places in android.
        //There are multiple variations of this, but this is the basic variant.
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item ,items);
        //set the spinners adapter to the previously created one.
        adapter.setDropDownViewResource(R.layout.spinner_list);
        dropdown.setAdapter(adapter);
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent,View view, int pos, long id) {
                category = parent.getItemAtPosition(pos).toString();
            }

            public void onNothingSelected(AdapterView parent) {
                Toast toast = Toast.makeText(context, R.string.select_category, Toast.LENGTH_LONG);
                View v = toast.getView();
                v.setBackgroundResource(R.drawable.error_toast);
                TextView t = (TextView) toast.getView().findViewById(android.R.id.message);
                t.setTextColor(Color.RED);
                toast.show();
            }
        });

//        boolean gps_enabled = false, network_enabled = false;
//        try {
//            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
//            Log.e("GPS","kleisto");
//        } catch(Exception ex) {}
//
//        try {
//            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
//        } catch(Exception ex) {}
//
//        if(!gps_enabled ) {
//            // notify user
//            new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.DialogTheme))
//                    .setMessage(R.string.gps_network_not_enabled)
//                    .setPositiveButton(R.string.open_location_settings, (paramDialogInterface, paramInt) -> startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
//                    .setNegativeButton(R.string.cancel,null)
//                    .show();
//        }
        bottomBar=findViewById(R.id.bottombar);
        bottomBar.getMenu().getItem(2).setChecked(true);
        bottomBar.setOnNavigationItemSelectedListener(item -> {
            if(item.getItemId()==R.id.my_reports){
                startActivity(new Intent(getApplicationContext(), MyReportsActivity.class));
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
            return item.getItemId() == R.id.new_report;
        });

        coord = findViewById(R.id.locationvalue);

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
            myloc = true;
            Log.i("location","clicked");
            StringBuilder gps_address = new StringBuilder();
            try {
                Geocoder geocoder = new Geocoder(this, Locale.forLanguageTag("EL"));
                if (current_loc != null){
                    List<Address> addresses = geocoder.getFromLocation(current_loc.latitude, current_loc.longitude, 1);
                    if (addresses.size() > 0) {
                        Address address = addresses.get(0);
                        gps_address.append(address.getAddressLine(0));
                        coord.setText(gps_address.toString());
                    }
                }else{

                    Toast.makeText(getApplicationContext(), R.string.loc_not_ready, Toast.LENGTH_LONG).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        fab.setOnClickListener(view -> {
            Log.i("report","click");
            try {
                report();
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

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void report() throws IOException {
        Report report = new Report(String.valueOf(UUID.randomUUID()));
        report.setTimestamp(datetimeView.getText().toString());
        report.setDescription(description.getText().toString());
        report.setLocation(coord.getText().toString());
        if (category!=null) report.setType(category);
        else {
            Toast toast = Toast.makeText(context, R.string.select_category, Toast.LENGTH_LONG);
            View v = toast.getView();
            v.setBackgroundResource(R.drawable.error_toast);
            TextView t = (TextView) toast.getView().findViewById(android.R.id.message);
            t.setTextColor(Color.RED);
            toast.show();
        }
        if (current_lon!= -99 && current_lat!= -99){
            report.setLatitude(current_lat);
            report.setLongitude(current_lon);
        }
        if(!myloc && !coord.getText().toString().isEmpty()){
            List<Address> addresses = new Geocoder(this, Locale.forLanguageTag("EL")).getFromLocationName(coord.getText().toString(),1);
            if (addresses.size()>0){
                Address address = addresses.get(0);
                user_input_loc = new LatLng(address.getLatitude(),address.getLongitude());
                Log.i("report", String.valueOf(user_input_loc));
            }
        }
        Log.i("report", report.toString());

        // adding listeners on upload
        // or failure of image
        // Progress Listener for loading
        // percentage on the dialog box
        //TODO check if everything is filled
        //TODO fix upload from camera
        ref.child(currentUser.getUid()).push().setValue(report).addOnCompleteListener(task -> {
                                    if(task.isSuccessful()){
                                        Log.i("click","ref");
                                        if (filePath!=null){
                                            Log.i("report",filePath.toString());
                                            storageReference.child(currentUser.getUid()).child(report.getId()).putFile(filePath)
                                                    .addOnSuccessListener(taskSnapshot ->{
                                                            Log.i("report", "success");
                                                            Toast.makeText(NewReportActivity.this,R.string.success, Toast.LENGTH_SHORT).show();
                                                            startActivity(new Intent(getApplicationContext(),MyReportsActivity.class));
                                                            })
                                                    .addOnFailureListener(e ->
                                                            Toast.makeText(NewReportActivity.this,"Failed " + e.getMessage(), Toast.LENGTH_SHORT).show())
                                                    .addOnProgressListener(
                                                            taskSnapshot -> {
                                                                double progress= (100.0* taskSnapshot.getBytesTransferred()/ taskSnapshot.getTotalByteCount());
                                                                Log.i("reportprogress", String.valueOf(progress));
                                                                if (progress<100) {
                                                                    progressBar.setVisibility(View.VISIBLE);
                                                                    ConstraintLayout layout = findViewById(R.id.mainLayout);
                                                                    layout.setAlpha((float) 0.5);
                                                                    progressBar.setProgress((int) progress);

                                                                } else {
                                                                    ConstraintLayout layout = findViewById(R.id.mainLayout);
                                                                    layout.setAlpha(1);
                                                                    progressBar.setVisibility(View.INVISIBLE);
                                                                }
                                                            });
                                        }
                                    }
                                    else  Log.i("click",task.getException().getMessage());

                                }).addOnFailureListener(e -> Log.i("click",e.getMessage()));

    }
    @Override
    public void onLocationChanged(@NonNull Location location) {
        if (location!=null){
            current_loc = new LatLng(location.getLatitude(),location.getLongitude());
            current_lat = location.getLatitude();
            current_lon = location.getLongitude();
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

