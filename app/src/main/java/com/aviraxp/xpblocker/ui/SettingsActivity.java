package com.aviraxp.xpblocker.ui;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.preference.PreferenceManager;

import com.aviraxp.xpblocker.BuildConfig;
import com.aviraxp.xpblocker.R;

import java.io.File;

public class SettingsActivity extends AppCompatActivity {
    static boolean isActivated = false;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWorldReadable();
        setContentView(R.layout.activity_settings);

        // Edge-to-edge (enforced on Android 15 / targetSdk 35): draw the toolbar
        // below the status bar and keep the list above the navigation bar.
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, bars.top, 0, bars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
        applySystemBarIcons();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.settings_container, new SettingsFragment())
                    .commit();
        }
        checkState();
        showUpdateLog();
    }

    private void applySystemBarIcons() {
        boolean night = (getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        controller.setAppearanceLightStatusBars(!night);
        controller.setAppearanceLightNavigationBars(!night);
    }

    private void showUpdateLog() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (sp.getInt("VERSION", 0) != BuildConfig.VERSION_CODE) {
            new LicensesDialog(SettingsActivity.this, true)
                    .setTitle(R.string.updatelog)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
            sp.edit().putInt("VERSION", BuildConfig.VERSION_CODE)
              .apply();
        }
    }

    private void checkState() {
        if (!isActivated) {
            new LicensesDialog(SettingsActivity.this, false)
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
            File prefsFile = new File(prefsDir, getPackageName() + "_preferences.xml");
            if (prefsFile.exists()) {
                for (File file : new File[]{dataDir, prefsDir, prefsFile}) {
                    file.setReadable(true, false);
                    file.setExecutable(true, false);
                }
            }
        }
    }
}
