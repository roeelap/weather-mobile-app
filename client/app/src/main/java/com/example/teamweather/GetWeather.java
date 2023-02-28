package com.example.teamweather;

import android.content.Context;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;

public class GetWeather {

    private RequestQueue _queue;
    // The IP
    private final static String IP = "10.0.2.2";
    // The URL to use the server
    private final static String REQUEST_URL = "http://" + IP + ":8080/weather";

    public class WeatherResponse {
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

    public GetWeather(Context context) {
        _queue = Volley.newRequestQueue(context);
    }

    private WeatherResponse createErrorResponse(String err) {
        return new WeatherResponse(true, null);
    }

    // The actual request send to the server
    public void dispatchRequest(String lat, String lon, String date, final WeatherResponseListener listener) {
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, REQUEST_URL + "?lat=" + lat + "&lon=" + lon + "&date=" + date , null,
                response -> {
                    // deals with the response
                    try {
                        WeatherResponse res = new WeatherResponse(false,
                                response.getJSONArray("weather"));
                        listener.onResponse(res);
                    }
                    // deals with errors
                    catch (JSONException e) {
                        listener.onResponse(createErrorResponse(e.toString()));
                    }
                }, error -> listener.onResponse(createErrorResponse(error.toString())));
        _queue.add(req);
    }
}
