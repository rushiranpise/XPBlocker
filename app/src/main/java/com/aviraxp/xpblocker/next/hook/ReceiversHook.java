package com.aviraxp.xpblocker.next.hook;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;

import com.aviraxp.xpblocker.next.helper.PreferencesHelper;
import com.aviraxp.xpblocker.next.util.ContextUtils;
import com.aviraxp.xpblocker.next.util.LogUtils;

import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

class ReceiversHook {
    public static void hook(final XC_LoadPackage.LoadPackageParam lpparam) {
        if (!PreferencesHelper.isReceiversHookEnabled()) return;

        try {
            final ActivityInfo[] receiverInfo = ContextUtils.getSystemContext().getPackageManager()
                                                            .getPackageInfo(lpparam.packageName, PackageManager.GET_RECEIVERS).receivers;
            if (receiverInfo == null) return;
            for (final ActivityInfo info : receiverInfo) {
                final String className = info.getClass().getName();
                if (!HookLoader.receiversList.contains(className) || PreferencesHelper.whiteListElements().contains(className)) continue;
                XposedHelpers.findAndHookMethod(info.name, lpparam.classLoader, "onReceive",
                                                Context.class, Intent.class, XC_MethodReplacement.DO_NOTHING);
                LogUtils.logRecord("Receiver Block Success: " + lpparam.packageName + "/" + info.name);
            }
        } catch (final Exception ignored) {
        }
    }
}