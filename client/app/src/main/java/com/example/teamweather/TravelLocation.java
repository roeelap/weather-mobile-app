package com.example.teamweather;

import com.google.android.gms.maps.model.LatLng;

public class TravelLocation {

    private final String name;
    private final LatLng latLng;

    public TravelLocation(String name, LatLng latLng) {
        this.name = name;
        this.latLng = latLng;
    }

    public String getName() {
        return name;
    }

    public LatLng getLatLng() {
        return latLng;
    }

}
