package com.example.teamweather;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WeatherActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private TextView mDisplayDate;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    // TODO: fix
    private ImageButton backButton;
    private String dateFormat;
    // values of the date chosen
    int year, month, day;
    boolean firstTime = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        // set up the location title (get the location of the marker that was chosen in the map)
        TextView locationTextView = findViewById(R.id.textViewLocation);
        String location = "Weather for " + getIntent().getStringExtra("location");
        locationTextView.setText(location);

        mDisplayDate = findViewById(R.id.textView_selectDate);

        mDisplayDate.setOnClickListener(view -> {
            if (firstTime) {
                Calendar cal = Calendar.getInstance();
                year = cal.get(Calendar.YEAR);
                month = cal.get(Calendar.MONTH);
                day = cal.get(Calendar.DAY_OF_MONTH);
                firstTime = false;
            }
            DatePickerDialog dialog = new DatePickerDialog(
                    WeatherActivity.this,
                    android.R.style.Theme_Holo_Dialog_MinWidth,
                    mDateSetListener,
                    year, month, day);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();
        });

        mDateSetListener = (datePicker, year, month, day) -> {
            this.year = year;
            this.month = month;
            this.day = day;
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

        // connecting to the buttons
        Button fetchWeather = findViewById(R.id.search);
        fetchWeather.setOnClickListener(this::fetchWeather);
        backButton = findViewById(R.id.back);
        backButton.setOnClickListener(this::setBackButton);
    }

    public void fetchWeather(final View view) {
        final GetWeather fetcher = new GetWeather(view.getContext());
        // getting the lat and lng
        double lat = getIntent().getDoubleExtra("lat", 0);
        double lng = getIntent().getDoubleExtra("lat", 0);

        final ProgressDialog progressDialog = new ProgressDialog(this);
        final int weatherTableLen = 8;
        progressDialog.setMessage("Fetching weather...");
        progressDialog.show();

        fetcher.dispatchRequest(String.valueOf(lat), String.valueOf(lng), dateFormat, response -> {
            progressDialog.hide();

            if (response.isError) {
                // TODO: implement <<<<<<<<<<<<<<<<<<<<<<<<<<<<
//                ((TextView)MainActivity.this.findViewById(R.id.current_stock_price)).setText("");
//                Toast.makeText(view.getContext(), response.price, Toast.LENGTH_LONG).show();
                return;
            }
            // Parse the JSON response
            try {
                String icon = null; // for the weather icon
                boolean noWeather = true; // to know if the icon was set already
                JSONArray jsonArray = response.weather; // getting the json

                // counter for the json objects
                int j = 0;
                // goes over the data and inserts it to the table
                for (int i = 0; i < weatherTableLen; i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(j);
                    String time = jsonObject.getString("time");
                    String weather = jsonObject.getString("weather");
                    double temp = Math.round(jsonObject.getDouble("temp"));
                    double wind = jsonObject.getDouble("wind");

                    int textViewId_time = getResources().getIdentifier("time_" + (i + 1), "id", getPackageName());
                    int textViewId_weather = getResources().getIdentifier("weather_" + (i + 1), "id", getPackageName());
                    int textViewId_temp = getResources().getIdentifier("temp_" + (i + 1), "id", getPackageName());
                    int textViewId_wind = getResources().getIdentifier("wind_" + (i + 1), "id", getPackageName());

                    String checkTime = String.valueOf(((TextView) WeatherActivity.this.findViewById(textViewId_time)).getText());

                    // if the current day is picked, makes sure that the passed hours are set to "-"
                    while (!checkTime.equals(time)) {
                        ((TextView) WeatherActivity.this.findViewById(textViewId_weather)).setText("-");
                        ((TextView) WeatherActivity.this.findViewById(textViewId_temp)).setText("-");
                        ((TextView) WeatherActivity.this.findViewById(textViewId_wind)).setText("-");
                        i++;
                        textViewId_time = getResources().getIdentifier("time_" + (i + 1), "id", getPackageName());
                        textViewId_weather = getResources().getIdentifier("weather_" + (i + 1), "id", getPackageName());
                        textViewId_temp = getResources().getIdentifier("temp_" + (i + 1), "id", getPackageName());
                        textViewId_wind = getResources().getIdentifier("wind_" + (i + 1), "id", getPackageName());
                        checkTime = String.valueOf(((TextView) WeatherActivity.this.findViewById(textViewId_time)).getText());
                    }

                    ((TextView) WeatherActivity.this.findViewById(textViewId_weather)).setText(weather);
                    ((TextView) WeatherActivity.this.findViewById(textViewId_temp)).setText(temp + "°C");
                    ((TextView) WeatherActivity.this.findViewById(textViewId_wind)).setText(wind + " Knots");

                    // Set the icon image to the weather at noon or later if it's later in the day
                    if ((time.equals("12:00") || time.equals("18:00") || time.equals("21:00")) && noWeather) {
                        icon = jsonObject.getString("icon");
                        noWeather = false;
                    }

                    j++;
                }
                // inserting the icon
                String iconUrl = "http://openweathermap.org/img/wn/" + icon + "@2x.png";
                ImageView iconView = findViewById(R.id.weather_icon);
                Picasso.get().load(iconUrl).into(iconView);

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void setBackButton(final View view) {
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MapsActivity.class);
            startActivity(intent);
            finish();
        });
    }
}