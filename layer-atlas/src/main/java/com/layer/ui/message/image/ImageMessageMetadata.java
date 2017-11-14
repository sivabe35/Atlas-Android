package com.layer.ui.message.image;

import android.databinding.BaseObservable;
import android.support.annotation.Dimension;
import android.support.annotation.Nullable;

import com.layer.ui.util.display.DisplayUtils;

import java.util.Map;

public class ImageMessageMetadata extends BaseObservable {
    private String mTitle;
    private String mArtist;
    private String mSubtitle;

    private String mFileName;
    private String mMimeType;

    @Dimension
    private int mWidth;
    @Dimension
    private int mHeight;
    @Dimension
    private int mPreviewWidth;
    @Dimension
    private int mPreviewHeight;

    private String mSourceUrl;
    private String mPreviewUrl;
    private int mOrientation;

    private Map<String, String> mActionData;
    private String mAction;

    @Nullable
    public String getTitle() {
        return mTitle;
    }

    @Nullable
    public String getArtist() {
        return mArtist;
    }

    @Nullable
    public String getSubtitle() {
        return mSubtitle;
    }

    @Nullable
    public String getFileName() {
        return mFileName;
    }

    public String getMimeType() {
        return mMimeType;
    }

    @Dimension
    public int getWidth() {
        return DisplayUtils.dpToPx(mWidth);
    }

    @Dimension
    public int getHeight() {
        return DisplayUtils.dpToPx(mHeight);
    }

    @Nullable
    public String getSourceUrl() {
        return mSourceUrl;
    }

    @Nullable
    public String getPreviewUrl() {
        return mPreviewUrl;
    }

    @Dimension
    public int getPreviewWidth() {
        return DisplayUtils.dpToPx(mPreviewWidth > 0 ? mPreviewWidth : mWidth);
    }

    @Dimension
    public int getPreviewHeight() {
        return DisplayUtils.dpToPx(mPreviewHeight > 0 ? mPreviewHeight : mHeight);
    }

    public int getOrientation() {
        return mOrientation;
    }
}
