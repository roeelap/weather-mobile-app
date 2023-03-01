package com.example.teamweather;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;

public class WeatherFetcher {

    private static final String TAG = "WeatherFetcher";
    private final RequestQueue _queue;

    // The IP (needs to be changed to run on different devices)
    private final static String IP = "10.0.2.2";
    private final static String REQUEST_URL = "http://" + IP + ":8080/weather";

    public static class WeatherResponse {
        public boolean isError;
        public JSONArray weather;

        public WeatherResponse(boolean isError, JSONArray weather) {
            this.isError = isError;
            this.weather = weather;
        }
    }

    public interface WeatherResponseListener {
        void onResponse(WeatherResponse response);
    }

    public WeatherFetcher(Context context) {
        _queue = Volley.newRequestQueue(context);
    }

    private WeatherResponse createErrorResponse() { return new WeatherResponse(true, null); }

    /**
     * This method is called when a user is trying fetch the weather for a location at a specific date.
     * Contact the server to get the weather for the location.
     * @param lat the latitude of the location
     * @param lon the longitude of the location
     * @param date the date of the weather
     * @param listener the listener to be called when the response is received
     */
    public void dispatchRequest(String lat, String lon, String date, final WeatherResponseListener listener) {
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, REQUEST_URL + "?lat=" + lat + "&lon=" + lon + "&date=" + date , null,
                response -> {
                    Log.d(TAG, "Got response: " + response.toString());
                    try {
                        WeatherResponse res = new WeatherResponse(false, response.getJSONArray("data"));
                        listener.onResponse(res);
                    }
                    catch (JSONException e) {
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
