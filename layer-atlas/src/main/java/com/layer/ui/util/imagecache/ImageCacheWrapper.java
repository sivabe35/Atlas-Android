package com.layer.ui.util.imagecache;

import android.graphics.Bitmap;
import android.widget.ImageView;

public interface ImageCacheWrapper {
    void load(String targetUrl, String tag, int width, int height, ImageView imageView, Object... args);

    void fetchBitmap(String url, Object tag, int width, int height, final Callback callback, Object... args);

    interface Callback {
        void onSuccess(Bitmap bitmap);

        void onFailure();

        void onPrepareLoad();
    }

    void cancelRequest(ImageView imageView);
    void cancelRequest(Object tag);

}
