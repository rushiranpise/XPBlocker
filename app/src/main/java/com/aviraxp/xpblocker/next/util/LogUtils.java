package com.aviraxp.xpblocker.next.util;

import android.content.Context;
import android.widget.Toast;

import com.aviraxp.xpblocker.next.helper.PreferencesHelper;

import de.robv.android.xposed.XposedBridge;

public class LogUtils {
    public static void logRecord(String string) {
        if (!PreferencesHelper.isDebugModeEnabled()) return;
        XposedBridge.log(string);
    }

    public static void toast(Context context, String string) {
        if (!PreferencesHelper.isDebugModeEnabled()) return;
        logRecord(string);
        Toast.makeText(context, string, Toast.LENGTH_SHORT).show();
    }
}