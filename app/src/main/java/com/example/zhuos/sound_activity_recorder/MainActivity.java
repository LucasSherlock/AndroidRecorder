package com.example.zhuos.sound_activity_recorder;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements ServiceConnection {


    BluetoothSocket socket = null;
    OutputStream outputStream;

    ActivityManager am;

    Intent serviceIntent;

    private SensorService service;

    //private Accelerometer accelerometer;

    TextView timerTextView, accX, accY, accZ,rotX,rotY,rotZ;
    private UUID uuid = UUID.fromString("6bfc8497-b445-406e-b639-a5abaf4d9739");


    //runs without a timer by reposting this handler at the end of the runnable
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {

            Log.d("testing:", "checking");
            if (service != null) {
                Log.d("testing:", "not null");
                timerTextView.setText(service.getSound() + " db");
                accX.setText("acc X : " + service.getAccX());
                accY.setText("acc Y : " + service.getAccY());
                accZ.setText("acc Z : " + service.getAccZ());
                rotX.setText("rot X : " + service.getRotX());
                rotY.setText("rot Y : " + service.getRotY());
                rotZ.setText("rot Z : " + service.getRotZ());
            } else {
                Log.d("testing:", " null");
            }

            timerHandler.postDelayed(this, 200);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 10);

        }

        am = (ActivityManager) this.getSystemService(Activity.ACTIVITY_SERVICE);


        serviceIntent = new Intent(this, SensorService.class);
        //Intent intent = new Intent(getApplicationContext(), SensorService.class);
        bindService(serviceIntent, this, Context.BIND_AUTO_CREATE);
        startService(serviceIntent);

        //accelerometer = new Accelerometer(this);

        timerTextView = (TextView) findViewById(R.id.timerTextView);
        accX = (TextView) findViewById(R.id.accX);
        accY = (TextView) findViewById(R.id.accY);
        accZ = (TextView) findViewById(R.id.accZ);
        rotX = (TextView) findViewById(R.id.rotX);
        rotY = (TextView) findViewById(R.id.rotY);
        rotZ = (TextView) findViewById(R.id.rotZ);


        Button b = (Button) findViewById(R.id.button);
        b.setText("start");
        b.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Button b = (Button) v;


                if (b.getText().equals("stop")) {
//                    sm.stop();
                    timerHandler.removeCallbacks(timerRunnable);
                    b.setText("start");

                    //unbindService(instance);
                    getApplicationContext().stopService(serviceIntent);


                } else {

                    serviceIntent = new Intent(getApplicationContext(), SensorService.class);

                    getApplicationContext().startService(serviceIntent);


                    timerHandler.postDelayed(timerRunnable, 0);
                    b.setText("stop");
                }
            }
        });


        Button connectBtn = findViewById(R.id.connectBtn);
        connectBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Button b = (Button) v;

                BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                //BluetoothDevice device = adapter.getRemoteDevice("14:ab:c5:7a:b5:12");


                Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();

                List<String> s = new ArrayList<String>();
                for (BluetoothDevice bt : pairedDevices) {
                    s.add(bt.getName());
                    Log.d("testing:", bt.getName());
                    if (bt.getName().equals("DESKTOP-3IKI16L")) {

                        service.connectToBluetooth(bt);

//                        try {
//                            socket = bt.createRfcommSocketToServiceRecord(uuid);
//                            socket.connect();
//                            outputStream = socket.getOutputStream();
//
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//
//                        if (socket.isConnected()) {
//                            Log.d("testing:", "connected");
//                        } else {
//                            Log.d("testing:", "not connected");
//                        }
                    }
                }

            }
        });


        Button activity = (Button) findViewById(R.id.activity);
        activity.setText("activity");
        activity.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d("testing:", "trying");
                Button b = (Button) v;

                new Thread(new Runnable() {
                    public void run() {
                        // a potentially time consuming task
                        Log.d("testing:", "running");
                        Process p;
                        try {
                            // Preform su to get root privledges
                            String line;
                            p = Runtime.getRuntime().exec("su");

                            // Attempt to write a file to a root-only
                            DataOutputStream os = new DataOutputStream(p.getOutputStream());
                            os.writeBytes("dumpsys activity\n");


                            // Close the terminal
                            os.writeBytes("exit\n");
                            os.flush();

                            BufferedReader reader2 = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                            while ((line = reader2.readLine()) != null) {
                                Log.d("testing:", "output(e): " + line);
                            }
                            reader2.close();

                            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                            //boolean a = false;
                            while ((line = reader.readLine()) != null) {
                                Log.d("testing:", "output: " + line);

                            }


                            reader.close();


                            try {
                                p.waitFor();
                                if (p.exitValue() != 255) {

                                    Log.d("testing:", "root");
                                } else {
                                    Log.d("testing:", "not root");
                                }
                            } catch (InterruptedException e) {
                                e.getStackTrace();
                            }
                        } catch (IOException e) {
                            e.getStackTrace();
                        }

                    }
                }).start();

