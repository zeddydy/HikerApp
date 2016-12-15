package com.hiker.app.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyStorageManager extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "data.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TRACKS_TABLE_NAME = "tracks";
    public static final String TRACKS_COLUMN_ID = "_id";
    public static final String TRACKS_COLUMN_NAME = "name";
    public static final String TRACKS_COLUMN_START_TIME = "start_time";
    public static final String TRACKS_COLUMN_END_TIME = "end_time";
    public static final String TRACKS_COLUMN_DISTANCE = "distance";
    public static final String TRACKS_COLUMN_ASCEND = "ascend";
    public static final String TRACKS_COLUMN_DESCEND = "descend";
    public static final String TRACKS_COLUMN_STEPS = "steps";

    public static final String POINTS_TABLE_NAME = "points";
    public static final String POINTS_COLUMN_ID = "_id";
    public static final String POINTS_COLUMN_TRACK_ID = "track_id";
    public static final String POINTS_COLUMN_LATITUDE = "latitude";
    public static final String POINTS_COLUMN_LONGITUDE = "longitude";
    public static final String POINTS_COLUMN_ALTITUDE = "altitude";
    public static final String POINTS_COLUMN_SPEED = "speed";

    public MyStorageManager(Context context) {
        super(context, DATABASE_NAME , null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TRACKS_TABLE_NAME + "(" +
                TRACKS_COLUMN_ID + " INTEGER PRIMARY KEY, " +
                TRACKS_COLUMN_NAME + " TEXT," +
                TRACKS_COLUMN_START_TIME + " INTEGER," +
                TRACKS_COLUMN_END_TIME + " INTEGER," +
                TRACKS_COLUMN_DISTANCE + " INTEGER," +
                TRACKS_COLUMN_ASCEND + " INTEGER," +
                TRACKS_COLUMN_DESCEND + " INTEGER," +
                TRACKS_COLUMN_STEPS + " INTEGER)"
        );

        db.execSQL("CREATE TABLE " + POINTS_TABLE_NAME + "(" +
                POINTS_COLUMN_ID + " INTEGER PRIMARY KEY, " +
                POINTS_COLUMN_TRACK_ID + " LONG, " +
                POINTS_COLUMN_LATITUDE + " DOUBLE, " +
                POINTS_COLUMN_LONGITUDE + " DOUBLE, " +
                POINTS_COLUMN_ALTITUDE + " DOUBLE, " +
                POINTS_COLUMN_SPEED + " FLOAT, " +
                "FOREIGN KEY(" + POINTS_COLUMN_TRACK_ID + ") REFERENCES " + TRACKS_TABLE_NAME + "(" + TRACKS_COLUMN_ID + "))"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TRACKS_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + POINTS_TABLE_NAME);
        onCreate(db);
    }

    /* Table Tracks */
    public long insertTrack(String name) { //TODO Ajouts de Start Time
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(TRACKS_COLUMN_NAME, name);
        contentValues.put(TRACKS_COLUMN_START_TIME, 0);
        contentValues.put(TRACKS_COLUMN_END_TIME, 0);
        contentValues.put(TRACKS_COLUMN_DISTANCE, 0);
        contentValues.put(TRACKS_COLUMN_ASCEND, 0);
        contentValues.put(TRACKS_COLUMN_DESCEND, 0);
        contentValues.put(TRACKS_COLUMN_STEPS, 0);
        return db.insert(TRACKS_TABLE_NAME, null, contentValues);
    }

    public long updateTrack(long id, int distance, int steps) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(TRACKS_COLUMN_DISTANCE, distance);
        contentValues.put(TRACKS_COLUMN_STEPS, steps);
        return db.update(TRACKS_TABLE_NAME, contentValues, TRACKS_COLUMN_ID + "=?", new String[]{Long.toString(id)});
    }

    public Cursor getTrack(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM " + TRACKS_TABLE_NAME + " WHERE " +
                TRACKS_COLUMN_ID + "=?", new String[]{Long.toString(id)});
        return res;
    }

    public Cursor getAllTracksDesc() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM " + TRACKS_TABLE_NAME +
                " ORDER BY " + TRACKS_COLUMN_ID + " DESC", null);
        return res;
    }

    /* Table Points */
    public long insertPoint(long key, double latitude, double longitude, double altitude, float speed) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(POINTS_COLUMN_TRACK_ID, key);
        contentValues.put(POINTS_COLUMN_LATITUDE, latitude);
        contentValues.put(POINTS_COLUMN_LONGITUDE, longitude);
        contentValues.put(POINTS_COLUMN_ALTITUDE, altitude);
        contentValues.put(POINTS_COLUMN_SPEED, speed);
        return db.insert(POINTS_TABLE_NAME, null, contentValues);
    }

    public Cursor getPoints(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM " + POINTS_TABLE_NAME + " WHERE " +
                TRACKS_COLUMN_ID + "=? ", new String[]{Integer.toString(id)});
        return res;
    }

    public Cursor getPointsOfTrack(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM " + POINTS_TABLE_NAME + " WHERE " +
                POINTS_COLUMN_TRACK_ID + "=? ",  new String[]{Long.toString(id)});
        return res;
    }

    public Cursor getAllPoints() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM " + POINTS_TABLE_NAME, null);
        return res;
    }

}
