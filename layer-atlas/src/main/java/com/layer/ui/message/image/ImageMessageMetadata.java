package com.layer.ui.message.image;

import android.databinding.BaseObservable;
import android.support.annotation.Dimension;
import android.support.annotation.Nullable;

import com.layer.ui.message.model.Action;
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

    private Action mAction;

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

    public Action getAction() {
        return mAction;
    }

    public int getOrientation() {
        return mOrientation;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public void setArtist(String artist) {
        mArtist = artist;
    }

    public void setSubtitle(String subtitle) {
        mSubtitle = subtitle;
    }

    public void setFileName(String fileName) {
        mFileName = fileName;
    }

    public void setMimeType(String mimeType) {
        mMimeType = mimeType;
    }

    public void setWidth(int width) {
        mWidth = width;
    }

    public void setHeight(int height) {
        mHeight = height;
    }

    public void setPreviewWidth(int previewWidth) {
        mPreviewWidth = previewWidth;
    }

    public void setPreviewHeight(int previewHeight) {
        mPreviewHeight = previewHeight;
    }

    public void setSourceUrl(String sourceUrl) {
        mSourceUrl = sourceUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        mPreviewUrl = previewUrl;
    }

    public void setOrientation(int orientation) {
        mOrientation = orientation;
    }

    public void setAction(Action action) {
        mAction = action;
    }
}
