package com.coretal.carinspection.models;

import android.os.Environment;

import com.coretal.carinspection.utils.Contents;
import com.coretal.carinspection.utils.DateHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by Kangtle_R on 1/27/2018.
 */

public class DateAndPicture {
    public static String STATUS_NO_CHANED = "NO_CHANGE";
    public static String STATUS_DELETED = "REMOVED";
    public static String STATUS_NEW = "ADDED";
    public static String STATUS_CHANGED = "MODIFIED";

    public Date date;
    public String dateStr;

    public String pictureId = "";
    public String oldPictureId = "";
    public String pictureURL = "";
    public String type;

    public String status;

    public DateAndPicture(String dateStr, String pictureId, String type, String status){
        this.dateStr = dateStr;
        this.date = DateHelper.stringToDate(dateStr);
        this.type = type;
        this.status = status;
        if(this.status.isEmpty()) this.status = STATUS_NO_CHANED;
        this.setPictureId(pictureId);
    }

    public DateAndPicture(JSONObject jsonObject) throws JSONException {
        this.dateStr = jsonObject.optString(Contents.JsonDateAndPictures.DATE);
        this.date = DateHelper.stringToDate(dateStr);
        this.type = jsonObject.optString(Contents.JsonDateAndPictures.TYPE);
        this.status = jsonObject.optString(Contents.JsonDateAndPictures.STATUS);
        if(this.status.isEmpty()) this.status = STATUS_NO_CHANED;
        this.setPictureId(jsonObject.optString(Contents.JsonDateAndPictures.PICTUREID));
    }

    public void setPictureId(String pictureId) {
        if (pictureId.contains("#")){
            String[] pictureIds = pictureId.split("#");
            this.oldPictureId = pictureIds[0];
            this.pictureId = pictureIds[1];
        }else{
            if (this.status.equals(STATUS_NEW)){
                this.pictureId = pictureId;
            }else{
                if (this.oldPictureId.isEmpty()) this.oldPictureId = this.pictureId;
                this.pictureId = pictureId;
            }
        }

        if(!this.pictureId.isEmpty()) {
            boolean isNewPicture = this.pictureId.split("_").length == 4;
            if (isNewPicture) {
                this.pictureURL = Contents.EXTERNAL_PICTURES_DIR_PATH + "/" + this.pictureId + ".jpg";
            }else{
                this.pictureURL = String.format(Contents.API_GET_PICTURE_BY_ID, Contents.PHONE_NUMBER, this.pictureId);
            }
        }
    }

    public JSONObject getJSONObject() throws JSONException {
        String concatPictureId = pictureId;
        if(oldPictureId!=null && !oldPictureId.isEmpty() && !oldPictureId.equals(pictureId)){
            concatPictureId = oldPictureId + "#" + pictureId;
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.putOpt(Contents.JsonDateAndPictures.DATE, dateStr);
        jsonObject.putOpt(Contents.JsonDateAndPictures.PICTUREID, concatPictureId);
        jsonObject.putOpt(Contents.JsonDateAndPictures.TYPE, type);
        jsonObject.putOpt(Contents.JsonDateAndPictures.STATUS, status);
        return jsonObject;
    }
}
