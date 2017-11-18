package com.layer.ui.message.action;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.google.gson.JsonObject;
import com.layer.sdk.LayerClient;
import com.layer.ui.util.imagepopup.ImagePopupActivity;

public class OpenUrlActionHandler extends ActionHandler {

    public static final String KEY_URL = "url";

    public static final String KEY_MIME_TYPE = "mime-type";
    public static final String KEY_HEIGHT = "height";
    public static final String KEY_WIDTH = "width";

    private Intent mBrowserIntent;

    public OpenUrlActionHandler(LayerClient layerClient) {
        super(layerClient, "open-url");
        mBrowserIntent = new Intent(Intent.ACTION_VIEW);
        ImagePopupActivity.init(layerClient);
    }

    @Override
    public void performAction(@NonNull Context context, JsonObject data) {
        if (data == null || !data.has(KEY_URL)) {
            throw new IllegalStateException("Incorrect data. No url to open");
        }

        if (data.has(KEY_MIME_TYPE)) {
            openPopupImage(context, data);
        } else {
            String url = data.get(KEY_URL).getAsString();
            openUrl(context, url);
        }
    }

    private void openUrl(Context context, String url) {
        mBrowserIntent.setData(Uri.parse(url));
        context.startActivity(mBrowserIntent);
    }

    private void openPopupImage(Context context, JsonObject data) {
        Intent intent = new Intent(context, ImagePopupActivity.class);
        ImagePopupActivity.Parameters parameters = new ImagePopupActivity.Parameters();
        String url = data.get(KEY_URL).getAsString();
        parameters.source(url);

        int width = data.get(KEY_WIDTH).getAsInt();
        int height = data.get(KEY_HEIGHT).getAsInt();
        parameters.dimensions(width, height);

        intent.putExtra(ImagePopupActivity.EXTRA_PARAMS, parameters);
        context.startActivity(intent);
    }
}
