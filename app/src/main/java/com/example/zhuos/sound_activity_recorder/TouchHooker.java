package com.example.zhuos.sound_activity_recorder;

import android.app.Activity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class TouchHooker implements IXposedHookLoadPackage {

    //private TouchEventHandler mTouchHandler = new TouchEventHanlder();

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        findAndHookMethod(Activity.class, "onTouchEvent", MotionEvent.class, new ActivityTouchEvent());
        findAndHookMethod(View.class, "dispatchTouchEvent", MotionEvent.class, new ViewTouchEvent(lpparam.packageName));

    }


    private class ActivityTouchEvent extends XC_MethodHook {

        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            super.beforeHookedMethod(param);
            Activity activity = (Activity) param.thisObject;
            View view = activity.findViewById(android.R.id.content);
            MotionEvent event = (MotionEvent) param.args[0];
            Log.d("testings:","activity event " + event.getX()+"  "+ + event.getY());
            //  Log.e("shang", "activityTouchEvent: " + event);
            //mTouchHandler.hookTouchEvent(view, event, mFilters, false);
        }
    }


    private class ViewTouchEvent extends XC_MethodHook {

        private final String packageName;
        Class viewRootImplClass;
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

            if ((Boolean) param.getResult() || view.getParent()==null || (viewRootImplClass.isInstance(view.getParent()) )) {
                Log.d("testings:","touch event "+event.getRawX() +"  "+ event.getRawY() +" action: "+actionToString(event.getActionMasked()) + event.getActionIndex());


            }
        }
    }

    public static String actionToString(int action) {
        switch (action) {

            case MotionEvent.ACTION_DOWN: return "Down";
            case MotionEvent.ACTION_MOVE: return "Move";
            case MotionEvent.ACTION_POINTER_DOWN: return "Pointer Down";
            case MotionEvent.ACTION_UP: return "Up";
            case MotionEvent.ACTION_POINTER_UP: return "Pointer Up";
            case MotionEvent.ACTION_OUTSIDE: return "Outside";
            case MotionEvent.ACTION_CANCEL: return "Cancel";
        }
        return "";
    }

}


