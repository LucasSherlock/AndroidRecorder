package com.example.zhuos.sound_activity_recorder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ActivitySensor extends BroadcastReceiver {

    String currentActivity;

    public ActivitySensor(){
        currentActivity = "";
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("testing:",intent.getAction());
         currentActivity = intent.getStringExtra("currentActivity");
        String temp = intent.getStringExtra("currentActivity");
        Log.d("testing:","from activity sensor:  "+currentActivity);
        //Log.d("testing:","from activity sensor xxx:  "+temp);
    }

    public String getCurrentActivity() {
        //Log.d("testing:","from activity sensor:  "+currentActivity);
        return currentActivity;
    }
}
