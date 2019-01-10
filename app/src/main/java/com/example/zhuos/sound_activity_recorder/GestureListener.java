package com.example.zhuos.sound_activity_recorder;

import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;

public class GestureListener extends SimpleOnGestureListener {

    @Override
    public boolean onDown(MotionEvent e) {
        Log.d("testings:","onDown called");
        return true;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        Log.d("testings:","onSingleTapUp called");
        //return super.onSingleTapUp(e);
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        Log.d("testings:","onLongPress called");
        super.onLongPress(e);
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        Log.d("testings:","onScroll called");
        //return super.onScroll(e1, e2, distanceX, distanceY);
        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        Log.d("testings:","onFling called");
        //return super.onFling(e1, e2, velocityX, velocityY);
        return true;
    }


}
