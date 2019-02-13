# AndroidRecorder

## What is a AndroidRecorder ?
AndroidRecorder is an Android application that would record data from the smart phone sensors, and display them on the screen,
and constantly sending data to the PC application.

The link to the PC application: https://github.com/SijieZhuo/SensorDataViewer

AndroidRecorder was developed by part 4 software student for the research project conducted at the University of Auckland.

**This App is only working on Rooted phone**

**This App has only been tested on Rooted Samsung Galaxy S7**

## Development Setup
The phone has to be rooted, each different phone model may be rooted different way.

Xposed Framework need to be installed to the phone, the link of the app can be found:
https://www.apkmirror.com/apk/rovo89/xposed-installer/xposed-installer-3-1-5-release/xposed-installer-3-1-5-android-apk-download/

## Software Installation
Since this app is only working on rooted phone, the app is not published on google play, so to install the app,
the phone needs to connect to PC, and using Android Studio is prefered.

1. Connect the phone to PC (assume phone is rooted)
2. Build the application and run it

## Functionalities
The main functionality of this app is to gather different type of data from the phone, and transfer the data to the PC.

The data collected from the app would include:

- Sound (include loudness/decibels, and sound frequency)
- Accelerometer
- Gyroscope
- Megnetometer
- Network status (downloading and uploading in bytes)
- Screen status (screen is on/off/lockscreen)
- Current Foreground App (requires rooted phone)
- User touch event (gesture start time, x, y coordinate, end time, x, y coordinate, and gesture type) (requires rooted phone)

The app would refresh every 0.2 seconds, and the touch event would update once an event is completed.
The sound part is referenced from https://github.com/lucns/Android-Audio-Sample

The app is connect to the PC via Bluetooth, the app would constantly send the data to PC every 0.2 second (exclude the touch event),
and the touch event is send to the PC separately (once the event is completed, it is sended to the PC directly).

### Xposed Framework
Xposed Framework is used in the app, it is used to hook the system method so that this app can modify on system level method and 
retrieve system level data.

In this app, the hooker class would gather the data, then create and fire an Intent to the corresponding BroadcastReceiver,
so that the data can be used for the app.
