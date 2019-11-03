package com.example.sharedpreference.Util;

public interface OnItemTouchListener {
    boolean onMove(int fromPosition,int toPosition);
    void onSwiped(int position);
}
