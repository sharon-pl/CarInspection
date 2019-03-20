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
    private CalendarPickerView calendarPickerView;
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
                String formatString = DateHelper.dateToString(calendar.getTime());
                DateEditText.this.setText(formatString);
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));

        this.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date date = DateHelper.stringToDate(DateEditText.this.getText().toString());
                final Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                datePickerDialog.updateDate( calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));
                datePickerDialog.show();
            }
        });
    }


    private void showCalendarInDialog(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );

        calendarPickerView = (CalendarPickerView) (inflater.inflate(R.layout.dialog_calendar, null, false));
        final AlertDialog theDialog = new AlertDialog.Builder(context).create();
        theDialog.setTitle("Calendar");
        theDialog.setView(calendarPickerView);
        theDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int i) {
                dialog.dismiss();
                String formatString = DateHelper.dateToString(calendarPickerView.getSelectedDate());
                DateEditText.this.setText(formatString);
            }
        });

        theDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        theDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                calendarPickerView.fixDialogDimens();
            }
        });

        calendarPickerView.setOnDateSelectedListener(new CalendarPickerView.OnDateSelectedListener() {
            @Override
            public void onDateSelected(Date date) {
                theDialog.dismiss();

                String formatString = DateHelper.dateToString(calendarPickerView.getSelectedDate(), Contents.DEFAULT_DATE_FORMAT);
                DateEditText.this.setText(formatString);
            }

            @Override
            public void onDateUnselected(Date date) {

            }
        });
        theDialog.show();
    }

    public void setDate(Date date){
        String dateStr = DateHelper.dateToString(date);
        this.setText(dateStr);
    }
}
