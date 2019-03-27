package com.coretal.carinspection.controls;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;

import com.coretal.carinspection.R;
import com.coretal.carinspection.utils.Contents;
import com.coretal.carinspection.utils.DateHelper;
import com.squareup.timessquare.CalendarPickerView;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Kangtle_R on 1/23/2018.
 */

public class DateEditText extends androidx.appcompat.widget.AppCompatEditText {

    private DatePickerDialog datePickerDialog;

    public DateEditText(final Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setKeyListener(null);
//        this.setFocusable(false);
        Calendar calendar = Calendar.getInstance();
        this.datePickerDialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                final Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DATE, dayOfMonth);
                DateEditText.this.setDate(calendar.getTime());
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));

        this.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date date = DateEditText.this.getDate();
                final Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                datePickerDialog.updateDate( calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));
                datePickerDialog.show();
            }
        });
    }

    public void setDate(Date date){
        String dateStr = DateHelper.dateToString(date);
        this.setDateString(dateStr);
    }

    public Date getDate(){
        String dateStr = this.getDateString();
        return DateHelper.stringToDate(dateStr);
    }

    public void setDateString(String dateString) {
        this.setText(Contents.DATE_PREFIX + dateString);
    }

    public String getDateString() {
        return this.getText().toString().replace(Contents.DATE_PREFIX, "");
    }
}
