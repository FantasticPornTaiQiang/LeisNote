package com.example.sharedpreference;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.sharedpreference.Database.MySQLiteOpenHelper;
import com.example.sharedpreference.Database.Note;
import com.example.sharedpreference.Util.MyItemTouchHelper;
import com.example.sharedpreference.Util.NoteRecyclerViewAdapter;
import com.example.sharedpreference.Util.OnItemClickListener;
import com.example.sharedpreference.Util.OnItemTouchListener;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    public List<Note> noteList = new ArrayList<>();

    public static final int EDIT_STATE_CREATE = 0;//点击添加进入Edit界面
    public static final int EDIT_STATE_MODIFY = 1;//点击列表项进入Edit界面
    public static final int CHANGE_TYPE_CANCEL = 0;
    public static final int CHANGE_TYPE_MODIFY = 1;
    public static final int CHANGE_TYPE_CREATE = 2;
    private static final int REQUEST_CODE = 1;
    private static final int TIME_SORT_ASC = 0;//时间正序
    private static final int TIME_SORT_DESC = 1;//时间逆序
    private static final int DICTIONARY_SORT_ASC = 0;
    private static final int DICTIONARY_SORT_DESC = 1;

    private int changeType = -1;
    private int positionId;
    private int timeSortMode;
    private int dictionarySortMode;

    RecyclerView noteRecyclerView;
    NoteRecyclerViewAdapter noteRecyclerViewAdapter;
    TextView addNoteButton;
    TextView timeSortButton;
    TextView dictionarySortButton;
    TextView weatherButton;

//    private boolean isDestroyed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

    }

    @Override
    protected void onStart() {
        super.onStart();
        timeSortMode = TIME_SORT_ASC;
        dictionarySortMode = DICTIONARY_SORT_ASC;
        refreshList();
        noteRecyclerViewAdapter.notifyDataSetChanged();
    }

    private void initView(){
        refreshList();
        changeType = CHANGE_TYPE_CANCEL;

        addNoteButton = findViewById(R.id.add_note_button);
        addNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                intent.putExtra("mode",MainActivity.EDIT_STATE_CREATE);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

        timeSortButton = findViewById(R.id.time_sort_button);
        timeSortButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                noteList.clear();
                List<Note> timeSortList;
                switch(timeSortMode){
                    case TIME_SORT_ASC:
                        timeSortList = LitePal.order("createtime").find(Note.class);
                        for(Note note : timeSortList){
                            noteList.add(note);
                        }
                        timeSortMode = TIME_SORT_DESC;
                        break;
                    case TIME_SORT_DESC:
                        timeSortList = LitePal.order("createtime desc").find(Note.class);
                        for(Note note : timeSortList){
                            noteList.add(note);
                        }
                        timeSortMode = TIME_SORT_ASC;
                        break;
                }
                noteRecyclerViewAdapter.notifyDataSetChanged();
            }
        });
        dictionarySortButton = findViewById(R.id.dictionary_sort_button);
        dictionarySortButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                noteList.clear();
                List<Note> dictionarySortList;
                switch(dictionarySortMode){
                    case DICTIONARY_SORT_ASC:
                        dictionarySortList = LitePal.order("title collate localized  asc").find(Note.class);
                        for(Note note : dictionarySortList){
                            noteList.add(note);
                        }
                        dictionarySortMode = DICTIONARY_SORT_DESC;
                        break;
                    case DICTIONARY_SORT_DESC:
                        dictionarySortList = LitePal.order("title collate localized  desc").find(Note.class);
                        for(Note note : dictionarySortList){
                            noteList.add(note);
                        }
                        dictionarySortMode = DICTIONARY_SORT_ASC;
                        break;
                }
                noteRecyclerViewAdapter.notifyDataSetChanged();
            }
        });

        noteRecyclerView = findViewById(R.id.note_recycler_view);
        noteRecyclerViewAdapter = new NoteRecyclerViewAdapter(noteList);
        //设置布局管理器
        noteRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        //设置Adapter
        noteRecyclerView.setAdapter(noteRecyclerViewAdapter);
        noteRecyclerViewAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onClick(int position) {
                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                intent.putExtra("id", getNoteId(position));
                intent.putExtra("position", position);
                intent.putExtra("mode", MainActivity.EDIT_STATE_MODIFY);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });
        //设置分隔线
        //noteRecyclerView.addItemDecoration( new DividerGridItemDecoration(this ));
        //设置增加或删除条目的动画
        //noteRecyclerView.setItemAnimator( new DefaultItemAnimator());
        MyItemTouchHelper touchHelper = new MyItemTouchHelper(new OnItemTouchListener() {
            //拖动排序
            @Override
            public boolean onMove(int fromPosition, int toPosition) {
                if (fromPosition < toPosition) {
                    //从上往下拖动，每滑动一个item，都将list中的item向下交换，向上滑同理。
                    for (int i = fromPosition; i < toPosition; i++) {
                        Collections.swap(noteList, i, i + 1);
                    }
                } else {
                    for (int i = fromPosition; i > toPosition; i--) {
                        Collections.swap(noteList, i, i - 1);
                    }
                }
                //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                //notifyItemMoved只是告诉RecyclerView它的Item换了位置，也就是说如果没有它那么你只能拖动
                //某一项而不能让它与其他项互换位置但是notifyItemMoved仅仅是互换了界面上的项，你必须还要
                //通知RecyclerView它的内容也改变了，这就要用notifyItemRangeChanged
                //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                noteRecyclerViewAdapter.notifyItemMoved(fromPosition, toPosition);
                noteRecyclerViewAdapter.notifyItemRangeChanged(Math.min(fromPosition, toPosition),
                        Math.abs(fromPosition - toPosition) +1);

                return true;
            }
            //滑动删除
            @Override
            public void onSwiped(int position) {
                LitePal.delete(Note.class, getNoteId(position));
                noteList.remove(position);
                noteRecyclerViewAdapter.notifyDataSetChanged();
            }
        });
        touchHelper.setSort(true);//打开拖动排序
        touchHelper.setDelete(true);//打开滑动删除
        new ItemTouchHelper(touchHelper).attachToRecyclerView(noteRecyclerView);

        weatherButton = findViewById(R.id.weather_button);
        weatherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,WeatherActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CODE && resultCode == RESULT_FIRST_USER) {
            changeType = data.getIntExtra("change_type", -1);
            positionId = data.getIntExtra("positionId", -1);
        }
    }

    private void refreshList(){
        Note note;
        switch (changeType) {
            case CHANGE_TYPE_CANCEL:
                break;
            case CHANGE_TYPE_CREATE:
                note = LitePal.findLast(Note.class);
                noteList.add(note);
                break;
            case CHANGE_TYPE_MODIFY:
                note = LitePal.find(Note.class, getNoteId(positionId));
                noteList.remove(positionId);
                noteList.add(positionId, note);
                break;
            default:

                List<Note> newNoteList = LitePal.findAll(Note.class);
                for(Note noteTemp : newNoteList){
                    noteList.add(noteTemp);
                }
                break;
        }
    }

    //根据noteList中的index值(即RecyclerView中的行值)获取数据库真实id
    private int getNoteId(int position){
        return noteList.get(position).getId();
    }

}



















