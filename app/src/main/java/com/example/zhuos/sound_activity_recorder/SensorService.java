package com.example.zhuos.sound_activity_recorder;

import android.app.ActivityManager;
import android.app.AndroidAppHelper;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;

import android.widget.LinearLayout.LayoutParams;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class SensorService extends Service implements OnTouchListener {


    private final IBinder mBinder = new LocalBinder();

    private SoundMeter soundMeter;
    private Accelerometer accelerometer;
    private Gyroscope gyroscope;
    private GravitySensor gravitySensor;


    private ActivitySensor activitySensor;

    private UUID uuid = UUID.fromString("6bfc8497-b445-406e-b639-a5abaf4d9739");
    BluetoothSocket socket = null;
    OutputStream outputStream;

    ActivityManager am;

    private String TAG = this.getClass().getSimpleName();
    private WindowManager mWindowManager;
    private LinearLayout touchLayout;


    private int sound;
    private float accX, accY, accZ,rotX,rotY,rotZ,graX,graY,graZ;

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.d("testing:", event.getX() + " " + event.getY());
        return false;
    }


    public class LocalBinder extends Binder {
        SensorService getService() {
            return SensorService.this;
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Toast.makeText(getBaseContext(), "onCreate", Toast.LENGTH_LONG).show();


        am = (ActivityManager) SensorService.this.getSystemService(ACTIVITY_SERVICE);


        Log.d("testing:", "service created");


        touchLayout = new LinearLayout(this);
        LayoutParams lp = new LayoutParams(1, LayoutParams.MATCH_PARENT);
        touchLayout.setLayoutParams(lp);
        touchLayout.setOnTouchListener(this);
        Log.d("testing:", "service created2");
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        WindowManager.LayoutParams mParams = new WindowManager.LayoutParams(
                1, /* width */
                1, /* height */
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSPARENT);
        mParams.gravity = Gravity.LEFT | Gravity.TOP;
        Log.d("testing:", "service created3");
        mWindowManager.addView(touchLayout, mParams);
        Log.d("testing:", "service created4");



        sound = 0;
        accX = accY = accZ = 0;


        soundMeter = new SoundMeter();
        soundMeter.start();

        accelerometer = new Accelerometer(this);
        accelerometer.mSensorManager.registerListener(accelerometer, accelerometer.mSensor, SensorManager.SENSOR_DELAY_NORMAL);

        gyroscope = new Gyroscope(this);
        gyroscope.mSensorManager.registerListener(gyroscope, gyroscope.mSensor, SensorManager.SENSOR_DELAY_NORMAL);

        gravitySensor = new GravitySensor(this);
        gravitySensor.mSensorManager.registerListener(gravitySensor, gravitySensor.mSensor, SensorManager.SENSOR_DELAY_NORMAL);

        //activitySensor = new ActivitySensor();


        timerHandler.postDelayed(timerRunnable, 0);


    }


    @Override
    public void onDestroy() {

        if(mWindowManager != null) {
            if(touchLayout != null) mWindowManager.removeView(touchLayout);
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
            Log.d("testing:", "connected");
        } else {
            Log.d("testing:", "not connected");
        }

    }

    private void checkCurrent() {
        ActivityManager.RunningTaskInfo foregroundTaskInfo = am.getRunningTasks(1).get(0);
        String foregroundTaskPackageName = foregroundTaskInfo.topActivity.getPackageName();
        if (foregroundTaskPackageName.equals("com.sec.android.app.launcher")){
            Log.d("testing:", "package name xxx: "+foregroundTaskPackageName);
        } else{

            String s  = XposedHelpers.findField(ActivitySensor.class,"currentName").toString();

            Log.d("testing:", "package name xxx: "+ s);
        }


//        String packageName =  ProcessManager.getRunningForegroundApps(getApplicationContext()).get(0).getPackageName();
//        Log.d("testing:","current app: " + currentApp);




    }

    private void outputFile(String content) {

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

        @Override
        public void run() {

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

            Log.d("output:", "getting data " + sound);

            String send = sound + "," +
                    String.format("%.2f", accelerometer.getX()) + "," +
                    String.format("%.2f", accelerometer.getY()) + "." +
                    String.format("%.2f", accelerometer.getZ());

            outputFile(send);
            checkCurrent();



            timerHandler.postDelayed(this, 200);
        }
    };

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
}
