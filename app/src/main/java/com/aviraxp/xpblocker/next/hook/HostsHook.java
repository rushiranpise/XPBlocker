package com.aviraxp.xpblocker.next.hook;

import android.net.Network;
import android.os.NetworkOnMainThreadException;
import android.os.StrictMode;

import androidx.annotation.NonNull;

import com.aviraxp.xpblocker.next.helper.PreferencesHelper;
import com.aviraxp.xpblocker.next.util.LogUtils;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

class HostsHook {
    public static void hook(final XC_LoadPackage.LoadPackageParam lpparam) {
        if (!PreferencesHelper.isHostsHookEnabled()) return;

        final XC_MethodHook socketClzHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(@NonNull final MethodHookParam param) {
                if (param.args == null || param.args.length == 0 || param.args[0] == null) return;
                final Object obj = param.args[0];
                final String host = obj instanceof String ? (String) obj : obj instanceof InetAddress
                                                                           ? ((InetAddress) obj).getHostName() : null;
                if (host != null && HookLoader.hostsList.contains(host) && !PreferencesHelper.whiteListElements().contains(host)) {
                    param.setResult(null);
                    LogUtils.logRecord("Hosts Block Success: " + lpparam.packageName + "/" + host);
                }
            }
        };

        final XC_MethodHook inetAddrHookSingleResult = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(@NonNull final MethodHookParam param) {
                final String host = (String) param.args[0];
                if (host != null && HookLoader.hostsList.contains(host) && !PreferencesHelper.whiteListElements().contains(host)) {
                    param.setResult(null);
                    param.setThrowable(new UnknownHostException());
                    LogUtils.logRecord("Hosts Block Success: " + lpparam.packageName + "/" + host);
                }
            }
        };

        final XC_MethodHook inetSockAddrClzHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(@NonNull final MethodHookParam param) {
                if (param.args == null || param.args.length <= 0 || param.args[0] == null) return;

                final Object obj = param.args[0];
                String host = null;
                if (obj instanceof String) host = (String) obj;
                else if (obj instanceof InetAddress) {
                    try {
                        host = ((InetAddress) obj).getHostName();
                    } catch (final NetworkOnMainThreadException e) {
                        final StrictMode.ThreadPolicy oldPolicy = StrictMode.getThreadPolicy();
                        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitNetwork().build());
                        host = ((InetAddress) obj).getHostName();
                        StrictMode.setThreadPolicy(oldPolicy);
                    }
                }
                if (host != null && HookLoader.hostsList.contains(host) && !PreferencesHelper.whiteListElements().contains(host)) {
                    param.setResult(new Object());
                    param.setThrowable(new UnknownHostException());
                    LogUtils.logRecord("Hosts Block Success: " + lpparam.packageName + "/" + host);
                }
            }
        };

        final XC_MethodHook ioBridgeHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(@NonNull final MethodHookParam param) {
                final InetAddress address = (InetAddress) param.args[1];
                final String host = address == null ? null : address.getHostName();
                if (host != null && HookLoader.hostsList.contains(host) && !PreferencesHelper.whiteListElements().contains(host)) {
                    param.setResult(null);
                    param.setThrowable(new UnknownHostException());
                    LogUtils.logRecord("Hosts Block Success: " + lpparam.packageName + "/" + host);
                }
            }
        };

        final XC_MethodHook blockGuardOsHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(@NonNull final MethodHookParam param) {
                final InetAddress address = (InetAddress) param.args[1];
                final String host = address == null ? null : address.getHostName();
                if (host != null && HookLoader.hostsList.contains(host) && !PreferencesHelper.whiteListElements().contains(host)) {
                    param.setResult(null);
                    param.setThrowable(new SocketException());
                    LogUtils.logRecord("Hosts Block Success: " + lpparam.packageName + "/" + host);
                }
            }
        };

        final XC_MethodHook ioBridgeBooleanHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(@NonNull final MethodHookParam param) {
                final InetAddress address = (InetAddress) param.args[1];
                final String host = address == null ? null : address.getHostName();
                if (host != null && HookLoader.hostsList.contains(host) && !PreferencesHelper.whiteListElements().contains(host)) {
                    param.setResult(false);
                    LogUtils.logRecord("Hosts Block Success: " + lpparam.packageName + "/" + host);
                }
            }
        };

        final Class<?> ioBridgeClz = XposedHelpers.findClass("libcore.io.IoBridge", lpparam.classLoader);
        final Class<?> blockGuardOsClz = XposedHelpers.findClass("libcore.io.BlockGuardOs", lpparam.classLoader);

        XposedBridge.hookAllConstructors(Socket.class, socketClzHook);
        XposedBridge.hookAllConstructors(InetSocketAddress.class, inetSockAddrClzHook);
        XposedBridge.hookAllMethods(InetAddress.class, "getAllByName", inetAddrHookSingleResult);
        XposedBridge.hookAllMethods(InetAddress.class, "getByName", inetAddrHookSingleResult);
        XposedBridge.hookAllMethods(InetSocketAddress.class, "createUnresolved", inetAddrHookSingleResult);
        XposedBridge.hookAllMethods(ioBridgeClz, "connectErrno", ioBridgeHook);
        XposedBridge.hookAllMethods(ioBridgeClz, "isConnected", ioBridgeBooleanHook);
        XposedBridge.hookAllMethods(blockGuardOsClz, "connect", blockGuardOsHook);
        XposedBridge.hookAllMethods(Network.class, "getAllByName", inetAddrHookSingleResult);
        XposedBridge.hookAllMethods(Network.class, "getByName", inetAddrHookSingleResult);
    }
}