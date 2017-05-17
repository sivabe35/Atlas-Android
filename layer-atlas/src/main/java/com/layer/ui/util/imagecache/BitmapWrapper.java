package com.layer.ui.util.imagecache;

import android.graphics.Bitmap;

public class BitmapWrapper {
    private Bitmap mBitmap;
    private String mUrl;

    public BitmapWrapper(String url) {
        mUrl = url;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public BitmapWrapper setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
        return this;
    }
}
