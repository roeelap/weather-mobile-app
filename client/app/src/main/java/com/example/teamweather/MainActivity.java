package com.example.teamweather;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private TextView locationTextView;
    private TextView mDisplayDate;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private Button fetchButton;
    private String dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // set up the location title (get the location of the marker that was chosen in the map)
        locationTextView = findViewById(R.id.textViewLocation);
        String location = getIntent().getStringExtra("location");
        locationTextView.setText(location);

        mDisplayDate = findViewById(R.id.textView_selectDate);

        mDisplayDate.setOnClickListener(view -> {
            Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int day = cal.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dialog = new DatePickerDialog(
                    MainActivity.this,
                    android.R.style.Theme_Holo_Dialog_MinWidth,
                    mDateSetListener,
                    year, month, day);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();
        });

        mDateSetListener = (datePicker, year, month, day) -> {
            month = month + 1;
            Log.d(TAG, "onDateSet: date: dd/mm/yyyy " + day + "/" + month + "/" + year);

            String dayString, monthString;
            if (day < 10)
                dayString = "0" + day;
            else
                dayString = String.valueOf(day);
            if (month < 10)
                monthString = "0" + month;
            else
                monthString = String.valueOf(month);
            String date = dayString + "/" + monthString + "/" + year;
            dateFormat = year + "-" + monthString + "-" + dayString;
            mDisplayDate.setText(date);
        };

        // connecting to the button
        fetchButton = findViewById(R.id.search);
        fetchButton.setOnClickListener(this::fetchWeather);
    }

    public void fetchWeather(final View view) {
        final GetWeather fetcher = new GetWeather(view.getContext());
        // getting the lat and lng
        // TODO: fix default values <<<<<<<<<<<<<<<<<<<
        double lat = getIntent().getDoubleExtra("lat", 32.085300);
        double lng = getIntent().getDoubleExtra("lat", 34.781769);

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Fetching weather...");
        progressDialog.show();

        fetcher.dispatchRequest(String.valueOf(lat), String.valueOf(lng), dateFormat, response -> {
            progressDialog.hide();

            if (response.isError) {
                // TODO: implement <<<<<<<<<<<<<<<<<<<<<<<<<<<<
                return;
            }
            // Parse the JSON response
            try {
                JSONArray jsonArray = response.weather;
                TableLayout tableLayout = findViewById(R.id.weather_table);

                // counter to match the times on the day of the search
                int j = 0;

                // goes over the data and inserts it to the table
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String time = jsonObject.getString("time");
                    String weather = jsonObject.getString("weather");
                    double temp = jsonObject.getDouble("temp");
                    double wind = jsonObject.getDouble("wind");

                    int textViewId_time = getResources().getIdentifier("time_" + (j+1), "id", getPackageName());
                    int textViewId_weather = getResources().getIdentifier("weather_" + (j+1), "id", getPackageName());
                    int textViewId_temp = getResources().getIdentifier("temp_" + (j+1), "id", getPackageName());
                    int textViewId_wind = getResources().getIdentifier("wind_" + (j+1), "id", getPackageName());

                    String checkTime = String.valueOf(((TextView) MainActivity.this.findViewById(textViewId_time)).getText());

                    while (!time.equals(checkTime)) {
                        ((TextView) MainActivity.this.findViewById(textViewId_weather)).setText("-");
                        ((TextView) MainActivity.this.findViewById(textViewId_temp)).setText("-");
                        ((TextView) MainActivity.this.findViewById(textViewId_wind)).setText("-");
                        j++;
                        textViewId_time = getResources().getIdentifier("time_" + (j+1), "id", getPackageName());
                        textViewId_weather = getResources().getIdentifier("weather_" + (j+1), "id", getPackageName());
                        textViewId_temp = getResources().getIdentifier("temp_" + (j+1), "id", getPackageName());
                        textViewId_wind = getResources().getIdentifier("wind_" + (j+1), "id", getPackageName());
                        checkTime = String.valueOf(((TextView) MainActivity.this.findViewById(textViewId_time)).getText());
                    }

                    ((TextView) MainActivity.this.findViewById(textViewId_weather)).setText(weather);
                    ((TextView) MainActivity.this.findViewById(textViewId_temp)).setText(temp + "°C");
                    ((TextView) MainActivity.this.findViewById(textViewId_wind)).setText(wind + " Knots");

                    j++;
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });
    }
}