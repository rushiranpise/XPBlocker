package com.aviraxp.xpblocker.next.hook;

import com.aviraxp.xpblocker.next.helper.PreferencesHelper;
import com.aviraxp.xpblocker.next.util.BlocklistInitUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookLoader implements IXposedHookLoadPackage, IXposedHookZygoteInit {
    static final HashSet<String> actViewList = new HashSet<>();
    static final HashSet<String> actViewList_aggressive = new HashSet<>();
    static final HashSet<String> hostsList = new HashSet<>();
    static final HashSet<String> receiversList = new HashSet<>();
    static final HashSet<String> servicesList = new HashSet<>();
    static final HashSet<String> urlList = new HashSet<>();
    static final Map<String, String> splashMap = new HashMap<>();

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) {
        SelfHook.hook(lpparam);
        ServicesHook.hook(lpparam);
        ShortcutHook.hook(lpparam);

        if (PreferencesHelper.isAndroidApp(lpparam.packageName)) return;

        HostsHook.hook(lpparam);
        ActViewHook.hook(lpparam);
        AntiXposedHook.hook(lpparam);
        BackPressHook.hook(lpparam);
        ReceiversHook.hook(lpparam);
        URLHook.hook(lpparam);
        WebViewHook.hook(lpparam);
        SplashHook.hook(lpparam);
    }

    @Override
    public void initZygote(final StartupParam startupParam) throws IOException {
        BlocklistInitUtils.init(startupParam, "blocklist/hosts", HookLoader.hostsList);
        BlocklistInitUtils.init(startupParam, "blocklist/hosts_yhosts", HookLoader.hostsList);
        BlocklistInitUtils.init(startupParam, "blocklist/urls", HookLoader.urlList);
        BlocklistInitUtils.init(startupParam, "blocklist/services", HookLoader.servicesList);
        BlocklistInitUtils.init(startupParam, "blocklist/receivers", HookLoader.receiversList);
        BlocklistInitUtils.init(startupParam, "blocklist/av", HookLoader.actViewList);
        BlocklistInitUtils.init(startupParam, "blocklist/av_aggressive", HookLoader.actViewList_aggressive);

        final HashSet<String> splashList = new HashSet<>();
        BlocklistInitUtils.init(startupParam, "blocklist/splash", splashList);
        for (final String s : splashList) {
            if (s.startsWith("#")) continue;
            final String[] split = s.split(",");
            HookLoader.splashMap.put(split[0], split.length == 1 ? null : split[1]);
        }
    }
}