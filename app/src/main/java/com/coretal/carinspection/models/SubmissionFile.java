package com.coretal.carinspection.models;

import android.database.Cursor;

import com.coretal.carinspection.db.DBHelper;
import com.coretal.carinspection.utils.DateHelper;

import java.util.Date;

/**
 * Created by Kangtle_R on 3/7/2018.
 */

public class SubmissionFile {

    public int id;
    public int submissionId;
    public String pictureId;
    public String type; //Online/Offline
    public String fileLocation;

    public SubmissionFile(Cursor cursor){
        id = cursor.getInt(cursor.getColumnIndex(DBHelper.TABLE_SUBMISSION_FILE_ID));
        submissionId = cursor.getInt(cursor.getColumnIndex(DBHelper.SUBMISSION_ID));
        pictureId = cursor.getString(cursor.getColumnIndex(DBHelper.TABLE_SUBMISSION_FILE_PICTURE_ID));
        type = cursor.getString(cursor.getColumnIndex(DBHelper.TABLE_SUBMISSION_FILE_TYPE));
        fileLocation = cursor.getString(cursor.getColumnIndex(DBHelper.TABLE_SUBMISSION_FILE_LOCATION));
    }
}
