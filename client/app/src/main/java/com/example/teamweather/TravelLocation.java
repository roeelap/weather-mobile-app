package com.example.teamweather;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class TravelLocation implements Parcelable {

    private static final String TAG = "TravelLocation_Class";

    private String name;
    private final LatLng latLng;

    public TravelLocation(String name, LatLng latLng) {
        this.name = name;
        this.latLng = latLng;
    }

    protected TravelLocation(Parcel in) {
        name = in.readString();
        latLng = in.readParcelable(LatLng.class.getClassLoader());
    }

    public static final Creator<TravelLocation> CREATOR = new Creator<TravelLocation>() {
        @Override
        public TravelLocation createFromParcel(Parcel in) {
            return new TravelLocation(in);
        }

        @Override
        public TravelLocation[] newArray(int size) {
            return new TravelLocation[size];
        }
    };

    public String getName() {
        return name;
    }
    public LatLng getLatLng() {
        return latLng;
    }

    public void setName(String newName) { this.name = newName; }

    public static ArrayList<TravelLocation> parseTravelLocations(JSONArray userLocations) {
        ArrayList<TravelLocation> markers = new ArrayList<>();
        for (int i = 0; i < userLocations.length(); i++) {
            try {
                String locationName = userLocations.getJSONObject(i).getString("name");
                double lat = userLocations.getJSONObject(i).getDouble("lat");
                double lng = userLocations.getJSONObject(i).getDouble("lng");
                markers.add(new TravelLocation(locationName, new LatLng(lat, lng)));
            } catch (JSONException e) {
                Log.e(TAG, "Error while parsing travel location: " + e.getMessage());
            }
        }

        return markers;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeParcelable(latLng, flags);
    }
}
