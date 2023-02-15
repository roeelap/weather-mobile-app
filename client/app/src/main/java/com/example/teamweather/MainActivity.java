package com.example.teamweather;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private TextView locationTextView;
    private TextView mDisplayDateFrom;
    private DatePickerDialog.OnDateSetListener mDateFromSetListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // set up the location title (get the location of the marker that was chosen in the map)
        locationTextView = findViewById(R.id.textViewLocation);
        String location = getIntent().getStringExtra("location");
        locationTextView.setText(location);

        mDisplayDateFrom = findViewById(R.id.textView_selectDateFrom);

        mDisplayDateFrom.setOnClickListener(view -> {
            Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int day = cal.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dialog = new DatePickerDialog(
                    MainActivity.this,
                    android.R.style.Theme_Holo_Dialog_MinWidth,
                    mDateFromSetListener,
                    year, month, day);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();
        });

        mDateFromSetListener = (datePicker, year, month, day) -> {
            month = month + 1;
            Log.d(TAG, "onDateSet: date: dd/mm/yyyy " + day + "/" + month + "/" + year);

            String dateFrom = day + "/" + month + "/" + year;
            mDisplayDateFrom.setText(dateFrom);
        };
    }
}