package com.layer.ui;

import android.content.Context;

import com.google.android.gms.tasks.RuntimeExecutionException;
import com.layer.sdk.LayerClient;
import com.layer.ui.avatar.AvatarContract;
import com.layer.ui.avatar.AvatarViewModel;
import com.layer.ui.util.ImageTransformImpl;
import com.layer.ui.util.picasso.ImageCacheWrapper;
import com.layer.ui.util.picasso.ImageCacheWrapperImpl;

public class Injection {
    private static ImageCacheWrapper sImageCacheWrapper;
    private static LayerClient sLayerClient;

    public static AvatarContract.ViewModel provideAvatarViewModel(Context context) {
        return sImageCacheWrapper != null ? new AvatarViewModel(sImageCacheWrapper)
                                     : new AvatarViewModel(provideImageCachingLibrary(context));
    }

    public static ImageCacheWrapper provideImageCachingLibrary(Context context) {
        if (sImageCacheWrapper == null) {
            if (sLayerClient == null) {
                throw new RuntimeExecutionException(new Throwable("Context or Layer Client is not set"));
            }
            sImageCacheWrapper = new ImageCacheWrapperImpl(context, sLayerClient, new ImageTransformImpl());
        }
        return sImageCacheWrapper;
    }

    public static void setLayerClient(LayerClient layerClient) {
        sLayerClient = layerClient;
    }

    public static ImageCacheWrapper.ImageTransform provideImageTransform() {
        return new ImageTransformImpl();
    }
}
