package com.example.teamweather;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class UserFetcher {

    private static final String TAG = "UserFetcher";
    private final RequestQueue _queue;
    private final static String IP = "10.0.2.2";
    private final static String VALIDATE_USER_REQUEST_URL = "http://" + IP + ":8080/validate-user";
    private final static String CREATE_USER_REQUEST_URL = "http://" + IP + ":8080/create-user";
    private final static String UPDATE_USER_MARKERS_REQUEST_URL = "http://" + IP + ":8080/update-user-markers";

    public static class UserResponse {
        public boolean isError;
        public boolean isSuccessful;
        public ArrayList<TravelLocation> userLocations;

        public UserResponse(boolean isError, boolean isSuccessful, ArrayList<TravelLocation> userLocations) {
            this.isError = isError;
            this.isSuccessful = isSuccessful;
            this.userLocations = userLocations;
        }
    }

    public interface UserResponseListener {
        void onResponse(UserResponse response);
    }

    public UserFetcher(Context context) {
        _queue = Volley.newRequestQueue(context);
    }

    private UserResponse createErrorResponse() {
        return new UserResponse(true, false, null);
    }

    /**
     * This method is called when a user is trying to login or create a new account.
     * If the desired action is to login, it validates the user and if the user exists, it will start the MapsActivity.
     * If the desired action is to create a new account, it will create a new user in the database.
     * @param isCreateNewUser true if the user is trying to create a new account, false if the user is trying to login
     * @param userName the user's unique username
     * @param password the user's password
     * @param listener the listener to be called when the response is received
     */
    public void fetchUser(final boolean isCreateNewUser, final String userName, final String password, final UserResponseListener listener) {
        String url;
        if (isCreateNewUser) {
            url = CREATE_USER_REQUEST_URL + "?userName=" + userName + "&password=" + password;
        } else {
            url = VALIDATE_USER_REQUEST_URL + "?userName=" + userName + "&password=" + password;
        }

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    Log.d(TAG, "Got response: " + response.toString());
                    try {
                        boolean isSuccessful = response.getBoolean("result");
                        JSONArray userLocations = response.getJSONArray("markers");
                        ArrayList<TravelLocation> locations = TravelLocation.parseTravelLocations(userLocations);
                        UserResponse res = new UserResponse(false, isSuccessful, locations);
                        listener.onResponse(res);
                    } catch (JSONException e) {
                        Log.e(TAG, "Error while parsing response: " + e.getMessage());
                        listener.onResponse(createErrorResponse());
                    }
                }, error -> {
                    Log.e(TAG, "Error while parsing response: " + error.getMessage());
                    listener.onResponse(createErrorResponse());
                });

        _queue.add(req);
    }

    /**
     * This method is called when the user clicks on the save button.
     * It sends the user's locations to the server to be saved in the database.
     * @param userName the user's unique username
     * @param locations the user's locations
     * @param listener the listener to be called when the response is received
     */
    public void updateUserLocations(final String userName, final ArrayList<TravelLocation> locations, final UserResponseListener listener) {
        JSONObject postBody = new JSONObject();
        try {
            postBody.put("userName", userName);
            JSONArray markersJson = new JSONArray();
            for (TravelLocation location : locations) {
                JSONObject markerJson = new JSONObject();
                markerJson.put("name", location.getName());
                markerJson.put("lat", location.getLatLng().latitude);
                markerJson.put("lng", location.getLatLng().longitude);
                markersJson.put(markerJson);
            }
            postBody.put("markers", markersJson);
        } catch (JSONException e) {
            Log.e(TAG, "Error while creating post body: " + e.getMessage());
            listener.onResponse(createErrorResponse());
            return;
        }
        Log.e(TAG, "Post body: " + postBody);
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, UPDATE_USER_MARKERS_REQUEST_URL, postBody,
            response -> {
                Log.d(TAG, "Got response: " + response.toString());
                try {
                    boolean isSuccessful = response.getBoolean("result");
                    UserResponse res = new UserResponse(false, isSuccessful, null);
                    listener.onResponse(res);
                } catch (JSONException e) {
                    Log.e(TAG, "Error while parsing response: " + e.getMessage());
                    listener.onResponse(createErrorResponse());
                }
            }, error -> {
                Log.e(TAG, "Error while parsing response: " + error.getMessage());
                listener.onResponse(createErrorResponse());
        });

        _queue.add(req);
    }
}