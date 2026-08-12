package com.aviraxp.xpblocker.next.util;

import static de.robv.android.xposed.IXposedHookZygoteInit.StartupParam;

import android.content.res.Resources;
import android.content.res.XModuleResources;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class BlocklistInitUtils {
    public static void init(@NonNull StartupParam startupParam, String resName, HashSet<String> blocklistName) throws IOException {
        Resources res = XModuleResources.createInstance(startupParam.modulePath, null);
        byte[] array = XposedHelpers.assetAsByteArray(res, resName);

        final String[] split = decodeString(resName, array).split("\r?\n");
        if (true) {
            XposedBridge.log("---> nigga split: " + split.length);
            Log.d("AWAISKING_APP", "split: " + split.length);
        }
        Collections.addAll(blocklistName, split);
    }

    private static String decodeString(@NonNull String resName, byte[] array) {
        String decoded = new String(array, StandardCharsets.UTF_8);
        // if (
        //         "blocklist/hosts".equals(resName)
        //         || "blocklist/hosts_yhosts".equals(resName)
        // ) {
        //     decoded = decoded.replace("127.0.0.1 ", "")
        //                      .replace("0.0.0.0 ", "")
        //                      .replace("localhost", "workaround")
        //     ;
        // }
        return decoded;
    }
}