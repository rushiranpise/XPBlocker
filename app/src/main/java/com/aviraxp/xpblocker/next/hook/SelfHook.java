package com.aviraxp.xpblocker.next.hook;

import androidx.annotation.NonNull;

import com.aviraxp.xpblocker.next.BuildConfig;

import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

class SelfHook {
    public static void hook(@NonNull final XC_LoadPackage.LoadPackageParam lpparam) {
        if (!lpparam.packageName.equals(BuildConfig.APPLICATION_ID)) return;
        Class<?> SettingsActivity = XposedHelpers.findClass(BuildConfig.APPLICATION_ID + ".ui.SettingsActivity", lpparam.classLoader);
        XposedHelpers.setStaticBooleanField(SettingsActivity, "isActivated", true);
    }
}