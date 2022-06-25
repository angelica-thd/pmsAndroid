package com.example.myapplication.utils;

import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

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



    public Report(String id) {
        this.id = id;
    }

    @NonNull
    @Override
    public String toString() {
        return this.id + ", "+ this.location + ", "+ this.timestamp +", "+this.type;
    }

    public String getType() {
        return type;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public boolean setType(String type) {
        String[] types = new String[]{"Ελλειπής Συντήρηση Δρόμων", "Ελλειπής Συντήρηση Παλαιών Κτιρίων", "Έλλειψη Μέσων Μαζικής Μεταφοράς στην Περιοχή", "Βλάβες/Καταστροφές", "Έλλειψη Θέσεις Πάρκινγκ", "Ρύπανση", "Ελλειπής Απομάκρυνση Απορριμάτων", "Εγκληματικότητα", "Βανδαλισμοί", "Άλλο"};
        if (Arrays.asList(types).contains(type)){
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
