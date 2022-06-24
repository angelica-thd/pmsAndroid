package com.example.myapplication;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Report {
    private String timestamp;
    private String location;
    private double latitude;
    private double longitude;
    private String description;
    private String id;
    private String type;
    private List<String> types;


    public Report(String id) {
        this.id = id;
        String[] commons = {"Ελλειπής Συντήρηση Δρόμων", "Ελλειπής Συντήρηση Παλαιών Κτιρίων", "Έλλειψη Μέσων Μαζικής Μεταφοράς στην Περιοχή", "Βλάβες/Καταστροφές", "Έλλειψη Θέσεις Πάρκινγκ", "Ρύπανση", "Ελλειπής Απομάκρυνση Απορριμάτων", "Εγκληματικότητα", "Βανδαλισμοί", "Άλλο"};
        types.addAll(Arrays.asList(commons));
    }
    public Report() {
    }
    public List<Report> getReports(DatabaseReference ref, String userID){
        List<Report> reports = new ArrayList<>();
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot myreport : snapshot.child(userID).getChildren()) {
                    String id = String.valueOf(myreport.child("id").getValue());
                    String location = String.valueOf(myreport.child("location").getValue());
                    String lat = String.valueOf(myreport.child("latitude").getValue());
                    String lon = String.valueOf(myreport.child("longitude").getValue());
                    String description = String.valueOf(myreport.child("description").getValue());
                    String timestamp = String.valueOf(myreport.child("timestamp").getValue());
                    //LatLng loc =new LatLng(Double.parseDouble(lat),Double.parseDouble(lon));

                    Report report = new Report(id);
                    report.setTimestamp(timestamp);
                    report.setDescription(description);
                    report.setLocation(location);
                    report.setLatitude(Double.parseDouble(lat));
                    report.setLongitude(Double.parseDouble(lon));

                    reports.add(report);

                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i("fire_error",error.getMessage());
            }
        });
        Log.i("reports", String.valueOf(reports));
        return reports;
    }

    public String getType() {
        return type;
    }

    public boolean setType(String type) {
        if (types.contains(type)){
            this.type = type;
            return true;
        }
        else return false;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    public String getId() {
        return id;
    }
    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
