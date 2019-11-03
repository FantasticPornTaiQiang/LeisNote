package com.example.sharedpreference;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sharedpreference.Clock.AlarmService;
import com.example.sharedpreference.Clock.ClockDialog;
import com.example.sharedpreference.Clock.Utils;
import com.example.sharedpreference.Database.MySQLiteOpenHelper;
import com.example.sharedpreference.Database.Note;
import com.example.sharedpreference.Music.Music;

import org.litepal.LitePal;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.util.Calendar;
import java.util.Date;


public class EditActivity extends AppCompatActivity {

    private static final int GET_TITLE = 0;
    private static final int GET_CONTENT = 1;
    private static final int REQUEST_CODE_CLOCK = 1;
    private static final int REQUEST_CODE_MUSIc = 2;

    private static final String TAG = "EditActivity";
    
    private int id;//唯一id
    private int mode;
    private int position;
    private String datetime;
    private String content;
    private String alertTime = "";
    private Calendar calendar;
    private String tempTime;
    private boolean hasClock = false;
    private Music music;
    private MediaPlayer mediaPlayer = new MediaPlayer();

    Context mContext;

    TextView cancelButton;
    TextView saveButton;
    TextView titletextView;
    EditText titleEditText;
    EditText contentEditText;
    TextView clockButton;
    ClockDialog clockDialog;
    TextView musicButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        initEvent();
        initView();
    }

    private void initEvent(){
        mContext = EditActivity.this;
        Intent intent = getIntent();
        id = intent.getIntExtra("id",-1);
        mode = intent.getIntExtra("mode",-1);
        position = intent.getIntExtra("position", -1);
        music = getMusic();
        if(music == null) {
            music = new Music();
        }
    }

    private void initView(){
        titleEditText = findViewById(R.id.title_edit_text);
        titletextView = findViewById(R.id.edit_title_text_view);
        contentEditText = findViewById(R.id.content_edit_text);
        if(mode == MainActivity.EDIT_STATE_MODIFY){
            titleEditText.setText(getNote(GET_TITLE));

            titleEditText.setSelection(titleEditText.length());
            contentEditText.setText(getNote(GET_CONTENT));
            contentEditText.setSelection(contentEditText.length());
        }
        cancelButton = findViewById(R.id.edit_cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("change_type", MainActivity.CHANGE_TYPE_CANCEL);
                setResult(RESULT_FIRST_USER, intent);
                EditActivity.this.finish();
            }
        });
        //获取输入的内容以及标题载入数据库
        saveButton = findViewById(R.id.edit_save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String titleText;
                if(!titleEditText.getText().toString().trim().equals("")) titleText = titleEditText.getText().toString();
                else{
                    titleText = titleEditText.getHint().toString();
                }
                String contentText = contentEditText.getText().toString();
                Intent intent = new Intent();
                //如果是直接添加内容
                if (mode == MainActivity.EDIT_STATE_CREATE){
                    saveNote(titleText, contentText, MainActivity.EDIT_STATE_CREATE);
                    intent.putExtra("change_type", MainActivity.CHANGE_TYPE_CREATE);
                }
                //如果是对现有内容进行修改
                else if (mode == MainActivity.EDIT_STATE_MODIFY){
                    saveNote(titleText, contentText, MainActivity.EDIT_STATE_MODIFY);
                    intent.putExtra("change_type", MainActivity.CHANGE_TYPE_MODIFY);
                    intent.putExtra("positionId", position);
                }
                setResult(RESULT_FIRST_USER, intent);
                EditActivity.this.finish();
            }
        });

        clockButton = findViewById(R.id.clock_button);
        clockButton.setOnClickListener( new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditActivity.this, ClockActivity.class);
                startActivityForResult(intent, REQUEST_CODE_CLOCK);
            }
        });

        musicButton = findViewById(R.id.music_button);
        musicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final TextView textView = new TextView(EditActivity.this);
                if(music != null && music.getName().length() != 0){
                    textView.setText(music.getName());
                } else {
                    textView.setText("");
                    textView.setHint("请选择音乐");
                }
                AlertDialog.Builder musicDialogBuilder = new AlertDialog.Builder(EditActivity.this);
                musicDialogBuilder.setTitle("添加音乐")
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setView(textView)
                        .setPositiveButton("选择", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(EditActivity.this, MusicActivity.class);
                                startActivityForResult(intent, REQUEST_CODE_MUSIc);
                            }
                        })
                        .setNeutralButton("移除", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if(music != null){
                                    music.setUrl("");
                                    music.setName("");
                                }
                                dialog.cancel();
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                dialog.cancel();
                            }
                        })
                        .setCancelable(false)
                        .show();
            }
        });

        playMusic();
    }
    //保存
    private void saveNote(String titleText, String contentText, int mode)
    {
        switch (mode){
            case MainActivity.EDIT_STATE_CREATE:
                Note noteCreate = new Note();
                noteCreate.setTitle(titleText);
                noteCreate.setContent(contentText);
                noteCreate.setCreateTime(getTime());
                if(music != null){
                    noteCreate.setMusicName(music.getName());
                    noteCreate.setMusicURL(music.getUrl());
                } else {
                    noteCreate.setMusicName("");
                    noteCreate.setMusicURL("");
                }
                noteCreate.save();
                break;
            case MainActivity.EDIT_STATE_MODIFY:
                Note noteModify = LitePal.find(Note.class, id);
                noteModify.setTitle(titleText);
                noteModify.setContent(contentText);
                if(music != null){
                    noteModify.setMusicName(music.getName());
                    noteModify.setMusicURL(music.getUrl());
                } else {
                    noteModify.setMusicName("");
                    noteModify.setMusicURL("");
                }
                noteModify.update(id);
                break;
            default:
                break;
        }
        if(hasClock && !alertTime.equals(""))
        {
            //setClock();
        }
    }

    //获取文本
    private String getNote(int what){
        Note note = LitePal.find(Note.class, id);
        Log.d("1111", note.getTitle());
        switch (what) {
            case GET_TITLE:
                return note.getTitle();
            case GET_CONTENT:
                return note.getContent();
            default:
                return "";
        }
    }

    private Music getMusic(){
        Note note = LitePal.find(Note.class, id);
        if(note != null){
            Log.d("aaaa", note.getMusicName());
            String musicName = note.getMusicName();
            String musicUrl = note.getMusicURL();
            if(musicName.equals("") || musicUrl.equals("")){
                return null;
            } else {
                Music music1 = new Music();
                music1.setName(note.getMusicName());
                music1.setUrl(note.getMusicURL());
                return music1;
            }
        }
        return null;
    }

    public static String getTime()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        String str = sdf.format(date);
        return str;
    }

    private void setClock(){
        Intent intent = new Intent(EditActivity.this, AlarmService.class);
        intent.putExtra("calendar", calendar);
        intent.putExtra("noteId", id);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startService(intent);
//        //发送一条启动闹铃图标的广播
//        Intent intentIcon = new Intent("com.gaozhidong.android.Color");
//        intentIcon.putExtra("noteId", noteId);
//        EditActivity.this.sendBroadcast(intentIcon);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_CLOCK && resultCode == RESULT_FIRST_USER){
            alertTime = data.getStringExtra("alert_time");
            calendar = (Calendar) data.getSerializableExtra("calendar");
            hasClock = data.getBooleanExtra("has_clock", false);
        } else if(requestCode == REQUEST_CODE_MUSIc && resultCode == 2){
            String musicName = data.getStringExtra("music_name");
            String musicUrl = data.getStringExtra("music_url");
            if(musicName.length() != 0 && musicUrl.length() != 0){
                music.setName(musicName);
                music.setUrl(musicUrl);
            }
        }
    }

    private void playMusic(){
        Music playingMusic = getMusic();
        if(playingMusic != null){
            if(playingMusic.getName().length() != 0 && playingMusic.getUrl().length() != 0) {
                try {
                    mediaPlayer.setDataSource(playingMusic.getUrl());
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                } catch (IOException e) {
                    Toast.makeText(EditActivity.this, "播放错误", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }
}
