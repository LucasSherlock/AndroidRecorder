package com.example.zhuos.sound_activity_recorder;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.Service;
import android.app.usage.NetworkStats;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.SensorManager;
import android.net.TrafficStats;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.example.zhuos.sound_activity_recorder.sensors.Accelerometer;
import com.example.zhuos.sound_activity_recorder.sensors.GravitySensor;
import com.example.zhuos.sound_activity_recorder.sensors.Gyroscope;
import com.example.zhuos.sound_activity_recorder.sensors.SoundMeter;


public class SensorService extends Service {


    private final IBinder mBinder = new LocalBinder();

    private SoundMeter soundMeter;
    private Accelerometer accelerometer;
    private Gyroscope gyroscope;
    private GravitySensor gravitySensor;
    private ActivityReceiver activityReceiver;
    private GestureReceiver gestureReceiver;


    private UUID uuid = UUID.fromString("6bfc8497-b445-406e-b639-a5abaf4d9739");
    BluetoothSocket socket = null;
    OutputStream outputStream;
    private boolean isConnected;


    private WindowManager mWindowManager;
    private LinearLayout touchLayout;


    private int sound;
    private float accX, accY, accZ, rotX, rotY, rotZ, graX, graY, graZ;
    private String currentActivity;
    private long totalRX, totalTX, rxBytes, txBytes;
    private String screenStatus;


    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    public class LocalBinder extends Binder {
        SensorService getService() {
            return SensorService.this;
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate() {
        super.onCreate();
        Toast.makeText(getBaseContext(), "onCreate", Toast.LENGTH_LONG).show();


        isConnected = false;

        Log.d("testing:", "service created");

//
//        touchLayout = new LinearLayout(this);
//        LayoutParams lp = new LayoutParams(1, LayoutParams.MATCH_PARENT);
//        touchLayout.setLayoutParams(lp);
//        touchLayout.setOnTouchListener(this);
//        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
//        WindowManager.LayoutParams mParams = new WindowManager.LayoutParams(
//                1, /* width */
//                1, /* height */
//                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
//                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
//                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
//                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
//                PixelFormat.TRANSPARENT);
//        mParams.gravity = Gravity.LEFT | Gravity.TOP;
//        mWindowManager.addView(touchLayout, mParams);


        sound = 0;
        accX = accY = accZ = 0;
        currentActivity = "com.example.zhuos.sound_activity_recorder.MainActivity";


        soundMeter = new SoundMeter();
        soundMeter.start();

        accelerometer = new Accelerometer(this);
        accelerometer.mSensorManager.registerListener(accelerometer, accelerometer.mSensor, SensorManager.SENSOR_DELAY_NORMAL);

        gyroscope = new Gyroscope(this);
        gyroscope.mSensorManager.registerListener(gyroscope, gyroscope.mSensor, SensorManager.SENSOR_DELAY_NORMAL);

        gravitySensor = new GravitySensor(this);
        gravitySensor.mSensorManager.registerListener(gravitySensor, gravitySensor.mSensor, SensorManager.SENSOR_DELAY_NORMAL);


        totalRX = TrafficStats.getTotalRxBytes();
        totalTX = TrafficStats.getTotalTxBytes();


        BroadcastReceiver brActivity = new ActivityReceiver();
        IntentFilter filterA = new IntentFilter();
        filterA.addAction("com.example.zhuos.sound_activity_recorder.ACTIVITY");
        this.registerReceiver(brActivity, filterA);
        activityReceiver = (ActivityReceiver) brActivity;
        activityReceiver.setService(this);

        BroadcastReceiver brGesture = new GestureReceiver();
        IntentFilter filterG = new IntentFilter();
        filterG.addAction("com.example.zhuos.sound_activity_recorder.GESTURE");
        this.registerReceiver(brGesture, filterG);
        gestureReceiver = (GestureReceiver) brGesture;
        gestureReceiver.setService(this);


        timerHandler.postDelayed(timerRunnable, 0);


    }


    @Override
    public void onDestroy() {

        if (mWindowManager != null) {
            if (touchLayout != null) mWindowManager.removeView(touchLayout);
        }

        super.onDestroy();
        Toast.makeText(getBaseContext(), "onDestroy", Toast.LENGTH_LONG).show();
        soundMeter.stop();
        timerHandler.removeCallbacks(timerRunnable);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);
        Log.d("testing:", "service started");
        return START_NOT_STICKY;
    }

    public void connectToBluetooth(BluetoothDevice device) {

        try {
            socket = device.createRfcommSocketToServiceRecord(uuid);
            socket.connect();
            outputStream = socket.getOutputStream();

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (socket.isConnected()) {
            isConnected = true;
            Log.d("testing:", "connected");
        } else {
            Log.d("testing:", "not connected");
        }

    }


    private void networkUsage() {
        rxBytes = TrafficStats.getTotalRxBytes() - totalRX;
        txBytes = TrafficStats.getTotalTxBytes() - totalTX;

        //Log.d("testings:",  "network usage:   " +  rxBytes+ "    " +txBytes);
        totalRX = TrafficStats.getTotalRxBytes();
        totalTX = TrafficStats.getTotalTxBytes();
    }

    private void checkPhoneState() {
        boolean isScreenOn;
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT_WATCH) {
            isScreenOn = pm.isInteractive();
        } else {
            isScreenOn = pm.isScreenOn();
        }
        if (isScreenOn) {
            if (km.inKeyguardRestrictedInputMode()) {
                // it is locked
                screenStatus = "locked";
                //Log.d("testings:", "screen: locked");
            } else {
                //it is not locked
                screenStatus = "unlocked";
                // Log.d("testings:", "screen: not locked");
            }
        } else {
            screenStatus = "off";
            // Log.d("testings:", "screen: off");
        }

    }


    public void outputFile(String content) {

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try {
            byte[] bytes = content.getBytes("UTF-8");
            output.write(bytes);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (socket != null) {
            try {
                outputStream.write(output.toByteArray());
                Log.d("testing:", "writing " + sound);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void run() {
            if(isConnected) {
                String output = outputToString();
                outputFile(output);
            }
            timerHandler.postDelayed(this, 200);
        }
    };

    private String outputToString() {
        sound = (int) Math.round(soundMeter.getAmplitude());
        accX = accelerometer.getX();
        accY = accelerometer.getY();
        accZ = accelerometer.getZ();
        rotX = gyroscope.getX();
        rotY = gyroscope.getY();
        rotZ = gyroscope.getZ();
        graX = gravitySensor.getX();
        graY = gravitySensor.getY();
        graZ = gravitySensor.getZ();
        networkUsage();
        checkPhoneState();

        List<String> outputList = new ArrayList<>();
        outputList.add(Integer.toString(sound));
        outputList.add(Float.toString(accX));
        outputList.add(Float.toString(accY));
        outputList.add(Float.toString(accZ));
        outputList.add(Float.toString(rotX));
        outputList.add(Float.toString(rotY));
        outputList.add(Float.toString(rotZ));
        outputList.add(Float.toString(graX));
        outputList.add(Float.toString(graY));
        outputList.add(Float.toString(graZ));
        outputList.add(currentActivity);
        outputList.add(screenStatus);
        outputList.add(Long.toString(rxBytes));
        outputList.add(Long.toString(txBytes));


        String send = android.text.TextUtils.join(",", outputList);
        return send;
    }

    //=================== Getters ====================//

    public int getSound() {
        return sound;
    }

    public float getAccX() {
        return accX;
    }

    public float getAccY() {
        return accY;
    }

    public float getAccZ() {
        return accZ;
    }

    public float getRotX() {
        return rotX;
    }

    public float getRotY() {
        return rotY;
    }

    public float getRotZ() {
        return rotZ;
    }

    public float getGraX() {
        return graX;
    }

    public float getGraY() {
        return graY;
    }

    public float getGraZ() {
        return graZ;
    }

    public void setCurrentActivity(String currentActivity) {
        this.currentActivity = currentActivity;
    }
}
