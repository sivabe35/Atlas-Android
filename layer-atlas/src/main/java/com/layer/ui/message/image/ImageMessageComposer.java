package com.layer.ui.message.image;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Message;

import java.io.File;
import java.io.IOException;

public abstract class ImageMessageComposer {
    private Context mContext;
    private LayerClient mLayerClient;

    public ImageMessageComposer(@NonNull Context context, @NonNull LayerClient layerClient) {
        mContext = context;
        mLayerClient = layerClient;
    }

    public abstract Message newImageMessage(@NonNull Uri imageUri) throws IOException;

    public abstract Message newImageMessage(@NonNull File file) throws IOException;

    public Context getContext() {
        return mContext;
    }

    public LayerClient getLayerClient() {
        return mLayerClient;
    }
}
