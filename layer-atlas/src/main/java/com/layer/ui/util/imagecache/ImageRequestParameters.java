package com.layer.ui.util.imagecache;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Dimension;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Px;
import android.support.media.ExifInterface;

import java.io.IOException;

import static android.content.ContentResolver.SCHEME_CONTENT;
import static android.content.ContentResolver.SCHEME_FILE;
import static android.support.media.ExifInterface.ORIENTATION_NORMAL;
import static android.support.media.ExifInterface.TAG_ORIENTATION;

public class ImageRequestParameters {

    private static final String[] CONTENT_ORIENTATION = new String[]{
            MediaStore.Images.ImageColumns.ORIENTATION
    };

    private final ImageCacheWrapper.Callback mCallBack;

    @DrawableRes
    private int mResourceId;
    @DrawableRes
    private int mPlaceholder;

    private Uri mUri;
    private String mUrl;
    private String mTag;
    private boolean mCenterCrop;
    private boolean mCenterInside;
    private boolean mFit;

    @Px
    private int mTargetWidth;
    @Px
    private int mTargetHeight;

    private boolean mApplyCircularTransform;
    @Dimension
    private float mCornerRadius;

    private boolean mNoFade;
    private boolean mRoundedTopCorners;
    private boolean mRoundedBottomCorners;
    private boolean mOnlyScaleDown;

    private float mRotationDegrees;
    private int mExifOrientation;

    public ImageRequestParameters(Builder builder) {
        mUri = builder.mUri;
        mUrl = builder.mUrl;
        mTag = builder.mTag;
        mResourceId = builder.mResourceId;
        mPlaceholder = builder.mPlaceholder;

        mTargetWidth = builder.mTargetWidth;
        mTargetHeight = builder.mTargetHeight;
        mRotationDegrees = builder.mRotationDegrees;
        mExifOrientation = builder.mExifOrientation;
        mOnlyScaleDown = builder.mOnlyScaleDown;
        mCenterCrop = builder.mCenterCrop;
        mCenterInside = builder.mCenterInside;
        mFit = builder.mFit;

        mApplyCircularTransform = builder.mApplyCircularTransform;
        mCornerRadius = builder.mCornerRadius;
        mRoundedTopCorners = builder.mRoundedTopCorners;
        mRoundedBottomCorners = builder.mRoundedBottomCorners;
        mNoFade = builder.mNoFade;

        mCallBack = builder.mCallBack;
    }

    public Uri getUri() {
        return mUri;
    }

    public String getUrl() {
        return mUrl;
    }

    @DrawableRes
    public int getResourceId() {
        return mResourceId;
    }

    @Nullable
    public String getTag() {
        return mTag;
    }

    @DrawableRes
    public int getPlaceholder() {
        return mPlaceholder;
    }

    public boolean centerCrop() {
        return mCenterCrop;
    }

    public boolean centerInside() {
        return mCenterInside;
    }

    public boolean fit() {
        return mFit;
    }

    @Px
    public int getTargetWidth() {
        return mTargetWidth;
    }

    @Px
    public int getTargetHeight() {
        return mTargetHeight;
    }

    public boolean shouldApplyCirclularTransform() {
        return mApplyCircularTransform;
    }

    public float getCornerRadius() {
        return mCornerRadius;
    }

    public boolean hasRoundedTopCorners() {
        return mRoundedTopCorners;
    }

    public boolean hasRoundedBottomCorners() {
        return mRoundedBottomCorners;
    }

    @Nullable
    public ImageCacheWrapper.Callback getCallback() {
        return mCallBack;
    }

    public float getRotationDegrees() {
        return mRotationDegrees;
    }

    public int getExifOrientation() {
        return mExifOrientation;
    }

    public float getExifRotationInDegrees(Context context) {
        int exifOrientation = 0;
        if (mUri != null) {
            if (SCHEME_FILE.equals(mUri.getScheme())) {
                exifOrientation = getFileExifRotation(mUri);
            } else if (SCHEME_CONTENT.equals(mUri.getScheme())) {
                exifOrientation = getExifOrientationFromContentUri(context, mUri);
            }
        } else if (mResourceId > 0) {
            exifOrientation = 0;
        } else {
            exifOrientation = mExifOrientation;
        }

        switch (exifOrientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return 90.0f;
            case ExifInterface.ORIENTATION_ROTATE_180:
                return 180.0f;
            case ExifInterface.ORIENTATION_ROTATE_270:
                return 270.0f;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
            case ExifInterface.ORIENTATION_TRANSPOSE:
            case ExifInterface.ORIENTATION_TRANSVERSE:
            case ExifInterface.ORIENTATION_NORMAL:
            case ExifInterface.ORIENTATION_UNDEFINED:
            default:
                return 0.0f;
        }
    }

    private int getFileExifRotation(Uri uri) {
        try {
            ExifInterface exifInterface = new ExifInterface(uri.getPath());
            return exifInterface.getAttributeInt(TAG_ORIENTATION, ORIENTATION_NORMAL);
        } catch (IOException ignored) {
            // assume no rotation
            return 0;
        }
    }

