package com.example.sharedpreference.Music;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.sharedpreference.R;

import java.util.List;

public class MusicAdapter extends ArrayAdapter<Music> {

    int resourceId;

    public MusicAdapter(Context context, int resource, List<Music> objects) {
        super(context, resource, objects);
        resourceId = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Music music = getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
        TextView musicNameTextView = view.findViewById(R.id.music_item_text_view);
        musicNameTextView.setText(music.getName());
        return view;
    }
}
