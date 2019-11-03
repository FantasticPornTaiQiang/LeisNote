package com.example.sharedpreference;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.sharedpreference.Clock.ClockDialog;
import com.example.sharedpreference.Clock.Utils;

import org.w3c.dom.Text;

import java.util.Calendar;

public class ClockActivity extends AppCompatActivity {

    ClockDialog clockDialog;
    TextView clockTextView;
    TextView backToEditButton;
    TextView setClockButton;
    TextView clockTitleTextView;

    private String alertTime;
    public Calendar calendar;
    private boolean hasClock = false;

    @Override
    protected void onStart() {
        super.onStart();
        if(hasClock){
            clockTextView.setText("已设定闹钟：" + Utils.timeTransfer(alertTime));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clock);

        clockTitleTextView = findViewById(R.id.clock_title_text_view);
        clockTextView = findViewById(R.id.clock_text_view);

        backToEditButton = findViewById(R.id.back_to_edit_button);
        backToEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("alert_time", alertTime);
                intent.putExtra("calendar", calendar);
                intent.putExtra("has_clock", hasClock);
                setResult(RESULT_FIRST_USER, intent);
                ClockActivity.this.finish();
            }
        });

        setClockButton = findViewById(R.id.set_clock_button);
        setClockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clockDialog = new ClockDialog(ClockActivity.this);
                //添加监听器，当dialog消失即执行cancel()方法时触发的事件
                clockDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        alertTime = clockDialog.getAlertTime();
                        if(alertTime != null){
                            hasClock = true;
                            clockTextView.setText("已设定闹钟：" + Utils.timeTransfer(alertTime));
                        }
                        else{
                            clockTextView.setText("");
                            hasClock = false;
                        }
                        calendar = clockDialog.calendar;
                    }
                });
                clockDialog.show();
            }
        });
    }

}
