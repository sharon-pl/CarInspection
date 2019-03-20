package com.coretal.carinspection.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.coretal.carinspection.activities.MainActivity;
import com.coretal.carinspection.models.Submission;
import com.coretal.carinspection.models.SubmissionFile;
import com.coretal.carinspection.utils.MyHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Kangtle_R on 12/25/2017.
 */

public class DBHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 3;
    public static final String DATABASE_NAME = "db_car_inspection";

    public static final String TABLE_SUBMISSION = "submission_table";
    public static final String TABLE_SUBMISSION_ID = "id";
    public static final String TABLE_SUBMISSION_STATUS = "status";
    public static final String TABLE_SUBMISSION_ERROR_DETAIL = "error_detail";
    public static final String TABLE_SUBMISSION_NUM_TRY = "num_try";
    public static final String TABLE_SUBMISSION_TYPE = "type";
    public static final String TABLE_SUBMISSION_VEHICLE_PLATE = "vehicle_plate";
    public static final String TABLE_SUBMISSION_MONTH = "month";
    public static final String TABLE_SUBMISSION_DATE = "date";
    public static final String TABLE_SUBMISSION_NOTES = "notes";
    public static final String TABLE_SUBMISSION_LOCATION_LONG = "location_long";
    public static final String TABLE_SUBMISSION_LOCATION_LAT = "location_lat";
    public static final String TABLE_SUBMISSION_STARTED_AT = "started_at";
    public static final String TABLE_SUBMISSION_ENDED_AT = "ended_at";

    public static final String TABLE_SUBMISSION_FILE = "submission_file";
    public static final String TABLE_SUBMISSION_FILE_ID = "id";
    public static final String SUBMISSION_ID = "submission_id";
    public static final String TABLE_SUBMISSION_FILE_PICTURE_ID = "picture_id";
    public static final String TABLE_SUBMISSION_FILE_TYPE = "type";
    public static final String TABLE_SUBMISSION_FILE_LOCATION = "location";

    private final Context context;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, 3);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String strCreateSubmissionTable = "CREATE TABLE IF NOT EXISTS " + TABLE_SUBMISSION + " (" +
                TABLE_SUBMISSION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TABLE_SUBMISSION_STATUS + " TEXT, " +
                TABLE_SUBMISSION_ERROR_DETAIL + " TEXT, " +
                TABLE_SUBMISSION_NUM_TRY + " INTEGER, " +
                TABLE_SUBMISSION_TYPE + " TEXT, " +
                TABLE_SUBMISSION_VEHICLE_PLATE + " TEXT, " +
                TABLE_SUBMISSION_MONTH + " INTEGER, " +
                TABLE_SUBMISSION_DATE + " INTEGER DEFAULT CURRENT_TIMESTAMP, " +
                TABLE_SUBMISSION_NOTES + " TEXT, " +
                TABLE_SUBMISSION_LOCATION_LONG + " TEXT, " +
                TABLE_SUBMISSION_LOCATION_LAT + " TEXT, " +
                TABLE_SUBMISSION_STARTED_AT + " INTEGER, " +
                TABLE_SUBMISSION_ENDED_AT + " INTEGER" + ")";
        db.execSQL(strCreateSubmissionTable); // execute the query...

        String strCreateFileTable = "CREATE TABLE IF NOT EXISTS " + TABLE_SUBMISSION_FILE + " (" +
                TABLE_SUBMISSION_FILE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                SUBMISSION_ID + " INTEGER, " +
                TABLE_SUBMISSION_FILE_PICTURE_ID + " TEXT, " +
                TABLE_SUBMISSION_FILE_TYPE + " TEXT, " +
                TABLE_SUBMISSION_FILE_LOCATION + " TEXT " +  ")";
        db.execSQL(strCreateFileTable); // execute the query...
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public long newSubmission(String vPlate){
        SQLiteDatabase db = this.getReadableDatabase();

        ContentValues initialValues = new ContentValues();
        boolean isConnected = MyHelper.isConnectedInternet(context);
        String type = isConnected? "ONLINE": "OFFLINE";

        Date currentDate = Calendar.getInstance().getTime();
        int thisMonth = Calendar.getInstance().get(Calendar.MONTH);

        initialValues.put(TABLE_SUBMISSION_STATUS, Submission.STATUS_DRAFT);
        initialValues.put(TABLE_SUBMISSION_NUM_TRY, 0);
        initialValues.put(TABLE_SUBMISSION_TYPE, type);
        initialValues.put(TABLE_SUBMISSION_VEHICLE_PLATE, vPlate);
        initialValues.put(TABLE_SUBMISSION_MONTH, thisMonth);
        initialValues.put(TABLE_SUBMISSION_DATE, System.currentTimeMillis());
        initialValues.put(TABLE_SUBMISSION_STARTED_AT, System.currentTimeMillis());

        return db.insert(TABLE_SUBMISSION, null, initialValues);
    }

    public boolean checkUnsubmittedSubmission(String vehicleNumber){
        SQLiteDatabase db = this.getReadableDatabase();
        String strGetAll = String.format("select * from %s where %s='%s' and (%s='%s' or %s='%s')",
                                        TABLE_SUBMISSION,
                                        TABLE_SUBMISSION_VEHICLE_PLATE, vehicleNumber,
                                        TABLE_SUBMISSION_STATUS, Submission.STATUS_READY_TO_SUBMIT,
                                        TABLE_SUBMISSION_STATUS, Submission.STATUS_FAILED);
        Cursor cursor = db.rawQuery(strGetAll, null);
        boolean exitst = false;
        if(cursor.moveToFirst()){
            exitst = true;
        }
        cursor.close();
        return exitst;
    }

    public Submission getDraftSubmission(){
        SQLiteDatabase db = this.getReadableDatabase();
        String strGetAll = String.format("select * from %s where %s='%s'", TABLE_SUBMISSION, TABLE_SUBMISSION_STATUS, Submission.STATUS_DRAFT);
        Cursor cursor = db.rawQuery(strGetAll, null);
        Submission submission = null;
        if(cursor.moveToFirst()){
            submission = new Submission(cursor);
        }
        cursor.close();
        return submission;
    }

    public List<Submission> getAllSubmissions(){
        SQLiteDatabase db = this.getReadableDatabase();
        String strGetAll = String.format("select * from %s", TABLE_SUBMISSION);
        Cursor cursor = db.rawQuery(strGetAll, null);
        List<Submission> submissions = new ArrayList<>();
        while (cursor.moveToNext()){
            submissions.add(new Submission(cursor));
        }
        cursor.close();
        return submissions;
    }

    public List<Submission> getSubmissionsToSubmit(){
        SQLiteDatabase db = this.getReadableDatabase();
        String strGetAll = String.format("select * from %s where %s='%s' or %s='%s'", TABLE_SUBMISSION,
                        TABLE_SUBMISSION_STATUS, Submission.STATUS_READY_TO_SUBMIT,
                        TABLE_SUBMISSION_STATUS, Submission.STATUS_FAILED);
        Cursor cursor = db.rawQuery(strGetAll, null);
        List<Submission> submissions = new ArrayList<>();
        while (cursor.moveToNext()){
            submissions.add(new Submission(cursor));
        }
        cursor.close();
        return submissions;
    }

    public void removeDraftSubmission() {
        SQLiteDatabase db = this.getReadableDatabase();
        String removeDraftQuery = String.format("delete from %s where %s='%s'", TABLE_SUBMISSION, TABLE_SUBMISSION_STATUS, Submission.STATUS_DRAFT);
        db.execSQL(removeDraftQuery);
    }

    public void setSubmissionStatus(Submission submission) {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues updateValues = new ContentValues();
        updateValues.put(TABLE_SUBMISSION_STATUS, submission.status);
        if (submission.status.equals(Submission.STATUS_FAILED)){
            updateValues.put(TABLE_SUBMISSION_NUM_TRY, submission.numTry+1);
            updateValues.put(TABLE_SUBMISSION_ERROR_DETAIL, submission.errorDetail);
        }
        db.update(TABLE_SUBMISSION, updateValues, TABLE_SUBMISSION_ID + "=" + submission.id, null);
    }

    public void setMonthForDraftSubmission(int month){
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues updateValues = new ContentValues();
        updateValues.put(TABLE_SUBMISSION_MONTH, month);
        db.update(TABLE_SUBMISSION, updateValues, String.format("%s='%s'", TABLE_SUBMISSION_STATUS, Submission.STATUS_DRAFT), null);
    }

    public void setNotesForDraftSubmission(String notes){
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues updateValues = new ContentValues();
        updateValues.put(TABLE_SUBMISSION_NOTES, notes);
        db.update(TABLE_SUBMISSION, updateValues, String.format("%s='%s'", TABLE_SUBMISSION_STATUS, Submission.STATUS_DRAFT), null);
    }

    public void setStatusForDraftSubmission(String status){
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues updateValues = new ContentValues();
        updateValues.put(TABLE_SUBMISSION_STATUS, status);
        db.update(TABLE_SUBMISSION, updateValues, String.format("%s='%s'", TABLE_SUBMISSION_STATUS, Submission.STATUS_DRAFT), null);
    }

    public boolean fileExists(String fileLocation){
        SQLiteDatabase db = this.getReadableDatabase();
        String strGetAll = String.format("select * from %s where %s='%s'", TABLE_SUBMISSION_FILE, TABLE_SUBMISSION_FILE_LOCATION, fileLocation);
        Cursor cursor = db.rawQuery(strGetAll, null);

        boolean fileExists = false;
        if(cursor.moveToFirst()){
            fileExists = true;
        }
        cursor.close();

        return fileExists;
    }

    public long newFile(String pictureId, String fileLocation){
        SQLiteDatabase db = this.getReadableDatabase();
        Submission currentSubmission = getDraftSubmission();

        ContentValues initialValues = new ContentValues();

        initialValues.put(SUBMISSION_ID, currentSubmission.id);

        initialValues.put(TABLE_SUBMISSION_FILE_PICTURE_ID, pictureId);

        initialValues.put(TABLE_SUBMISSION_FILE_LOCATION, fileLocation);

        return db.insert(TABLE_SUBMISSION_FILE, null, initialValues);
    }

    public void setFileLocation(long fileID, String location){
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues updateValues = new ContentValues();
        updateValues.put(TABLE_SUBMISSION_FILE_LOCATION, location);
        db.update(TABLE_SUBMISSION_FILE, updateValues, "id=" + fileID, null);
    }

    public void setFileType(long fileID, String type){
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues updateValues = new ContentValues();
        updateValues.put(TABLE_SUBMISSION_FILE_TYPE, type);
        db.update(TABLE_SUBMISSION_FILE, updateValues, "id=" + fileID, null);
    }

    public void removeFile(String pictureId){
        SQLiteDatabase db = this.getReadableDatabase();
        db.delete(TABLE_SUBMISSION_FILE, String.format("%s='%s'", TABLE_SUBMISSION_FILE_PICTURE_ID, pictureId), null);
    }

    public void resetNumtry(){
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues updateValues = new ContentValues();
        updateValues.put(TABLE_SUBMISSION_NUM_TRY, 0);
        db.update(TABLE_SUBMISSION, updateValues, String.format("%s='%s'", TABLE_SUBMISSION_STATUS, Submission.STATUS_FAILED), null);
    }

    public long getLastInsertFileId(){
        long lastInsertId = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        String query = String.format("select %s from %s order by %s desc", TABLE_SUBMISSION_FILE_ID, TABLE_SUBMISSION_FILE, TABLE_SUBMISSION_FILE_ID);
        Cursor cursor = db.rawQuery(query, null);
        if(cursor.moveToFirst()){
            lastInsertId = cursor.getLong(cursor.getColumnIndex(TABLE_SUBMISSION_FILE_ID));
        }
        cursor.close();
        return lastInsertId;
    }

    public List<SubmissionFile> getFilesForSubmissionId(int submissionId){
        SQLiteDatabase db = this.getReadableDatabase();
        String strGetAll = String.format("select * from %s where %s=%s", TABLE_SUBMISSION_FILE, SUBMISSION_ID, String.valueOf(submissionId));
        Cursor cursor = db.rawQuery(strGetAll, null);
        List<SubmissionFile> submissionFiles = new ArrayList<>();
        while (cursor.moveToNext()){
            submissionFiles.add(new SubmissionFile(cursor));
        }
        cursor.close();
        return submissionFiles;
    }

    public String getPictureId(int submissionId, String fileType){
        String pictureId = "";
        SQLiteDatabase db = this.getReadableDatabase();
        String strQuery = String.format("select * from %s where %s=%s and %s='%s'", TABLE_SUBMISSION_FILE,
                                                    SUBMISSION_ID, submissionId,
                                                    TABLE_SUBMISSION_FILE_TYPE, fileType);
        Cursor cursor = db.rawQuery(strQuery, null);
        SubmissionFile submissionFile = null;
        if (cursor.moveToFirst()){
            submissionFile = new SubmissionFile(cursor);
            pictureId = submissionFile.pictureId;
        }
        cursor.close();
        return pictureId;
    }

    public void clearSubmissions() {
        SQLiteDatabase db = this.getReadableDatabase();
        db.delete(TABLE_SUBMISSION, String.format("%s!='%s'", TABLE_SUBMISSION_STATUS, Submission.STATUS_DRAFT), null);
    }
}
