package com.example.zhuos.sound_activity_recorder.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.hardware.SensorEventListener;

import com.example.zhuos.sound_activity_recorder.HelperTool;

public class Accelerometer implements SensorEventListener {

    public SensorManager mSensorManager;
    public Sensor mSensor;
    private long lastUpdate;
    private float x, y, z;


    public Accelerometer(Context context) {
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor mySensor = event.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {


            long curTime = System.currentTimeMillis();

            if ((curTime - lastUpdate) > 100) {

                float accx = event.values[0];
                float accy = event.values[1];
                float accz = event.values[2];

                x = HelperTool.round(accx, 2);
                y = HelperTool.round(accy, 2);
                z = HelperTool.round(accz, 2);


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
