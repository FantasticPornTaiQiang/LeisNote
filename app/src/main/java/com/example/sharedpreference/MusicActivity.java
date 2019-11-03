package com.example.sharedpreference;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.sharedpreference.Music.Music;
import com.example.sharedpreference.Music.MusicAdapter;
import com.example.sharedpreference.Music.MusicFilter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MusicActivity extends AppCompatActivity {

    List<Music> musicList = new ArrayList<>();   //音乐列表
    File[] songFiles;
    TextView musicTitleTextView;
    TextView musicBackToEditButton;
    ListView musicListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);

        musicTitleTextView = findViewById(R.id.music_title_text_view);
        musicBackToEditButton = findViewById(R.id.music_back_to_edit_button);
        musicBackToEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("music_name", "");
                intent.putExtra("music_url", "");
                setResult(2, intent);
                MusicActivity.this.finish();
            }
        });

        File sdpath = Environment.getExternalStorageDirectory(); //获得手机SD卡路径
        //getFileName(sdpath.listFiles());

        if(musicList != null && musicList.size() > 0){
            MusicAdapter musicAdapter = new MusicAdapter(MusicActivity.this, R.layout.music_item_layout, musicList);
            musicListView = findViewById(R.id.choose_music_list);
            musicListView.setAdapter(musicAdapter);
            musicListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    Music music = musicList.get(position);
                    Intent intent = new Intent();
                    intent.putExtra("music_name", music.getName());
                    intent.putExtra("music_url", music.getUrl());
                    setResult(2, intent);
                    MusicActivity.this.finish();
                }
            });
        }
    }

//    public void getSDcardMusicFile(File groupPath){
//        //循环获取sdcard目录下面的目录和文件
//        for(int i=0; i< groupPath.listFiles().length; i++){
//            File childFile = groupPath.listFiles()[i];
//
//            //假如是目录的话就继续调用getSDcardFile（）将childFile作为参数传递的方法里面
//            if(childFile.isDirectory()){
//                getSDcardMusicFile(childFile);
//            }else{
//                //如果是文件的话，判断是不是以.mp3结尾，是就加入到List里面
//                if(childFile.toString().endsWith(".mp3")){
//                    Music music = new Music();
//                    music.setName(childFile.getName());
//                    music.setUrl(childFile.getAbsolutePath());//获取文件的绝对路径
//                    musicList.add(music);
//                }
//            }
//        }
//    }


//    private void getFileName(File[] files) {
//        if (files != null)// 先判断目录是否为空，否则会报空指针
//        {
//            for (File file : files) {
//                if (file.isDirectory()) {
//                    getFileName(file.listFiles());
//                } else {
//                    String fileName = file.getName();
//                    if (fileName.endsWith(".mp3")) {
//                        HashMap map = new HashMap();
//                        String s = fileName.substring(0,
//                                fileName.lastIndexOf("."));
//                        map.put("Name", s);
//                        name.add(map);
//                    }
//                }
//            }
//        }
//    }

}
