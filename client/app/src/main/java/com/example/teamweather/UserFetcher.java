package com.example.teamweather;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;

public class UserFetcher {

    private static final String TAG = "UserFetcher";
    private final RequestQueue _queue;
    private final static String REQUEST_URL = "http://10.0.0.2:8080/users";

    public static class UserResponse {
        public boolean isError;
        public boolean isSuccessful;

        public UserResponse(boolean isError, boolean isSuccessful) {
            this.isError = isError;
            this.isSuccessful = isSuccessful;
        }
    }

    public interface UserResponseListener {
        void onResponse(UserResponse response);
    }

    public UserFetcher(Context context) {
        _queue = Volley.newRequestQueue(context);
    }

    private UserResponse createErrorResponse() {
        return new UserResponse(true, false);
    }

    public void validateUser(final String email, final String password, final UserResponseListener listener) {
        String url = REQUEST_URL + "?email=" + email + "&password=" + password;

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    Log.d(TAG, "Got response: " + response.toString());
                    try {
                        boolean isSuccessful = response.getBoolean("isUserExists");
                        UserResponse res = new UserResponse(false, isSuccessful);
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