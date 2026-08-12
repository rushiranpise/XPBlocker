package com.aviraxp.xpblocker.next.hook;

import static com.aviraxp.xpblocker.next.helper.PreferencesHelper.isActViewHookEnabled;
import static com.aviraxp.xpblocker.next.helper.PreferencesHelper.isAggressiveHookEnabled;
import static com.aviraxp.xpblocker.next.helper.PreferencesHelper.whiteListElements;
import static com.aviraxp.xpblocker.next.hook.HookLoader.actViewList;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.aviraxp.xpblocker.next.util.LogUtils;

import java.util.HashSet;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

class ActViewHook {
    private static final HashSet<String> aggressiveBlockCache = new HashSet<>();

    public static void hook(final XC_LoadPackage.LoadPackageParam lpparam) {
        if (!isActViewHookEnabled()) return;

        final XC_MethodHook activityStartHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(@NonNull final MethodHookParam param) {
                if (param.args[0] == null) return;
                final ComponentName component = ((Intent) param.args[0]).getComponent();
                final String activityClassName = component == null ? null : component.getClassName();
                if (activityClassName == null || whiteListElements().contains(activityClassName)) return;
                if (!actViewList.contains(activityClassName) && !(isAggressiveHookEnabled() && isAggressiveBlock(activityClassName))) return;

                param.setResult(null);
                LogUtils.logRecord("Activity Block Success: " + lpparam.packageName + "/" + activityClassName);
            }
        };

        final XC_MethodHook viewHook = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(@NonNull final MethodHookParam param) {
                hideIfAdView((View) param.thisObject, lpparam.packageName);
            }
        };

        final XC_MethodHook visibilityHook = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(@NonNull final MethodHookParam param) {
                if ((Integer) param.args[0] == 8) return;
                hideIfAdView((View) param.thisObject, lpparam.packageName);
            }
        };

        XposedHelpers.findAndHookMethod(ContextWrapper.class, "startActivity", Intent.class, activityStartHook);
        XposedHelpers.findAndHookMethod(Activity.class, "startActivityForResult", Intent.class, int.class, Bundle.class, activityStartHook);
        XposedHelpers.findAndHookMethod(View.class, "setVisibility", int.class, visibilityHook);
        XposedBridge.hookAllConstructors(View.class, viewHook);
        XposedBridge.hookAllConstructors(ViewGroup.class, viewHook);
    }

    private static boolean isAggressiveBlock(final String activityClassName) {
        if (aggressiveBlockCache.contains(activityClassName)) return true;
        for (final String listItem : HookLoader.actViewList_aggressive) {
            if (activityClassName.contains(listItem)) {
                aggressiveBlockCache.add(activityClassName);
                return true;
            }
        }
        return false;
    }

    private static void hideIfAdView(@NonNull final View paramView, final String paramString) {
        final String viewName = paramView.getClass().getName();
        if (whiteListElements().contains(viewName)) return;
        if ((!isAggressiveHookEnabled() || !isAggressiveBlock(viewName)) && !actViewList.contains(viewName)) return;
        paramView.clearAnimation();
        paramView.setVisibility(View.GONE);
        LogUtils.logRecord("View Block Success: " + paramString + "/" + viewName);
    }
}