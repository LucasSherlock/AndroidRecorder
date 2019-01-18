package com.example.zhuos.sound_activity_recorder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class GestureReceiver extends BroadcastReceiver {

    Gesture currentGesture;
    SensorService sensorService;

    public GestureReceiver(){
        //this.sensorService = service;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        currentGesture = (Gesture) intent.getSerializableExtra("gesture");
        Log.d("testings:", currentGesture.toString());
        if (sensorService != null) {
            sensorService.outputFile(currentGesture.toString());
        }
    }

    public Gesture getCurrentGesture() {
        return currentGesture;
    }

    public void setService(SensorService service){
        this.sensorService = service;
    }
}
