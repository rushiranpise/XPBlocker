package com.aviraxp.adblocker.reborn.util;

import com.aviraxp.adblocker.reborn.helper.PreferencesHelper;

import de.robv.android.xposed.XposedBridge;

public class LogUtils {
    public static void logRecord(String string) {
        if (PreferencesHelper.isDebugModeEnabled()) {
            XposedBridge.log(string);
        }
    }
}
