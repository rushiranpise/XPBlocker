package com.aviraxp.xpblocker.next.util;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ContextUtils {
    @NonNull
    @SuppressLint("PrivateApi")
    public static Class<?> getActivityThreadClass() {
        try {
            return Class.forName("android.app.ActivityThread");
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static Context getSystemContext() {
        try {
            final Object activityThread = getActivityThread();

            final Class<?> aClass = activityThread.getClass();
            Method getSystemContext;
            try {
                getSystemContext = aClass.getMethod("getSystemContext");
            } catch (Throwable e) {
                getSystemContext = aClass.getDeclaredMethod("getSystemContext");
            }
            getSystemContext.setAccessible(true);
            return (Context) getSystemContext.invoke(activityThread);
        } catch (Throwable e) {
            LogUtils.logRecord("ContextUtils::getSystemContext:err=" + e);
            throw new RuntimeException(e);
        }
    }

    private static Object getActivityThread() throws Exception {
        return getActivityThread(null);
    }

    @SuppressLint({"DiscouragedPrivateApi", "PrivateApi"})
    private static Object getActivityThread(Context context) throws Exception {
        Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");

        Method currentActivityThreadMethod;
        try {
            currentActivityThreadMethod = activityThreadClass.getMethod("currentActivityThread");
        } catch (Throwable e) {
            currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
        }
        currentActivityThreadMethod.setAccessible(true);
        Object currentActivityThread = currentActivityThreadMethod.invoke(null);
        if (currentActivityThread != null) return currentActivityThread;
        else if (context != null) {
            // In older versions of Android (prior to frameworks/base 66a017b63461a22842) the currentActivityThread was built on thread locals,
            // so we'll need to try even harder
            Field mLoadedApk = context.getClass().getField("mLoadedApk");
            mLoadedApk.setAccessible(true);
            Object apk = mLoadedApk.get(context);
            if (apk != null) {
                Field mActivityThreadField = apk.getClass().getDeclaredField("mActivityThread");
                mActivityThreadField.setAccessible(true);
                return mActivityThreadField.get(apk);
            }
        }

        Field sCurrentActivityThreadField;
        try {
            sCurrentActivityThreadField = activityThreadClass.getField("sCurrentActivityThread");
        } catch (Throwable e) {
            sCurrentActivityThreadField = activityThreadClass.getDeclaredField("sCurrentActivityThread");
        }
        sCurrentActivityThreadField.setAccessible(true);

        return sCurrentActivityThreadField.get(null);
    }
}