
package com.example.travelitineraryplanner;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends Activity {

    private EditText etDestination, etLocation, etActivities;
    private Button btnStartDate, btnEndDate, btnSave, btnView;
    private TextView tvStartDate, tvEndDate;
    private ListView listItineraries;

    private DBHelper dbHelper;

    private String startDate = "";
    private String endDate = "";
    private int selectedId = -1;

    private final ArrayList<Integer> itineraryIds = new ArrayList<>();
    private final ArrayList<String> itineraryList = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DBHelper(this);

        etDestination = findViewById(R.id.etDestination);
        etLocation = findViewById(R.id.etLocation);
        etActivities = findViewById(R.id.etActivities);

        btnStartDate = findViewById(R.id.btnStartDate);
        btnEndDate = findViewById(R.id.btnEndDate);
        btnSave = findViewById(R.id.btnSave);
        btnView = findViewById(R.id.btnView);

        tvStartDate = findViewById(R.id.tvStartDate);
        tvEndDate = findViewById(R.id.tvEndDate);
        listItineraries = findViewById(R.id.listItineraries);

        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                itineraryList
        );

        listItineraries.setAdapter(adapter);

        btnStartDate.setOnClickListener(v -> selectDate(true));
        btnEndDate.setOnClickListener(v -> selectDate(false));
        btnSave.setOnClickListener(v -> saveOrUpdateItinerary());
        btnView.setOnClickListener(v -> loadItineraries());

        // Tap a saved plan to edit it
        listItineraries.setOnItemClickListener(
                (parent, view, position, id) -> {
                    int itineraryId = itineraryIds.get(position);
                    loadItineraryForEditing(itineraryId);
                }
        );

        // Long-press a saved plan to delete it
        listItineraries.setOnItemLongClickListener(
                (parent, view, position, id) -> {
                    confirmDelete(position);
                    return true;
                }
        );

        loadItineraries();
    }

    private void selectDate(boolean isStartDate) {

        Calendar calendar = Calendar.getInstance();

        String currentDate = isStartDate ? startDate : endDate;

        if (!currentDate.isEmpty()) {
            String[] parts = currentDate.split("-");
            calendar.set(
                    Integer.parseInt(parts[0]),
                    Integer.parseInt(parts[1]) - 1,
                    Integer.parseInt(parts[2])
            );
        }

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (datePicker, year, month, day) -> {

                    String date = String.format(
                            Locale.US,
                            "%04d-%02d-%02d",
                            year, month + 1, day
                    );

                    if (isStartDate) {
                        startDate = date;
                        tvStartDate.setText("Start date: " + date);
                    } else {
                        endDate = date;
                        tvEndDate.setText("End date: " + date);
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        dialog.show();
    }

    private void saveOrUpdateItinerary() {

        String destination =
                etDestination.getText().toString().trim();

        String location =
                etLocation.getText().toString().trim();

        String activities =
                etActivities.getText().toString().trim();

        if (destination.isEmpty()) {
            etDestination.setError("Enter destination");
            etDestination.requestFocus();
            return;
        }

        if (startDate.isEmpty() || endDate.isEmpty()) {
            Toast.makeText(this,
                    "Select both travel dates",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (endDate.compareTo(startDate) < 0) {
            Toast.makeText(this,
                    "End date cannot be before start date",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            if (selectedId == -1) {

                // Save a new itinerary
                dbHelper.insertData(
                        destination, location,
                        startDate, endDate, activities
                );

                Toast.makeText(this,
                        "Itinerary saved successfully",
                        Toast.LENGTH_SHORT).show();

            } else {

                // Update the selected itinerary
                dbHelper.updateData(
                        selectedId, destination, location,
                        startDate, endDate, activities
                );

                Toast.makeText(this,
                        "Itinerary updated successfully",
                        Toast.LENGTH_SHORT).show();
            }

            clearFields();
            loadItineraries();

        } catch (Exception e) {
            Toast.makeText(this,
                    "Database error: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void loadItineraries() {

        itineraryList.clear();
        itineraryIds.clear();

        Cursor cursor = dbHelper.getData();

        try {
            while (cursor.moveToNext()) {

                int id = cursor.getInt(
                        cursor.getColumnIndexOrThrow("id"));

                String destination = cursor.getString(
                        cursor.getColumnIndexOrThrow("destination"));

                String location = cursor.getString(
                        cursor.getColumnIndexOrThrow("location"));

                String start = cursor.getString(
                        cursor.getColumnIndexOrThrow("start_date"));

                String end = cursor.getString(
                        cursor.getColumnIndexOrThrow("end_date"));

                String activities = cursor.getString(
                        cursor.getColumnIndexOrThrow("activities"));

                itineraryIds.add(id);

                String item =
                        "Destination: " + destination +
                                "\nLocation: " + location +
                                "\nStart date: " + start +
                                "\nEnd date: " + end +
                                "\nActivities: " +
                                (activities == null || activities.isEmpty()
                                        ? "Not specified" : activities);

                itineraryList.add(item);
            }
        } finally {
            cursor.close();
        }

        adapter.notifyDataSetChanged();
    }

    private void loadItineraryForEditing(int id) {

        Cursor cursor = dbHelper.getDataById(id);

        try {
            if (cursor.moveToFirst()) {

                etDestination.setText(cursor.getString(
                        cursor.getColumnIndexOrThrow("destination")));

                etLocation.setText(cursor.getString(
                        cursor.getColumnIndexOrThrow("location")));

                startDate = cursor.getString(
                        cursor.getColumnIndexOrThrow("start_date"));

                endDate = cursor.getString(
                        cursor.getColumnIndexOrThrow("end_date"));

                etActivities.setText(cursor.getString(
                        cursor.getColumnIndexOrThrow("activities")));

                tvStartDate.setText("Start date: " + startDate);
                tvEndDate.setText("End date: " + endDate);

                selectedId = id;
                btnSave.setText("Update Itinerary");

                Toast.makeText(this,
                        "Edit the details and tap Update",
                        Toast.LENGTH_SHORT).show();
            }
        } finally {
            cursor.close();
        }
    }

    private void confirmDelete(int position) {

        int id = itineraryIds.get(position);

        new AlertDialog.Builder(this)
                .setTitle("Delete Itinerary")
                .setMessage("Do you want to delete this travel plan?")
                .setPositiveButton("Delete", (dialog, which) -> {

                    try {
                        dbHelper.deleteData(id);

                        if (selectedId == id) {
                            clearFields();
                        }

                        loadItineraries();

                        Toast.makeText(this,
                                "Itinerary deleted",
                                Toast.LENGTH_SHORT).show();

                    } catch (Exception e) {
                        Toast.makeText(this,
                                "Unable to delete itinerary",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void clearFields() {

        etDestination.setText("");
        etLocation.setText("");
        etActivities.setText("");

        startDate = "";
        endDate = "";
        selectedId = -1;

        tvStartDate.setText("Start date: Not selected");
        tvEndDate.setText("End date: Not selected");

        btnSave.setText("Save Itinerary");
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }
}
