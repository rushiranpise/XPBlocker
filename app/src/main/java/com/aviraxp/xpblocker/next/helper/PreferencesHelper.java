package com.aviraxp.xpblocker.next.helper;

import androidx.annotation.NonNull;

import com.aviraxp.xpblocker.next.BuildConfig;

import java.util.Arrays;
import java.util.List;

import de.robv.android.xposed.XSharedPreferences;

public class PreferencesHelper {
    private static XSharedPreferences preferences = null;
    private static boolean isNoReloadPreferences = false;

    private static XSharedPreferences getModulePrefs() {
        if (preferences == null) {
            preferences = new XSharedPreferences(BuildConfig.APPLICATION_ID);
            preferences.makeWorldReadable();
            isNoReloadPreferences = preferences.getBoolean("NORELOAD", false);
        } else if (!isNoReloadPreferences) {
            preferences.reload();
        }
        return preferences;
    }

    public static boolean isActViewHookEnabled() {
        return getModulePrefs().getBoolean("ACTVIEW_HOOK", false);
    }

    public static boolean isSplashHookEnabled() {
        return getModulePrefs().getBoolean("SPLASH_HOOK", false);
    }

    public static boolean isHostsHookEnabled() {
        return getModulePrefs().getBoolean("HOSTS_HOOK", false);
    }

    public static boolean isWebViewHookEnabled() {
        return getModulePrefs().getBoolean("WEBVIEW_HOOK", false);
    }

    public static boolean isServicesHookEnabled() {
        return getModulePrefs().getBoolean("SERVICES_HOOK", false);
    }

    public static boolean isReceiversHookEnabled() {
        return getModulePrefs().getBoolean("RECEIVERS_HOOK", false);
    }

    public static boolean isBackPressHookEnabled() {
        return getModulePrefs().getBoolean("BACKPRESS_HOOK", false);
    }

    public static boolean isURLHookEnabled() {
        return getModulePrefs().getBoolean("URL_HOOK", false);
    }

    public static boolean isAggressiveHookEnabled() {
        return getModulePrefs().getBoolean("AGGRESSIVE_HOOK", false);
    }

    public static boolean isShortcutHookEnabled() {
        return getModulePrefs().getBoolean("SHORTCUT_HOOK", false);
    }

    public static boolean isDebugModeEnabled() {
        return getModulePrefs().getBoolean("DEBUG", false);
    }

    public static boolean isDisableXposedEnabled() {
        return getModulePrefs().getBoolean("ANTIXPOSED_HOOK", false);
    }

    public static boolean isAndroidApp(@NonNull String pkg) {
        return pkg.startsWith("com.android") && !"com.android.webview".equals(pkg) || "android".equalsIgnoreCase(pkg);
    }

    @NonNull
    public static List<String> whiteListElements() {
        return Arrays.asList(getModulePrefs().getString("ACTIVITY_WHITELIST", "").split("\r?\n"));
    }
}