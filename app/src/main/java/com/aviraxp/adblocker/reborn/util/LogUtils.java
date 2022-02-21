package com.aviraxp.adblocker.reborn.util;

import android.content.Context;
import android.widget.Toast;

import com.aviraxp.adblocker.reborn.helper.PreferencesHelper;

import de.robv.android.xposed.XposedBridge;

public class LogUtils {
    public static void logRecord(String string) {
        if (PreferencesHelper.isDebugModeEnabled()) {
            XposedBridge.log(string);
        }
    }

    public static void toast(Context context, String string) {
        if (PreferencesHelper.isDebugModeEnabled()) {
            logRecord(string);
            Toast.makeText( context, string, Toast.LENGTH_SHORT).show();
        }
    }
}
