package com.example.sharedpreference.Clock;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.sharedpreference.ClockActivity;
import com.example.sharedpreference.R;

import java.util.Calendar;

public class ClockDialog extends Dialog {

    private Button dateSetButton;
    private Button confirmClockButton;
    private Button cancelClockButton;
    private TimePicker timePicker;
    public Calendar calendar;
    private String date;
    private String alertTime;
    private ClockDialog clockDialog;

    //初始化时间设置
    private void init(){
        calendar.setTimeInMillis(System.currentTimeMillis());
        dateSetButton.setText(Utils.toDateString(calendar));
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        timePicker.setIs24HourView(true);
        timePicker.setCurrentHour(hour);
        timePicker.setCurrentMinute(minute);
    }
    public ClockDialog(Context context) {
        super(context);
        setContentView(R.layout.clock_dialog);

        clockDialog = this;
        this.setTitle("设置时间提醒我");

        calendar = Calendar.getInstance();
        timePicker = (TimePicker)findViewById(R.id.time_picker);
        dateSetButton = (Button)findViewById(R.id.date_button);
        confirmClockButton = (Button)findViewById(R.id.confirm_clock_button);
        cancelClockButton = (Button)findViewById(R.id.cancel_clock_button);

        init();

        dateSetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener(){
                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        //设置日期
                        calendar.set(year, monthOfYear, dayOfMonth);
                        date = Utils.toDateString(calendar);
                        dateSetButton.setText(date);
                    }}, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        confirmClockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());
                calendar.set(Calendar.MINUTE, timePicker.getCurrentMinute());
                alertTime = calendar.getTimeInMillis()+"";
                if(Utils.timeTransfer(alertTime).length() == 0)
                    Toast.makeText(getContext(), "请选择有效日期", Toast.LENGTH_SHORT).show();
                clockDialog.cancel();
            }
        });
        cancelClockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clockDialog.cancel();
            }
        });
    }

    public String getAlertTime() {
        return alertTime;
    }

    public void setAlertTime(String alertTime) {
        this.alertTime = alertTime;
    }
}

