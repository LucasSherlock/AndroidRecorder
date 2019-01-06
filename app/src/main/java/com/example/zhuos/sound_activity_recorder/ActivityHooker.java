package com.example.zhuos.sound_activity_recorder;

import android.app.Activity;
import android.app.AndroidAppHelper;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.ContextThemeWrapper;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static android.support.v4.content.ContextCompat.startActivity;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class ActivityHooker implements IXposedHookLoadPackage {

    Activity mCurrentActivity;


    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        Class<?> instrumentation = XposedHelpers.findClass(
                "android.app.Instrumentation", lpparam.classLoader);

        XposedBridge.hookAllMethods(instrumentation, "callActivityOnResume", new XC_MethodHook() {

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                mCurrentActivity = (Activity) param.args[0];



                //Log.v(TAG, "Current Activity : " + mCurrentActivity.getClass().getName());
                Log.d("testing:", "Current Activity : " + mCurrentActivity.getClass().getName());
                String currentName = mCurrentActivity.getClass().getName();

                Intent intent = new Intent();
                intent.setAction("com.example.zhuos.sound_activity_recorder.ACTIVITY");
                intent.putExtra("currentActivity",currentName);

                Context context = (Context) AndroidAppHelper.currentApplication();
                context.sendBroadcast(intent);
            }
        });


    }

}
