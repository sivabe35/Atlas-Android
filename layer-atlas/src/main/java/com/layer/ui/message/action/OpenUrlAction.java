package com.layer.ui.message.action;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.util.Map;

public class OpenUrlAction extends Action<Map<String, String>> {

    private static final String KEY_URL = "url";

    private Intent mBrowserIntent;

    public OpenUrlAction(@NonNull Map<String, String> data) {
        super("open-url", data);
        mBrowserIntent = new Intent(Intent.ACTION_VIEW);
    }

    @Override
    public void performAction(@NonNull Context context) {
        if (getData() == null || !getData().containsKey("url")) {
            throw new IllegalStateException("Incorrect data. No url to open");
        }

        openUrl(context, getData().get(KEY_URL));
    }

    @Override
    public void performAction(@NonNull Context context, @NonNull Map<String, String> customData) {
        if (!customData.containsKey(KEY_URL)) {
            throw new IllegalStateException("Incorrect data. No url to open");
        }

        String url = customData.get(KEY_URL);
        openUrl(context, url);
    }

    private void openUrl(Context context, String url) {
        mBrowserIntent.setData(Uri.parse(url));
        context.startActivity(mBrowserIntent);
    }
}
