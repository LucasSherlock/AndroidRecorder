package com.example.zhuos.sound_activity_recorder.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.example.zhuos.sound_activity_recorder.HelperTool;

public class GravitySensor implements SensorEventListener {

    public SensorManager mSensorManager;
    public Sensor mSensor;

    private long lastUpdate;
    private float x, y, z;

    public GravitySensor(Context context){
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor mySensor = event.sensor;

        if (mySensor.getType() == Sensor.TYPE_GRAVITY) {


            long curTime = System.currentTimeMillis();

            if ((curTime - lastUpdate) > 100) {

                float rotx = event.values[0];
                float roty = event.values[1];
                float rotz = event.values[2];
                x = HelperTool.round(rotx,1);
                y = HelperTool.round(roty,1);
                z = HelperTool.round(rotz,1);

                lastUpdate = curTime;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }
}
