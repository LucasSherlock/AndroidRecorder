package com.example.zhuos.sound_activity_recorder;

import android.app.Activity;
import android.util.Log;
import android.view.ContextThemeWrapper;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class ActivitySensor implements IXposedHookLoadPackage {

    Activity mCurrentActivity;
    String currentName;

    public ActivitySensor(){
        currentName = "";
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        Class<?> instrumentation = XposedHelpers.findClass(
                "android.app.Instrumentation", lpparam.classLoader);

        XposedBridge.hookAllMethods(instrumentation, "newActivity", new XC_MethodHook() {

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                mCurrentActivity = (Activity) param.getResult();

                //Log.v(TAG, "Current Activity : " + mCurrentActivity.getClass().getName());
                Log.d("testing:", "Current Activity : " + mCurrentActivity.getClass().getName());
                currentName = mCurrentActivity.getClass().getName();
            }
        });


//        XposedBridge.log("Loaded app: " + lpparam.packageName);
//        Log.d("testing:", "Loaded app: " + lpparam.packageName);

//        if (!lpparam.packageName.equals("android.app")) {
//            return;
//        }
//        findAndHookMethod("android.app.Activity", lpparam.classLoader, "onResume", new XC_MethodHook() {
//            @Override
//            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                // this will be called before the clock was updated by the original method
//                ContextThemeWrapper context = (ContextThemeWrapper)param.thisObject;
//                String name = context.getPackageName();
//                XposedBridge.log("package name is : " + name);
//                Log.d("testing:","package name is :" + name);
//            }


//        });
    }

    public String getCurrentName() {
        return currentName;
    }
}
