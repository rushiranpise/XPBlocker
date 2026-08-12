package com.aviraxp.xpblocker.ui;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.aviraxp.xpblocker.BuildConfig;
import com.aviraxp.xpblocker.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        // The Xposed hooks read the prefs via
        // XSharedPreferences(BuildConfig.APPLICATION_ID), which loads
        // shared_prefs/<packageName>_preferences.xml (the same file the
        // framework PreferenceManager used). Keep the androidx preference
        // manager writing to that same file so the module sees every change.
        getPreferenceManager().setSharedPreferencesName(requireContext().getPackageName() + "_preferences");
        setPreferencesFromResource(R.xml.pref_settings, rootKey);

        uriListener();
        licensesListener();
        hideIconListener();
    }

    private void uriListener() {
        uriHelper("GITHUB", "https://github.com/rushiranpise/XPBlocker");
        uriHelper("MAINTAINER", "https://github.com/rushiranpise");
        //uriHelper("XDA", "https://forum.xda-developers.com/xposed/modules/xposed-adblocker-reborn-1-0-1-2017-02-11-t3554617");
    }

    private void uriHelper(String pref, final String uri) {
        Preference preference = findPreference(pref);
        if (preference == null) return;
        preference.setOnPreferenceClickListener(p -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW)
                  .setData(Uri.parse(uri));
            startActivity(intent);
            return true;
        });
    }

    private void licensesListener() {
        Preference preference = findPreference("LICENSES");
        if (preference == null) return;
        preference.setOnPreferenceClickListener(p -> {
            new LicensesDialog(requireContext(), false)
                    .setTitle(R.string.licensedialog)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
            return true;
        });
    }

    private void hideIconListener() {
        Preference preference = findPreference("HIDEICON");
        if (preference == null) return;
        preference.setOnPreferenceChangeListener((pref, obj) -> {
            PackageManager packageManager = requireContext().getPackageManager();
            ComponentName aliasName = new ComponentName(requireContext(), BuildConfig.APPLICATION_ID + ".SettingsActivityLauncher");
            if ((boolean) obj) {
                packageManager.setComponentEnabledSetting(aliasName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            } else {
                packageManager.setComponentEnabledSetting(aliasName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
            }
            return true;
        });
    }
}
