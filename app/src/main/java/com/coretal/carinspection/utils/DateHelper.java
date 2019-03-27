package com.coretal.carinspection.utils;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Kangtle_R on 1/25/2018.
 */

public class DateHelper {
    public static SimpleDateFormat dateFormat = new SimpleDateFormat(Contents.DEFAULT_DATE_FORMAT, Locale.US);
    public static Date stringToDate(String dateStr){
        try {
            return dateFormat.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new Date();
    }

    public static Date timestampToDate(long timeStamp){
        return new Date(timeStamp);
    }

    public static String dateToString(Date date){
        return dateFormat.format(date);
    }

    public static String dateToString(Date date, String format){
        if (date == null) return "";
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.US);
        return dateFormat.format(date);
    }

    public static String datetimeToString(Date date){
        if (date == null) return "";
        String format = Contents.DEFAULT_DATE_FORMAT + " h:m:s";
        return datetimeToString(date, format);
    }

    public static String datetimeToString(Date date, String format){
        if (date == null) return "";
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.US);
        return dateFormat.format(date);
    }

    public static Date addDays(Date date, int days)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, days); //minus number would decrement the days
        return cal.getTime();
    }

    public static void testDateFormat()
    {
        Log.d("Kangtle", "===================================");
        SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd==MM==yyyy", Locale.US);
        SimpleDateFormat dateFormat3 = new SimpleDateFormat("yyyy@MM@dd", Locale.US);
        SimpleDateFormat dateFormat4 = new SimpleDateFormat("yyyy/dd/MM", Locale.US);

        Date curDate = new Date();
        String dateStr = "21/03/2019";
        String wrongDateStr = "2019/23/03";
        String u = Character.toString((char)0x0020);

        Log.d("Kangtle", dateStr);
        Log.d("Kangtle", wrongDateStr);

        Log.d("Kangtle", " " + dateStr);
        Log.d("Kangtle", " " + wrongDateStr);

        Log.d("Kangtle", " " + dateStr + " ");
        Log.d("Kangtle", " " + wrongDateStr + " ");

        Log.d("Kangtle", u + dateStr);
        Log.d("Kangtle", u + wrongDateStr);

        Log.d("Kangtle", u + dateStr + u);
        Log.d("Kangtle", u + wrongDateStr + u);

        Log.d("Kangtle", "C " + dateStr);
        Log.d("Kangtle", "W " + wrongDateStr);

        Log.d("Kangtle", dateFormat1.format(curDate));
        Log.d("Kangtle", dateFormat2.format(curDate));
        Log.d("Kangtle", dateFormat3.format(curDate));
        Log.d("Kangtle", dateFormat4.format(curDate));
        try {
            Log.d("Kangtle", dateFormat1.parse(dateStr).toString());
            Log.d("Kangtle", dateFormat1.parse(wrongDateStr).toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Log.d("Kangtle", "===================================");
    }

}
