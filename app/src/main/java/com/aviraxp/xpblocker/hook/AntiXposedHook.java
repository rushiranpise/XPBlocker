package com.aviraxp.xpblocker.hook;

import com.aviraxp.xpblocker.helper.PreferencesHelper;
import com.aviraxp.xpblocker.util.LogUtils;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

class AntiXposedHook {

    public void hook(final XC_LoadPackage.LoadPackageParam lpparam) {

        if (!PreferencesHelper.isDisableXposedEnabled()) {
            return;
        }

        XC_MethodHook disableXposedHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (param.args[0].equals("disableHooks") || param.args[0].equals("sHookedMethodCallbacks")) {
                    param.setThrowable(new NoClassDefFoundError());
                    LogUtils.logRecord("AntiXposedHook Success: " + lpparam.packageName);
                }
            }
        };

        XposedHelpers.findAndHookMethod(Class.class, "getDeclaredField", String.class, disableXposedHook);
    }
}
