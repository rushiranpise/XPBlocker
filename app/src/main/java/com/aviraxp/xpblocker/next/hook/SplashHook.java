package com.aviraxp.xpblocker.next.hook;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;

import com.aviraxp.xpblocker.next.helper.PreferencesHelper;
import com.aviraxp.xpblocker.next.util.LogUtils;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

class SplashHook {
    private static Pair<String, Long> lastStartActivity;
    private static Pair<String, Long> lastFinishActivity;

    public static void hook(final XC_LoadPackage.LoadPackageParam lpparam) {
        if (!PreferencesHelper.isSplashHookEnabled()) return;

        XposedBridge.hookAllMethods(Activity.class, "onCreate", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(final MethodHookParam param) {
                final String activityName = param.thisObject.getClass().getName();

                lastStartActivity = new Pair<>(activityName, System.currentTimeMillis());
                if (lastFinishActivity != null && System.currentTimeMillis() - lastFinishActivity.second < 500
                    && !HookLoader.splashMap.containsKey(lastFinishActivity.first))
                    LogUtils.toast((Context) param.thisObject, "New splashScreen detected: " + lastFinishActivity.first + "," + activityName);

                if (!HookLoader.splashMap.containsKey(activityName)) return;

                final String nextClassName = HookLoader.splashMap.get(activityName);
                if (nextClassName == null) LogUtils.toast((Context) param.thisObject, "Finish Splash: " + activityName);
                else {
                    final Class<?> next = XposedHelpers.findClassIfExists(nextClassName, lpparam.classLoader);
                    if (next == null) {
                        LogUtils.toast((Context) param.thisObject, "Skip Splash Failed: " + activityName);
                        return;
                    }
                    LogUtils.toast((Context) param.thisObject, "Skip Splash: " + activityName + " => " + nextClassName);
                    final Intent intent = new Intent().setClass((Context) param.thisObject, next).putExtra("isSkipped", true);
                    XposedHelpers.callMethod(param.thisObject, "startActivity", intent);
                }
                XposedHelpers.callMethod(param.thisObject, "finish");
            }
        });
        XposedBridge.hookAllMethods(Activity.class, "finish", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(final MethodHookParam param) {
                final String activityName = param.thisObject.getClass().getName();
                lastFinishActivity = new Pair<>(activityName, System.currentTimeMillis());
                LogUtils.logRecord("finish Activity: " + activityName);
                if (lastStartActivity != null && lastStartActivity.first.equals(activityName) && System.currentTimeMillis() - lastStartActivity.second < 10000 && System.currentTimeMillis() - lastStartActivity.second > 1000) {
                    LogUtils.logRecord("fastCloseActivity: " + activityName);
                }
                lastStartActivity = new Pair<>(activityName, System.currentTimeMillis());
            }
        });

        XposedBridge.hookAllMethods(Activity.class, "startActivity", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(final MethodHookParam param) {
                final String activityName = param.thisObject.getClass().getName();
                final Intent intent = (Intent) param.args[0];
                final Bundle bundle = intent.getExtras();
                LogUtils.logRecord("start Activity: " + activityName + " " + (bundle == null ? "<null>" : bundle.toString()));
                // prevent duplicate startActivity
                if (!intent.getBooleanExtra("isSkipped", false) && HookLoader.splashMap.containsKey(activityName))
                    param.setResult(null);
            }
        });
    }
}