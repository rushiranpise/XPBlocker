package com.aviraxp.xpblocker.hook;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;

import com.aviraxp.xpblocker.helper.PreferencesHelper;
import com.aviraxp.xpblocker.util.LogUtils;

import java.util.HashSet;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

class SplashHook {

    private Pair<String, Long> lastStartActivity;
    private Pair<String, Long> lastFinishActivity;

    public void hook(final XC_LoadPackage.LoadPackageParam lpparam) {

        if (!PreferencesHelper.isSplashHookEnabled()) {
            return;
        }

        XposedBridge.hookAllMethods(Activity.class, "onCreate", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                String activityName = param.thisObject.getClass().getName();

                lastStartActivity = new Pair<>(activityName, System.currentTimeMillis());

                if (lastFinishActivity != null && System.currentTimeMillis() - lastFinishActivity.second < 500) {
                    if (!HookLoader.splashMap.containsKey(lastFinishActivity.first))
                        LogUtils.toast((Context) param.thisObject, String.format("New splashScreen detected: %s,%s", lastFinishActivity.first, activityName));
                }

                if (HookLoader.splashMap.containsKey(activityName)) {
                    String nextClassName = HookLoader.splashMap.get(activityName);
                    if (nextClassName != null) {
                        Intent intent = new Intent();
                        Class<?> next = XposedHelpers.findClassIfExists(nextClassName, lpparam.classLoader);
                        if (next == null) {
                            LogUtils.toast((Context) param.thisObject, "Skip Splash Failed: " + activityName);
                            return;
                        }
                        LogUtils.toast((Context) param.thisObject, String.format("Skip Splash: %s => %s", activityName, nextClassName));
                        intent.setClass((Context) param.thisObject, next);
                        intent.putExtra("isSkipped", true);
                        XposedHelpers.callMethod(param.thisObject, "startActivity", intent);
                    } else
                        LogUtils.toast((Context) param.thisObject, "Finish Splash: " + activityName);
                    XposedHelpers.callMethod(param.thisObject, "finish");

                }

            }

        });
        XposedBridge.hookAllMethods(Activity.class, "finish", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                String activityName = param.thisObject.getClass().getName();
                lastFinishActivity = new Pair<>(activityName, System.currentTimeMillis());
                LogUtils.logRecord("finish Activity: " + activityName);
                if (lastStartActivity != null && lastStartActivity.first.equals(activityName) && System.currentTimeMillis() - lastStartActivity.second < 10000 && System.currentTimeMillis() - lastStartActivity.second > 1000) {
                    LogUtils.logRecord(String.format("fastCloseActivity: %s", activityName));
                }
                lastStartActivity = new Pair<String, Long>(activityName, System.currentTimeMillis());
            }
        });

        XposedBridge.hookAllMethods(Activity.class, "startActivity", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                String activityName = param.thisObject.getClass().getName();
                Intent intent = (Intent) param.args[0];
                Bundle bundle = (Bundle) intent.getExtras();
                LogUtils.logRecord("start Activity: " + activityName + " " + (bundle == null ? "<null>" : bundle.toString()));
                // prevent duplicate startActivity
                if (!intent.getBooleanExtra("isSkipped", false) && HookLoader.splashMap.containsKey(activityName))
                    param.setResult(null);
            }
        });
    }

}