//                    Log.d("testing:", "try process");
//                    int read;
//                    char[] buffer = new char[4096];
//                    StringBuffer output = new StringBuffer();
//                    while ((read = reader.read(buffer)) > 0) {
//                        output.append(buffer, 0, read);
//                    }


                //Log.d("testing:", "output: "+output.toString());


//                List<ActivityManager.RunningAppProcessInfo> procInfos = am
//                        .getRunningAppProcesses();
//
//
//
//                for (int idx = 0; idx < procInfos.size(); idx++) {
//
//                    Log.d("testing:",procInfos.get(idx).processName );
//                }


//                try {
//                    Process p = Runtime.getRuntime().exec(new String[]{"adb","shell","ps","-A"});
//                    BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
//                    Log.d("testing:","try process");
//                    int read;
//                    char[] buffer = new char[4096];
//                    StringBuffer output = new StringBuffer();
//                    while ((read = reader.read(buffer)) > 0) {
//                        output.append(buffer, 0, read);
//                    }
//                    reader.close();
//
//                    p.waitFor();
//
//                    Log.d("testing:",output.toString());
//                    Toast.makeText(getApplicationContext(),output.toString(),Toast.LENGTH_LONG).show();
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
            }
        });


    }


    private void threadwork() {
        Process p;
        try {
            // Preform su to get root privledges
            String line;
            p = Runtime.getRuntime().exec("su");

            // Attempt to write a file to a root-only
            DataOutputStream os = new DataOutputStream(p.getOutputStream());
            os.writeBytes("getevent -lt /dev/input/event1\n");


            // Close the terminal
            os.writeBytes("exit\n");
            os.flush();

            BufferedReader reader2 = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            while ((line = reader2.readLine()) != null) {
                Log.d("testing:", "output(e): " + line);
            }
            reader2.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((line = reader.readLine()) != null) {
                Log.d("testing:", "output: " + line);
            }


            reader.close();


            try {
                p.waitFor();
                if (p.exitValue() != 255) {

                    Log.d("testing:", "root");
                } else {
                    Log.d("testing:", "not root");
                }
            } catch (InterruptedException e) {
                e.getStackTrace();
            }
        } catch (IOException e) {
            e.getStackTrace();
        }


    }

    @Override
    public void onPause() {
        super.onPause();
        timerHandler.removeCallbacks(timerRunnable);
        Button b = (Button) findViewById(R.id.button);
        b.setText("start");

        //accelerometer.mSensorManager.unregisterListener(accelerometer);
        //unbindService(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //accelerometer.mSensorManager.registerListener(accelerometer, accelerometer.mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        //serviceIntent = new Intent(this, SensorService.class);
        //Intent intent = new Intent(getApplicationContext(), SensorService.class);
        //bindService(intent, this, Context.BIND_AUTO_CREATE);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Toast.makeText(getBaseContext(),"onDestroy", Toast.LENGTH_LONG).show();
        getApplicationContext().unbindService(this);

    }


    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        SensorService.LocalBinder b = (SensorService.LocalBinder) binder;
        service = b.getService();
        Log.d("testing:", "service is connected");
        Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.d("testing:", "service is disconnected");
        service = null;
    }
}
