package com.example.zhuos.sound_activity_recorder.GestureSensor;

import android.app.AndroidAppHelper;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static com.example.zhuos.sound_activity_recorder.GestureSensor.GestureType.*;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class GestureHooker implements IXposedHookLoadPackage {

    //private TouchEventHandler mTouchHandler = new TouchEventHanlder();
//    private GestureDetectorCompat mDetector;
    private Gesture gesture;
    private final int REFRESH_TIME = 100;
    private final int MOVE_MIN_THRESHOLD = 80;
    private final int MOVE_HMAX_THRESHOLD = 330;
    private final int MOVE_VMAX_THRESHOLD = 230;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        //mDetector = new GestureDetectorCompat(lpparam,this);
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
                Log.d("testings:", motionEventToString(event));
            }
        }
    }


    public String motionEventToString(MotionEvent event) {
        long currentTime = System.currentTimeMillis();

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:

                gesture = new Gesture(currentTime, currentTime, event.getRawX(), event.getRawY(), event.getRawX(), event.getRawY(), Start);

                break;

            case MotionEvent.ACTION_MOVE:

                if (currentTime - gesture.getCurrentTime() > REFRESH_TIME) {
                    if ((event.getRawX() - gesture.getCurrentX() > 10) ||
                            (event.getRawX() - gesture.getCurrentX() < -10) ||
                            (event.getRawY() - gesture.getCurrentY() > 10) ||
                            (event.getRawY() - gesture.getCurrentY() < -10)) {
                        switch (gesture.getType()) {
                            case Start:
                                if (gesture.getStartX() - event.getRawX() > MOVE_MIN_THRESHOLD) {//swipe left
                                    gesture.setType(MoveLeft);
                                } else if (gesture.getStartX() - event.getRawX() < -MOVE_MIN_THRESHOLD) {//swipe right
                                    gesture.setType(MoveRight);
                                } else if (gesture.getStartY() - event.getRawY() > MOVE_MIN_THRESHOLD) {//swipe up
                                    gesture.setType(MoveUp);
                                } else if (gesture.getStartY() - event.getRawY() < -MOVE_MIN_THRESHOLD) {//swipe down
                                    gesture.setType(MoveDown);
                                } else {
                                    gesture.setType(Move);
                                }
                                break;

                            case MoveUp:
                                if (gesture.getStartX() - event.getRawX() > MOVE_HMAX_THRESHOLD ||//too much to the left
                                        gesture.getStartX() - event.getRawX() < -MOVE_HMAX_THRESHOLD ||//to the right
                                        gesture.getStartY() - event.getRawY() < -MOVE_MIN_THRESHOLD) {//moving down
                                    gesture.setType(Move);
                                }
                                break;

                            case MoveDown:
                                if (gesture.getStartX() - event.getRawX() > MOVE_HMAX_THRESHOLD ||//too much to the left
                                        gesture.getStartX() - event.getRawX() < -MOVE_HMAX_THRESHOLD ||//to the right
                                        gesture.getStartY() - event.getRawY() > MOVE_MIN_THRESHOLD) {//moving up
                                    gesture.setType(Move);
                                }
                                break;

                            case MoveLeft:
                                if (gesture.getStartY() - event.getRawY() > MOVE_VMAX_THRESHOLD ||//too much up
                                        gesture.getStartY() - event.getRawY() < -MOVE_VMAX_THRESHOLD ||//down
                                        gesture.getStartX() - event.getRawX() < -MOVE_MIN_THRESHOLD) {//right
                                    gesture.setType(Move);
                                }
                                break;

                            case MoveRight:
                                if (gesture.getStartY() - event.getRawY() > MOVE_VMAX_THRESHOLD ||//too much up
                                        gesture.getStartY() - event.getRawY() < -MOVE_VMAX_THRESHOLD ||//down
                                        gesture.getStartX() - event.getRawX() > MOVE_MIN_THRESHOLD) {//left
                                    gesture.setType(Move);
                                }
                                break;

                        }

                    }

              //      Log.d("testings:", "touch event " + event.getRawX() + "  " + event.getRawY() + " action: " + gesture.getType());
                    gesture.setCurrentTime(currentTime);
                    gesture.setCurrentX(event.getRawX());
                    gesture.setCurrentY(event.getRawY());
                }

             //  Log.d("testings:", "touch event3 " + event.getRawX() + "  " + event.getRawY() + " action: " + gesture.getType());
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                if (event.getPointerCount() == 2) {
                    gesture.setType(TwoFingers);
                } else if (event.getPointerCount() > 2) {
                    gesture.setType(MultiFingers);
                }
                gesture.setCurrentTime(currentTime);
                gesture.setCurrentX(event.getRawX());
                gesture.setCurrentY(event.getRawY());
              //  Log.d("testings:", "touch event " + event.getRawX() + "  " + event.getRawY() + " action: " + gesture.getType());

                return "";

            case MotionEvent.ACTION_POINTER_UP:
                gesture.setCurrentTime(currentTime);
                gesture.setCurrentX(event.getRawX());
                gesture.setCurrentY(event.getRawY());
                //Log.d("testings:", "touch event " + event.getRawX() + "  " + event.getRawY() + " action: " + gesture.getType());
                return "";
            case MotionEvent.ACTION_OUTSIDE:
                return "Outside";
            case MotionEvent.ACTION_UP:

                if (gesture.getType() == Start) {
                    if (currentTime - gesture.getStartTime() > 500) {
                        gesture.setType(LongPress);
                    } else {
                        gesture.setType(Tap);
                    }
                }

                gesture.setCurrentTime(currentTime);
                gesture.setCurrentX(event.getRawX());
                gesture.setCurrentY(event.getRawY());
                //Log.d("testings:", "touch event " + event.getRawX() + "  " + event.getRawY() + " action: " + gesture.getType());

                sendIntent(gesture);
                break;
            case MotionEvent.ACTION_CANCEL:
                return "Cancel";

        }

        return "";
    }

    private void sendIntent(Gesture gesture) {
        Intent intent = new Intent();
        intent.setAction("com.example.zhuos.sound_activity_recorder.GESTURE");
        intent.putExtra("gesture", gesture);

        Context context = (Context) AndroidAppHelper.currentApplication();
        context.sendBroadcast(intent);
    }


}


