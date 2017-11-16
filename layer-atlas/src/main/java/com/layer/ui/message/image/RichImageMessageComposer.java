package com.layer.ui.message.image;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Message;

import java.io.File;
import java.io.IOException;

public class RichImageMessageComposer extends ImageMessageComposer {
    public RichImageMessageComposer(@NonNull Context context, @NonNull LayerClient layerClient) {
        super(context, layerClient);
    }

    @Override
    public Message newImageMessage(@NonNull Uri imageUri) throws IOException {
        return null;
    }

    @Override
    public Message newImageMessage(@NonNull File file) throws IOException {
        return null;
    }
}
