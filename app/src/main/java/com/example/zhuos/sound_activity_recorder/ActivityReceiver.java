package com.example.zhuos.sound_activity_recorder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ActivityReceiver extends BroadcastReceiver {

    String currentActivity;
    SensorService sensorService;

    public ActivityReceiver(SensorService service) {
        currentActivity = "";
        this.sensorService = service;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        currentActivity = intent.getStringExtra("currentActivity");
        sensorService.setCurrentActivity(currentActivity);

        //Log.d("testing:","from activity sensor:  "+currentActivity);
    }

}
