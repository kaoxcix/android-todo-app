package com.arms_asia.todo.helper;

import android.view.MotionEvent;
import android.view.View;

public interface ItemClickListener {
    void onClick (View view, int position, boolean isLongClick, MotionEvent motionEvent);
}