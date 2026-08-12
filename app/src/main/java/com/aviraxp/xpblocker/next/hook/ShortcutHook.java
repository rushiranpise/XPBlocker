package com.aviraxp.xpblocker.next.hook;

import android.content.Intent;

import androidx.annotation.NonNull;

import com.aviraxp.xpblocker.next.helper.PreferencesHelper;
import com.aviraxp.xpblocker.next.util.LogUtils;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

class ShortcutHook {
    public static void hook(@NonNull final XC_LoadPackage.LoadPackageParam lpparam) {
        if (!lpparam.packageName.equals("android")) return;

        final XC_MethodHook shortcutHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(@NonNull final MethodHookParam param) {
                final Intent intent = (Intent) param.args[2];
                if (intent != null && "com.android.launcher.action.INSTALL_SHORTCUT".equals(intent.getAction())) {
                    final String packageName = (String) param.args[1];
                    if (!PreferencesHelper.isShortcutHookEnabled()) return;
                    param.setResult(0);
                    LogUtils.logRecord("Shortcut Block Success:" + packageName);
                }
            }
        };
        XposedBridge.hookAllMethods(XposedHelpers.findClass("com.android.server.am.ActivityManagerService", lpparam.classLoader),
                                    "broadcastIntentLocked", shortcutHook);
    }
}