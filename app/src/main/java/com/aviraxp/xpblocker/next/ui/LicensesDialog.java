package com.aviraxp.xpblocker.next.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class LicensesDialog extends MaterialAlertDialogBuilder {
    private static final String LICENSES_URL = "file:///android_asset/html/licenses.html";
    private static final String UPDATE_LOG_URL = "file:///android_asset/html/update_en.html";

    public LicensesDialog(final Context context, boolean isUpdateLog) {
        super(context);
        final WebView view = new WebView(context);
        view.setWebChromeClient(new WebChromeClient());
        view.setWebViewClient(new WebViewClient() {
            @Override
            @SuppressWarnings("deprecation")
            public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
                if (url.startsWith("http")) {
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    return true;
                }
                return super.shouldOverrideUrlLoading(view, url);
            }
        });
        view.loadUrl(isUpdateLog ? UPDATE_LOG_URL : LICENSES_URL);
        setView(view);
    }
}
