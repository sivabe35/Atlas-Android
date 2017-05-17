package com.layer.ui.avatar;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.layer.ui.util.picasso.ImageCacheWrapper;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.concurrent.atomic.AtomicLong;

public class UiImageTarget implements Target, ImageCacheWrapper.ImageTarget {
    private final static AtomicLong sCounter = new AtomicLong(0);
    private final long mId;
    private final AvatarView mCluster;
    private String mUrl;
    private Bitmap mBitmap;

    public UiImageTarget(AvatarView cluster) {
        mId = sCounter.incrementAndGet();
        mCluster = cluster;
    }

    public UiImageTarget setUrl(String url) {
        mUrl = url;
        return this;
    }

    public String getUrl() {
        return mUrl;
    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        mCluster.invalidate();
        mBitmap = bitmap;
    }

    @Override
    public void onBitmapFailed(Drawable errorDrawable) {
        mCluster.invalidate();
        mBitmap = null;
    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {
        mBitmap = null;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UiImageTarget target = (UiImageTarget) o;
        return mId == target.mId;
    }

    @Override
    public int hashCode() {
        return (int) (mId ^ (mId >>> 32));
    }
}
