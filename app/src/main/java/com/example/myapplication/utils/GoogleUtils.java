package com.example.myapplication.utils;

import com.example.myapplication.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;

import java.util.List;
import java.util.Objects;

public class GoogleUtils {

    public void addMarkerFromReport(GoogleMap mMap, List<Report> reports, DataSnapshot user, FirebaseUser currentUser){
        mMap.clear();
        for (Report report : reports){
            LatLng loc = new LatLng(report.getLatitude(),report.getLongitude());
            MarkerOptions my_options = new MarkerOptions().position(loc);
            if (Objects.equals(user.getKey(), currentUser.getUid())) my_options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).title(String.valueOf(R.string.myreport));
            else my_options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE));
            mMap.addMarker(my_options);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 15));
        }

    }
}
