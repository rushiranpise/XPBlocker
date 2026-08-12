package com.aviraxp.xpblocker.next.hook;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aviraxp.xpblocker.next.helper.PreferencesHelper;
import com.aviraxp.xpblocker.next.util.LogUtils;

import java.net.MalformedURLException;
import java.net.URL;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

class URLHook {
    public static void hook(final XC_LoadPackage.LoadPackageParam lpparam) {
        if (!PreferencesHelper.isURLHookEnabled()) return;

        final XC_MethodHook urlHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(final MethodHookParam param) throws MalformedURLException {
                final String url = urlLengthTweaker(param);
                if (url == null) return;
                for (final String adUrl : HookLoader.urlList) {
                    if (lengthTweaker(param) != null && url.contains(adUrl) && !PreferencesHelper.whiteListElements().contains(url)) {
                        lengthSetter(param, lengthTweaker(param));
                        LogUtils.logRecord("URL Block Success: " + lpparam.packageName + "/" + url);
                        return;
                    }
                }
            }
        };

        final XC_MethodHook hostsHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(final MethodHookParam param) throws MalformedURLException {
                final String url = lengthTweaker(param);
                if (url == null || !url.startsWith("http")) return;
                final String urlCutting = url.substring(url.indexOf("://") + 3);
                for (final String adUrl : HookLoader.hostsList) {
                    if (urlCutting.startsWith(adUrl) && !PreferencesHelper.whiteListElements().contains(url)) {
                        lengthSetter(param, url);
                        LogUtils.logRecord("URL Block Success: " + lpparam.packageName + "/" + url);
                        return;
                    }
                }
            }
        };

        XposedHelpers.findAndHookConstructor(URL.class, String.class, hostsHook);
        XposedHelpers.findAndHookConstructor(URL.class, URL.class, String.class, hostsHook);
        XposedHelpers.findAndHookConstructor(URL.class, URL.class, String.class, urlHook);
        XposedHelpers.findAndHookConstructor(URL.class, String.class, String.class, String.class, urlHook);
        XposedHelpers.findAndHookConstructor(URL.class, String.class, String.class, String.class, hostsHook);
        XposedHelpers.findAndHookConstructor(URL.class, String.class, String.class, int.class, String.class, hostsHook);
        XposedHelpers.findAndHookConstructor(URL.class, String.class, String.class, int.class, String.class, urlHook);
    }

    @NonNull
    private static String httpsTweaker(@NonNull final String string) {
        return string.startsWith("https") ? "https://127.0.0.1" : "http://127.0.0.1";
    }

    @Nullable
    private static String lengthTweaker(@NonNull final XC_MethodHook.MethodHookParam param) {
        if (param.args.length == 1) return (String) param.args[0];
        if (param.args.length != 2) return (String) param.args[1];
        final URL urlOrigin = (URL) param.args[0];
        return urlOrigin != null ? urlOrigin.toString() : null;
    }

    private static String urlLengthTweaker(@NonNull final XC_MethodHook.MethodHookParam param) {
        if (param.args.length == 2) return lengthTweaker(param) == null ? (String) param.args[1] : lengthTweaker(param) + param.args[1];
        if (param.args.length == 3) return (String) param.args[2];
        return (String) param.args[3];
    }

    private static void lengthSetter(@NonNull final XC_MethodHook.MethodHookParam param, final String string) throws MalformedURLException {
        if (param.args.length == 1) param.args[0] = httpsTweaker(string);
        else if (param.args.length != 2) param.args[1] = httpsTweaker(string);
        else {
            final URL url = new URL(httpsTweaker(string));
            param.args[0] = url;
        }
    }
}