package com.example.sharedpreference.Music;

import java.io.File;
import java.io.FilenameFilter;

public class MusicFilter implements FilenameFilter {
    private String type;

    public MusicFilter(String type) {
        this.type = type;
    }

    @Override
    public boolean accept(File dir, String name) {
        return name.endsWith(type);
    }
}