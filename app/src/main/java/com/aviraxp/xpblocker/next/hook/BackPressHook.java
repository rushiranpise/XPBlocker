package com.aviraxp.xpblocker.next.hook;

import androidx.annotation.NonNull;

import com.aviraxp.xpblocker.next.helper.PreferencesHelper;
import com.aviraxp.xpblocker.next.util.LogUtils;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

class BackPressHook {
    public static void hook(final XC_LoadPackage.LoadPackageParam lpparam) {
        if (!PreferencesHelper.isBackPressHookEnabled()) return;

        final XC_MethodHook backPressHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(@NonNull final MethodHookParam param) {
                if ((Boolean) param.args[0]) return;
                param.args[0] = true;
                LogUtils.logRecord("BackPressHook Success: " + lpparam.packageName);
            }
        };

        XposedBridge.hookAllMethods(android.app.Dialog.class, "setCancelable", backPressHook);
        XposedBridge.hookAllMethods(android.app.Dialog.class, "setCanceledOnTouchOutside", backPressHook);
        XposedBridge.hookAllMethods(android.app.AlertDialog.Builder.class, "setCancelable", backPressHook);
        XposedBridge.hookAllMethods(android.app.Activity.class, "setFinishOnTouchOutside", backPressHook);
    }
}