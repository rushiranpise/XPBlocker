package com.aviraxp.xpblocker.next.hook;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;

import androidx.annotation.NonNull;

import com.aviraxp.xpblocker.next.helper.PreferencesHelper;
import com.aviraxp.xpblocker.next.util.ContextUtils;
import com.aviraxp.xpblocker.next.util.LogUtils;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

class ServicesHook {
    public static void hook(final XC_LoadPackage.LoadPackageParam lpparam) {
        if (!PreferencesHelper.isServicesHookEnabled() || !"android".equals(lpparam.packageName)) return;

        final XC_MethodHook servicesStartHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(@NonNull final MethodHookParam param) {
                handleServiceStart(param, (Intent) param.args[1]);
            }
        };
        final XC_MethodHook servicesBindHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(@NonNull final MethodHookParam param) {
                handleServiceStart(param, (Intent) param.args[2]);
            }
        };

        final Class<?> activeServices = XposedHelpers.findClass("com.android.server.am.ActiveServices", lpparam.classLoader);
        XposedBridge.hookAllMethods(activeServices, "startServiceLocked", servicesStartHook);
        XposedBridge.hookAllMethods(activeServices, "bindServiceLocked", servicesBindHook);
    }

    private static void handleServiceStart(final XC_MethodHook.MethodHookParam param, final Intent serviceIntent) {
        final ComponentName serviceName = serviceIntent == null ? null : serviceIntent.getComponent();
        if (serviceName == null) return;
        final String splitServicesName = serviceName.getClassName();
        if (!HookLoader.servicesList.contains(splitServicesName) || PreferencesHelper.whiteListElements().contains(splitServicesName)) return;

        final String packageName = serviceName.getPackageName();
        try {
            final ApplicationInfo info = ContextUtils.getSystemContext().getPackageManager().getApplicationInfo(packageName, 0);
            if ((info.flags & ApplicationInfo.FLAG_SYSTEM) == 0) param.setResult(null);
        } catch (final Throwable e) {
            return;
        }
        LogUtils.logRecord("Service Block Success: " + serviceName.flattenToShortString());
    }
}