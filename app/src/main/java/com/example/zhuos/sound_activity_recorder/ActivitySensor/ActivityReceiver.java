package com.example.zhuos.sound_activity_recorder.ActivitySensor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.zhuos.sound_activity_recorder.SensorService;

public class ActivityReceiver extends BroadcastReceiver {

    String currentActivity;
    SensorService sensorService;

    public ActivityReceiver() {
        currentActivity = "";
        //this.sensorService = service;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        currentActivity = intent.getStringExtra("currentActivity");
        if (sensorService != null) {
            sensorService.setCurrentActivity(currentActivity);
        }
        //Log.d("testing:","from activity sensor:  "+currentActivity);
    }

    public void setService(SensorService service){
        this.sensorService = service;
    }

}
