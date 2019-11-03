package com.example.sharedpreference.Util;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sharedpreference.Database.Note;
import com.example.sharedpreference.R;

import java.util.List;

public class NoteRecyclerViewAdapter extends RecyclerView.Adapter {

    public static class NoteViewHolder extends RecyclerView.ViewHolder{
        private final TextView title;
        private final TextView createTime;
        public NoteViewHolder(View v) {
            super(v);
            title = v.findViewById(R.id.note_textview);
            createTime = v.findViewById(R.id.note_create_time_textview);
        }
    }

    private List<Note> noteList;
    public NoteRecyclerViewAdapter(List<Note> noteList) {
        this.noteList = noteList;
    }

    private OnItemClickListener myClickListener;
    public void setOnItemClickListener(OnItemClickListener myClickListener) {
        this.myClickListener = myClickListener;
    }


    @Override
    public NoteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_item_layout, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        NoteViewHolder noteViewHolder = (NoteViewHolder)holder;
        noteViewHolder.title.setText(noteList.get(position).getTitle());
        noteViewHolder.createTime.setText(noteList.get(position).getCreateTime());

        noteViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (myClickListener != null) {
                    myClickListener.onClick(position);
                }
            }
        });
    }



    @Override
    public int getItemCount() {
        return noteList.size();
    }
}
