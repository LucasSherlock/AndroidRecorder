package com.example.zhuos.sound_activity_recorder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class GestureReceiver extends BroadcastReceiver {

    Gesture currentGesture;

    @Override
    public void onReceive(Context context, Intent intent) {
        currentGesture = (Gesture) intent.getSerializableExtra("gesture");
    }

    public Gesture getCurrentGesture() {
        return currentGesture;
    }
}
