package com.aviraxp.adblocker.reborn.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;

import com.aviraxp.adblocker.reborn.BuildConfig;
import com.aviraxp.adblocker.reborn.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("deprecation")
@SuppressLint("WorldReadableFiles")
public class SettingsActivity extends PreferenceActivity {

    static boolean isActivated = false;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWorldReadable();
        addPreferencesFromResource(R.xml.pref_settings);
        checkState();
        showUpdateLog();
        new AppPicker().execute();
        removePreference();
        uriListener();
        hideIconListener();
        licensesListener();
    }

    private void showUpdateLog() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (sp.getInt("VERSION", 0) != BuildConfig.VERSION_CODE) {
            new LicensesDialog(SettingsActivity.this, getLocalUpdateLog())
                    .setTitle(R.string.updatelog)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
            sp.edit().putInt("VERSION", BuildConfig.VERSION_CODE)
                    .apply();
        }
    }

    private String getLocalUpdateLog() {
            return "file:///android_asset/html/update_en.html";
    }

    private void uriListener() {
        uriHelper("GITHUB", "https://github.com/HardcodedCat/AdBlocker_Reborn_v2");
        uriHelper("MAINTAINER", "https://github.com/HardcodedCat");
        uriHelper("XDA", "https://forum.xda-developers.com/xposed/modules/xposed-adblocker-reborn-1-0-1-2017-02-11-t3554617");
    }

    private void uriHelper(String pref, final String uri) {
        findPreference(pref).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW)
                        .setData(Uri.parse(uri));
                startActivity(intent);
                return true;
            }
        });
    }

    private void licensesListener() {
        findPreference("LICENSES").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new LicensesDialog(SettingsActivity.this, "file:///android_asset/html/licenses.html")
                        .setTitle(R.string.licensedialog)
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
                return true;
            }
        });
    }

    private void checkState() {
        if (!isActivated) {
            new AlertDialog.Builder(this)
                    .setCancelable(true)
                    .setMessage(R.string.hint_reboot_not_active)
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @SuppressLint("SetWorldReadable")
    private void setWorldReadable() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            File dataDir = new File(getApplicationInfo().dataDir);
            File prefsDir = new File(dataDir, "shared_prefs");
            File prefsFile = new File(prefsDir, getPreferenceManager().getSharedPreferencesName() + ".xml");
            if (prefsFile.exists()) {
                for (File file : new File[]{dataDir, prefsDir, prefsFile}) {
                    file.setReadable(true, false);
                    file.setExecutable(true, false);
                }
            }
        } else {
            getPreferenceManager().setSharedPreferencesMode(MODE_WORLD_READABLE);
        }
    }

    private void hideIconListener() {
        findPreference("HIDEICON").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object obj) {
                PackageManager packageManager = SettingsActivity.this.getPackageManager();
                ComponentName aliasName = new ComponentName(SettingsActivity.this, BuildConfig.APPLICATION_ID + ".SettingsActivityLauncher");
                if ((boolean) obj) {
                    packageManager.setComponentEnabledSetting(aliasName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                } else {
                    packageManager.setComponentEnabledSetting(aliasName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
                }
                return true;
            }
        });
    }

    private class AppPicker extends AsyncTask<Void, Void, Void> {

        private final MultiSelectListPreference selectedApps = (MultiSelectListPreference) findPreference("SELECTED_APPS");
        private final List<CharSequence> appNames = new ArrayList<>();
        private final List<CharSequence> packageNames = new ArrayList<>();

        @Override
        protected void onPreExecute() {
            selectedApps.setEnabled(false);
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            final List<String[]> sortedApps = new ArrayList<>();
            final PackageManager pm = getApplicationContext().getPackageManager();
            final List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

            for (ApplicationInfo app : packages) {
                if (!app.packageName.startsWith("com.android") && !app.packageName.equalsIgnoreCase("android") || app.packageName.equals("com.android.webview") || app.packageName.equals("com.android.chrome")) {
                    sortedApps.add(new String[]{app.packageName, app.loadLabel(pm).toString()});
                }
            }

            Collections.sort(sortedApps, new Comparator<String[]>() {
                @Override
                public int compare(String[] entry1, String[] entry2) {
                    return entry1[1].compareToIgnoreCase(entry2[1]);
                }
            });

            for (int i = 0; i < sortedApps.size(); i++) {
                appNames.add(sortedApps.get(i)[1] + "\n" + "(" + sortedApps.get(i)[0] + ")");
                packageNames.add(sortedApps.get(i)[0]);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            final CharSequence[] appNamesList = appNames.toArray(new CharSequence[0]);
            final CharSequence[] packageNamesList = packageNames.toArray(new CharSequence[0]);
            selectedApps.setEntries(appNamesList);
            selectedApps.setEntryValues(packageNamesList);
            selectedApps.setEnabled(true);
        }
    }
}
