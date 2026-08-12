package com.aviraxp.xpblocker.next.hook;

import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;

import com.aviraxp.xpblocker.next.helper.PreferencesHelper;
import com.aviraxp.xpblocker.next.util.LogUtils;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

class WebViewHook {
    private static boolean adExist = false;

    public static void hook(final XC_LoadPackage.LoadPackageParam lpparam) {
        if (!PreferencesHelper.isWebViewHookEnabled()) return;

        final XC_MethodHook requestHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(final MethodHookParam param) {
                final String url = checkURL(param);
                final String urlCutting = url.substring(url.indexOf("://") + 3);
                for (final String adUrl : HookLoader.hostsList) {
                    if (urlCutting.startsWith(adUrl) && !PreferencesHelper.whiteListElements().contains(url)) {
                        param.setResult(new WebResourceResponse(null, null, null));
                        LogUtils.logRecord("WebViewClient Block Success: " + lpparam.packageName + "/" + url);
                        return;
                    }
                }
                for (final String adUrl : HookLoader.urlList) {
                    if (urlCutting.contains(adUrl) && !PreferencesHelper.whiteListElements().contains(url)) {
                        param.setResult(new WebResourceResponse(null, null, null));
                        LogUtils.logRecord("WebViewClient Block Success: " + lpparam.packageName + "/" + url);
                        return;
                    }
                }
            }
        };

        final XC_MethodHook urlHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(@NonNull final MethodHookParam param) {
                final String url = (String) param.args[0];
                if (url == null) return;
                adExist = urlFiltering(url, null, param);
                if (adExist) LogUtils.logRecord("WebView Block Success: " + lpparam.packageName + "/" + url);
            }
        };

        final XC_MethodHook loadDataHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(@NonNull final MethodHookParam param) {
                final String data = (String) param.args[0];
                if (data == null) return;
                adExist = urlFiltering(null, data, param);
                if (adExist) LogUtils.logRecord("WebView Block Success: " + lpparam.packageName + "/" + data);
            }
        };

        final XC_MethodHook loadDataWithBaseURL = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(@NonNull final MethodHookParam param) {
                final String url = (String) param.args[0];
                final String data = (String) param.args[1];
                if (url == null && data == null) return;
                adExist = urlFiltering(url, data, param);
                if (adExist) LogUtils.logRecord("WebView Block Success: " + lpparam.packageName + "/" + url + " & " + data);
            }
        };

        XposedBridge.hookAllMethods(WebViewClient.class, "shouldInterceptRequest", requestHook);
        XposedBridge.hookAllMethods(WebView.class, "postUrl", urlHook);
        XposedBridge.hookAllMethods(WebView.class, "loadUrl", urlHook);
        XposedBridge.hookAllMethods(WebView.class, "loadData", loadDataHook);
        XposedBridge.hookAllMethods(WebView.class, "loadDataWithBaseURL", loadDataWithBaseURL);
    }

    private static boolean urlFiltering(final String url, final String data, final XC_MethodHook.MethodHookParam param) {
        return hostsBlock(url, param) || hostsBlock(data, param) || urlBlock(url, param) || urlBlock(data, param);
    }

    private static boolean hostsBlock(final String string, final XC_MethodHook.MethodHookParam param) {
        if (string != null && string.startsWith("http")) for (final String adUrl : HookLoader.hostsList) {
            if (string.substring(string.indexOf("://") + 3).startsWith(adUrl) && !PreferencesHelper.whiteListElements().contains(string)) {
                param.setResult(null);
                ((View) param.thisObject).clearAnimation();
                ((View) param.thisObject).setVisibility(View.GONE);
                return true;
            }
        }
        return false;
    }

    private static boolean urlBlock(final String string, final XC_MethodHook.MethodHookParam param) {
        if (string != null && string.startsWith("http")) for (final String adUrl : HookLoader.urlList) {
            if (string.contains(adUrl) && !PreferencesHelper.whiteListElements().contains(string)) {
                param.setResult(null);
                ((View) param.thisObject).clearAnimation();
                ((View) param.thisObject).setVisibility(View.GONE);
                return true;
            }
        }
        return false;
    }

    @NonNull
    private static String checkURL(@NonNull final XC_MethodHook.MethodHookParam param) {
        if (!(param.args[1] instanceof String)) {
            final WebResourceRequest request = (WebResourceRequest) param.args[1];
            return request.getUrl().toString();
        }
        return param.args[1].toString();
    }
}