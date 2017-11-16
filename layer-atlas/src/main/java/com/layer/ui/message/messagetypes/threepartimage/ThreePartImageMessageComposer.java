package com.layer.ui.message.messagetypes.threepartimage;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Message;
import com.layer.ui.message.image.ImageMessageComposer;

import java.io.File;
import java.io.IOException;

public class ThreePartImageMessageComposer extends ImageMessageComposer {
    public ThreePartImageMessageComposer(@NonNull Context context, @NonNull LayerClient layerClient) {
        super(context, layerClient);
    }

    @Override
    public Message newImageMessage(@NonNull Uri imageUri) throws IOException {
        return ThreePartImageUtils.newThreePartImageMessage(getContext(), getLayerClient(), imageUri);
    }

    @Override
    public Message newImageMessage(@NonNull File file) throws IOException {
        return ThreePartImageUtils.newThreePartImageMessage(getContext(), getLayerClient(), file);
    }
}
