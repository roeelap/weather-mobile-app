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
    private final static String VALIDATE_USER_REQUEST_URL = "http://10.0.0.16:8080/validate-user";
    private final static String CREATE_USER_REQUEST_URL = "http://10.0.0.16:8080/create-user";

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

    public void dispatchRequest(final boolean isCreateNewUser, final String userName, final String password, final UserResponseListener listener) {
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