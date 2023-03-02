package com.example.teamweather;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class WeatherActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private TextView mDisplayDate;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private String dateFormat;
    // values of the date chosen
    int year, month, day;
    boolean firstTime = true;
    // field ID's for the weather table
    int textViewId_time, textViewId_weather, textViewId_temp, textViewId_wind;

    private final int weatherTableLen = 8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        // set up the location title (get the location of the marker that was chosen in the map)
        TextView locationTextView = findViewById(R.id.textViewLocation);
        String location = "Weather for " + getIntent().getStringExtra("location");
        locationTextView.setText(location);

        // the interface to choose the date to get the weather for
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
            // making the format of the date united for all numbers
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
        Button fetchWeather = findViewById(R.id.search);
        fetchWeather.setOnClickListener(this::fetchWeather);
    }

    /**
     * This method is called when the search button is clicked.
     * It makes sure a date was picked and if so, goes over the the json received from the server and puts the data into the weather table.
     * It also writes a relevant message for the user if the date pick is outside the date range.
     */
    private void fetchWeather(final View view) {
        if (firstTime) {
            ((TextView) findViewById(R.id.error_text)).setText(R.string.date_request);
            return;
        }
        
        ((TextView) findViewById(R.id.error_text)).setText("");
        final WeatherFetcher fetcher = new WeatherFetcher(view.getContext());
        // getting the lat and lng
        double lat = getIntent().getDoubleExtra("lat", 0);
        double lng = getIntent().getDoubleExtra("lat", 0);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        
        progressDialog.setMessage("Fetching weather...");
        progressDialog.show();

        fetcher.dispatchRequest(String.valueOf(lat), String.valueOf(lng), dateFormat, response -> {
            progressDialog.hide();
            
            // checks if an Exception was received
            if (response.isError) {
                resetTableLines(weatherTableLen);
                Toast.makeText(view.getContext(), "Error while fetching the weather", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Parse the JSON response
            try {
                JSONArray jsonArray = response.weather; // getting the json                
                // empty json = no weather for this day
                if (jsonArray.length() == 0) {
                    updateFieldsID(0);
                    resetTableLines(weatherTableLen);
                    ((TextView) findViewById(R.id.error_text)).setText(R.string.date_out_of_bound);
                    drawIcon(null);
                } else {
                    String icon = insertWeather(jsonArray); // goes over the data and inserts it to the table
                    drawIcon(icon);
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * inserts the weather received to the weather table.
     * @param jsonArray The array of the jsonObject received from the server.
     * @return The string of the relevant weather icon to insert.
     */
    private String insertWeather (JSONArray jsonArray) throws JSONException {
        String icon = null; // for the weather icon
        boolean noWeather = true; // to know if the icon was set already
        // counter for the hours before the current time (relevant only if the day requested is today)
        int j = 0;
        for (int i = 0; i < jsonArray.length(); i++) {
            ((TextView) findViewById(R.id.error_text)).setText("");
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String time = jsonObject.getString("time");
            String weather = jsonObject.getString("weather");
            int temp = (int) (jsonObject.getDouble("temp"));
            double wind = jsonObject.getDouble("wind");

            // gets the ID's of the next row in the weather table
            updateFieldsID(j);

            // the first time in the json of the date chosen
            String checkTime = String.valueOf(((TextView) findViewById(textViewId_time)).getText());
            
            // if the current day is picked, makes sure that the passed hours are set to "-"
            if (!checkTime.equals(time)) {
                int times = weatherTableLen - jsonArray.length();
                resetTableLines(times);
                j = times;
            }
            // update data in the weather map
            ((TextView) findViewById(textViewId_weather)).setText(weather);
            String tempText = getResources().getString(R.string.temperature_text, temp);
            ((TextView) findViewById(textViewId_temp)).setText(tempText);
            String windText = String.format(Locale.getDefault(), "%.1f Knots", wind);
            ((TextView) findViewById(textViewId_wind)).setText(windText);

            // Set the icon image to the weather at noon or later if it's later in the day
            if ((time.equals("12:00") || time.equals("15:00") || time.equals("18:00") || time.equals("21:00")) && noWeather) {
                icon = jsonObject.getString("icon");
                noWeather = false;
            }
            j++;
        }
        return icon;
    }

    /**
     * inserts the weather icon to it's place
     * @param icon The icon info received from the server.
     */
    private void drawIcon(String icon) {
        String iconUrl = "http://openweathermap.org/img/wn/" + icon + "@2x.png";
        ImageView iconView = findViewById(R.id.weather_icon);
        Picasso.get().load(iconUrl).into(iconView);
    }
    
    /**
     * Updates the ID's to the ones parallel in the weather table
     * @param j The index number that is being read in the function.
     */
    @SuppressLint("DiscouragedApi")
    private void updateFieldsID (int j) {
        textViewId_time = getResources().getIdentifier("time_" + (j + 1), "id", getPackageName());
        textViewId_weather = getResources().getIdentifier("weather_" + (j + 1), "id", getPackageName());
        textViewId_temp = getResources().getIdentifier("temp_" + (j + 1), "id", getPackageName());
        textViewId_wind = getResources().getIdentifier("wind_" + (j + 1), "id", getPackageName());
    }

    /**
     * Resets the rows to a "-"
     * @param times The number of empty rows to reset in the table.
     */
    private void resetTableLines (int times) {
        for (int i = 0; i < times; i++) {
            ((TextView) WeatherActivity.this.findViewById(textViewId_weather)).setText("-");
            ((TextView) WeatherActivity.this.findViewById(textViewId_temp)).setText("-");
            ((TextView) WeatherActivity.this.findViewById(textViewId_wind)).setText("-");

            updateFieldsID(i+1);
        }
    }
}