package com.example.zhuos.sound_activity_recorder;

import android.app.Activity;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.util.HashMap;
import java.util.Timer;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class TouchHooker implements IXposedHookLoadPackage {

    //private TouchEventHandler mTouchHandler = new TouchEventHanlder();
    private final GestureListener gestureListener = new GestureListener();
    private GestureDetectorCompat mDetector;
    private HashMap<Integer, FingerTouch> touchTimeMap;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        //mDetector = new GestureDetectorCompat(lpparam,this);
        touchTimeMap = new HashMap<>();
        //findAndHookMethod(Activity.class, "onTouchEvent", MotionEvent.class, new ActivityTouchEvent());
        findAndHookMethod(View.class, "dispatchTouchEvent", MotionEvent.class, new ViewTouchEvent(lpparam.packageName));



    }

//
//    private class ActivityTouchEvent extends XC_MethodHook {
//
//        @Override
//        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//            super.beforeHookedMethod(param);
//            Activity activity = (Activity) param.thisObject;
//
//            View view = activity.findViewById(android.R.id.content);
//            MotionEvent event = (MotionEvent) param.args[0];
//            mDetector = new GestureDetectorCompat(activity, gestureListener);
//            //mDetector.onTouchEvent(event);
//            //Log.d("testings:", "activity event " + event.getX() + "  " + +event.getY());
//            //  Log.e("shang", "activityTouchEvent: " + event);
//            //mTouchHandler.hookTouchEvent(view, event, mFilters, false);
//
//        }
//
//
//    }
//

    private class ViewTouchEvent extends XC_MethodHook {

        private final String packageName;
        Class viewRootImplClass;
        private GestureDetector mDetector;


        public ViewTouchEvent(String packageName) {
            this.packageName = packageName;
            try {
                viewRootImplClass = this.getClass().getClassLoader().loadClass("android.view.ViewRootImpl");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }


        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            super.afterHookedMethod(param);
            View view = (View) param.thisObject;
            MotionEvent event = (MotionEvent) param.args[0];
            // mDetector = new GestureDetector(view.getContext(), gestureListener);


            if ((Boolean) param.getResult() || view.getParent() == null || (viewRootImplClass.isInstance(view.getParent()))) {
               // Log.d("testings:", "touch event " + event.getRawX() + "  " + event.getRawY() + " action: " + actionToString(event.getActionMasked()) + event.getActionIndex());
                // mDetector.onTouchEvent(event);
                //motionEventToString(event);
                Log.d("testings:",motionEventToString(event));
            }
        }
    }


    public String motionEventToString(MotionEvent event) {
        int fingerID = event.getActionIndex();
        long currentTime = System.currentTimeMillis();
        FingerTouch previous;

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:

                    FingerTouch touch = new FingerTouch(fingerID, currentTime, event.getRawX(), event.getRawY(),MotionEvent.ACTION_DOWN);

                if (touch != null) {
                    touchTimeMap.put(fingerID, touch);
                }

                break;

            case MotionEvent.ACTION_MOVE:
                previous = touchTimeMap.get(fingerID);
                //if (currentTime - previous.getTime() > 100) {
                    if ((event.getRawX() - previous.getX() > 10) ||
                            (event.getRawX() - previous.getX() < -10) ||
                            (event.getRawY() - previous.getY() > 10) ||
                            (event.getRawY() - previous.getY() < -10)) {
                        FingerTouch touch1 = new FingerTouch(fingerID,currentTime,event.getRawX(),event.getRawY(),MotionEvent.ACTION_MOVE);
                        touchTimeMap.put(fingerID,touch1);
                        Log.d("testings:", "touch event " + event.getRawX() + "  " + event.getRawY() + " action: move " + event.getActionIndex());

                    }
               // }

                if (event.getPointerCount()>1){
                    Log.d("testings:", "touch event " + event.getX(1) + "  " + event.getY(1) + " action: pointer " + event.getActionIndex());

                }
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                touchTimeMap.remove(fingerID);
                return "Pointer Down" + fingerID;

            case MotionEvent.ACTION_POINTER_UP:
                FingerTouch touch1 = new FingerTouch(fingerID,currentTime,event.getX(fingerID),event.getY(fingerID),MotionEvent.ACTION_MOVE);
                touchTimeMap.put(fingerID,touch1);
                return "Pointer Up" + fingerID;
            case MotionEvent.ACTION_OUTSIDE:
                return "Outside";
            case MotionEvent.ACTION_UP:
                previous = touchTimeMap.get(fingerID);
                if (previous.getType() == MotionEvent.ACTION_MOVE){
                    Log.d("testings:", "touch event " + event.getRawX() + "  " + event.getRawY() + " action: move end " + event.getActionIndex());
                } else {
                    if (currentTime - touchTimeMap.get(fingerID).getTime() < 500) {
                        Log.d("testings:", "touch event " + event.getRawX() + "  " + event.getRawY() + " action: tap " + event.getActionIndex());
                    } else {
                        Log.d("testings:", "touch event " + event.getRawX() + "  " + event.getRawY() + " action: long press " + event.getActionIndex());

                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                return "Cancel";

        }
        return "";
    }


    private class FingerTouch {
        private int ID, type;
        private long time;
        private float x, y;

        public FingerTouch(int ID, long time, float x, float y,int type) {
            this.ID = ID;
            this.time = time;
            this.x = x;
            this.y = y;
            this.type = type;
        }

        public int getID() {
            return ID;
        }

        public long getTime() {
            return time;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }

        public int getType() {
            return type;
        }
    }


}


