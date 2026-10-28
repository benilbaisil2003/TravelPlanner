
package com.example.travelitineraryplanner;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "TravelPlanner.db";
    private static final int DATABASE_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE itineraries (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "destination TEXT NOT NULL, " +
                "location TEXT, " +
                "start_date TEXT NOT NULL, " +
                "end_date TEXT NOT NULL, " +
                "activities TEXT)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db,
                          int oldVersion, int newVersion) {
        // Preserve existing data when upgrading this version.
    }

    // CREATE: Save itinerary
    public long insertData(String destination, String location,
                           String startDate, String endDate,
                           String activities) {
        SQLiteDatabase db = getWritableDatabase();

        String sql = "INSERT INTO itineraries " +
                "(destination, location, start_date, end_date, activities) " +
                "VALUES (?, ?, ?, ?, ?)";

        android.database.sqlite.SQLiteStatement statement =
                db.compileStatement(sql);

        statement.bindString(1, destination);
        statement.bindString(2, location);
        statement.bindString(3, startDate);
        statement.bindString(4, endDate);
        statement.bindString(5, activities);

        long id = statement.executeInsert();
        statement.close();

        return id;
    }

    // READ: Retrieve all itineraries
    public Cursor getData() {
        SQLiteDatabase db = getReadableDatabase();

        return db.rawQuery(
                "SELECT * FROM itineraries ORDER BY start_date ASC",
                null
        );
    }

    // READ: Retrieve one itinerary
    public Cursor getDataById(int id) {
        SQLiteDatabase db = getReadableDatabase();

        return db.rawQuery(
                "SELECT * FROM itineraries WHERE id = ?",
                new String[]{String.valueOf(id)}
        );
    }

    // UPDATE: Edit saved itinerary
    public int updateData(int id, String destination,
                          String location, String startDate,
                          String endDate, String activities) {
        SQLiteDatabase db = getWritableDatabase();

        String sql = "UPDATE itineraries SET destination = ?, " +
                "location = ?, start_date = ?, end_date = ?, " +
                "activities = ? WHERE id = ?";

        android.database.sqlite.SQLiteStatement statement =
                db.compileStatement(sql);

        statement.bindString(1, destination);
        statement.bindString(2, location);
        statement.bindString(3, startDate);
        statement.bindString(4, endDate);
        statement.bindString(5, activities);
        statement.bindLong(6, id);

        int rows = statement.executeUpdateDelete();
        statement.close();

        return rows;
    }

    // DELETE: Remove saved itinerary
    public int deleteData(int id) {
        SQLiteDatabase db = getWritableDatabase();

        return db.delete(
                "itineraries",
                "id = ?",
                new String[]{String.valueOf(id)}
        );
    }
}