    private int getExifOrientationFromContentUri(Context context, Uri uri) {
        Cursor cursor = null;
        try {
            ContentResolver contentResolver = context.getContentResolver();
            cursor = contentResolver.query(uri, CONTENT_ORIENTATION, null, null, null);
            if (cursor == null || !cursor.moveToFirst()) {
                return 0;
            }
            return cursor.getInt(0);
        } catch (RuntimeException ignored) {
            // If the orientation column doesn't exist, assume no rotation.
            return 0;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public boolean shouldScaleDown() {
        return mOnlyScaleDown;
    }

    public boolean noFade() {
        return mNoFade;
    }

    public static class Builder {
        private String mTag;

        private ImageCacheWrapper.Callback mCallBack;

        private Uri mUri;
        private String mUrl;
        @DrawableRes
        private int mResourceId;
        @DrawableRes
        private int mPlaceholder;

        private boolean mFit;
        private boolean mCenterInside;
        private boolean mCenterCrop;
        private boolean mApplyCircularTransform;
        private boolean mOnlyScaleDown;

        private float mRotationDegrees = 0.0f;
        private int mExifOrientation;

        @Px
        private int mTargetWidth;
        @Px
        private int mTargetHeight;

        @Dimension
        private float mCornerRadius;
        private boolean mRoundedTopCorners = false;
        private boolean mRoundedBottomCorners = false;
        private boolean mNoFade;

        public Builder() {

        }

        public Builder(@NonNull Uri uri) {
            uri(uri);
        }

        public Builder(@NonNull String url) {
            url(url);
        }

        public Builder(@DrawableRes int resourceId) {
            resourceId(resourceId);
        }

        public Builder uri(@NonNull Uri uri) {
            if (uri == null) {
                throw new IllegalArgumentException("Image URI may not be null.");
            }
            mUri = uri;

            // Clear any resource id or url that may be set
            mResourceId = 0;
            mUrl = null;
            return this;
        }

        public Builder url(@NonNull String url) {
            if (url == null || url.isEmpty()) {
                throw new IllegalArgumentException("Image url may not be null.");
            }
            mUrl = url;

            // Clear any resource id or uri that may be set
            mResourceId = 0;
            mUri = null;
            return this;
        }

        public Builder resourceId(@DrawableRes int resourceId) {
            if (resourceId == 0) {
                throw new IllegalArgumentException("Image resource ID may not be 0.");
            }
            mResourceId = resourceId;

            // Clear any uri or url that may have been set
            mUri = null;
            mUrl = null;
            return this;
        }

        public Builder callback(@Nullable ImageCacheWrapper.Callback callback) {
            mCallBack = callback;
            return this;
        }

        public Builder placeHolder(@DrawableRes int placeholder) {
            mPlaceholder = placeholder;
            return this;
        }

        public Builder resize(@Px int targetWidth, @Px int targetHeight) {
            if (targetWidth < 0) {
                throw new IllegalArgumentException("Width must be positive number or 0.");
            }
            if (targetHeight < 0) {
                throw new IllegalArgumentException("Height must be positive number or 0.");
            }
            if (targetHeight == 0 && targetWidth == 0) {
                throw new IllegalArgumentException("At least one dimension has to be positive number.");
            }

            mTargetWidth = targetWidth;
            mTargetHeight = targetHeight;

            return this;
        }

        public Builder tag(@Nullable String tag) {
            mTag = tag;
            return this;
        }

        public Builder centerInside(boolean centerInside) {
            mCenterInside = centerInside;
            return this;
        }

        public Builder fit(boolean fit) {
            mFit = fit;
            return this;
        }

        public Builder centerCrop(boolean centerCrop) {
            mCenterCrop = centerCrop;
            return this;
        }

        public Builder roundedTopCorners(boolean roundedTopCorners) {
            mRoundedTopCorners = roundedTopCorners;
            return this;
        }

        public Builder roundedBottomCorners(boolean roundedBottomCorners) {
            mRoundedBottomCorners = roundedBottomCorners;
            return this;
        }

        public Builder noFade(boolean noFade) {
            mNoFade = noFade;
            return this;
        }

        public Builder circularTransform(float cornerRadius) {
            mApplyCircularTransform = true;
            mCornerRadius = cornerRadius;
            return this;
        }

        public Builder defaultCircularTransform(boolean applyCircularTransform) {
            mApplyCircularTransform = applyCircularTransform;
            roundedBottomCorners(applyCircularTransform);
            roundedTopCorners(applyCircularTransform);
            return this;
        }

        public Builder rotate(float rotationDegrees) {
            mRotationDegrees = rotationDegrees;
            return this;
        }

        public Builder exifOrientation(int exifOrientation) {
            mExifOrientation = exifOrientation;
            return this;
        }

        public Builder onlyScaleDown(boolean onlyScaleDown) {
            mOnlyScaleDown = onlyScaleDown;
            return this;
        }

        public ImageRequestParameters build() {
            if (mCenterInside && mCenterCrop) {
                throw new IllegalStateException("Center crop and center inside can not be used together.");
            }

            if (mCenterCrop && (mTargetWidth == 0 && mTargetHeight == 0)) {
                throw new IllegalStateException(
                        "Center crop requires calling resize with positive width and height.");
            }
            if (mCenterInside && (mTargetWidth == 0 && mTargetHeight == 0)) {
                throw new IllegalStateException(
                        "Center inside requires calling resize with positive width and height.");
            }


            return new ImageRequestParameters(this);
        }
    }
}
