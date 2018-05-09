package com.coretal.carinspection.models;

import android.database.Cursor;

import com.coretal.carinspection.db.DBHelper;
import com.coretal.carinspection.utils.DateHelper;

import java.util.Date;

/**
 * Created by Kangtle_R on 1/25/2018.
 */

public class Submission {
    public static String STATUS_DRAFT = "draft";
    public static String STATUS_READY_TO_SUBMIT = "ready_to_submit";
    public static String STATUS_FAILED = "failed";
    public static String STATUS_SUBMITTED = "submitted";

    public int id;
    public String status;
    public String errorDetail;
    public int numTry;
    public String type; //Online/Offline
    public String vehiclePlate;
    public int month;
    public Date date;
    public String notes;
    public double locationLong;
    public double locationLat;
    public Date startedAt;
    public Date endedAt;

    public int failedCount = 0;

    public Submission(Cursor cursor){
        int id = cursor.getInt(cursor.getColumnIndex(DBHelper.TABLE_SUBMISSION_ID));
        String status = cursor.getString(cursor.getColumnIndex(DBHelper.TABLE_SUBMISSION_STATUS));
        String errorDetail = cursor.getString(cursor.getColumnIndex(DBHelper.TABLE_SUBMISSION_ERROR_DETAIL));
        int numTry = cursor.getInt(cursor.getColumnIndex(DBHelper.TABLE_SUBMISSION_NUM_TRY));
        String type = cursor.getString(cursor.getColumnIndex(DBHelper.TABLE_SUBMISSION_TYPE));
        String vehiclePlate = cursor.getString(cursor.getColumnIndex(DBHelper.TABLE_SUBMISSION_VEHICLE_PLATE));
        int month = cursor.getInt(cursor.getColumnIndex(DBHelper.TABLE_SUBMISSION_MONTH));
        Date date = DateHelper.timestampToDate(cursor.getLong(cursor.getColumnIndex(DBHelper.TABLE_SUBMISSION_DATE)));
        String notes = cursor.getString(cursor.getColumnIndex(DBHelper.TABLE_SUBMISSION_NOTES));
        double locationLong = cursor.getDouble(cursor.getColumnIndex(DBHelper.TABLE_SUBMISSION_LOCATION_LONG));
        double locationLat = cursor.getDouble(cursor.getColumnIndex(DBHelper.TABLE_SUBMISSION_LOCATION_LAT));
        long startTimeStamp = cursor.getLong(cursor.getColumnIndex(DBHelper.TABLE_SUBMISSION_STARTED_AT));
        if(startTimeStamp > 0) startedAt = DateHelper.timestampToDate(startTimeStamp);
        long endTimeStamp = cursor.getLong(cursor.getColumnIndex(DBHelper.TABLE_SUBMISSION_ENDED_AT));
        if(endTimeStamp > 0) endedAt = DateHelper.timestampToDate(endTimeStamp);

        this.id = id;
        this.status = status;
        this.numTry = numTry;
        this.type = type;
        this.vehiclePlate = vehiclePlate;
        this.month = month;
        this.date = date;
        this.notes = notes;
        this.locationLong = locationLong;
        this.locationLat = locationLat;
        this.errorDetail = errorDetail;
    }
}
