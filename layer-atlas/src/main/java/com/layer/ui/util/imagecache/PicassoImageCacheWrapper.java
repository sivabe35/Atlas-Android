package com.layer.ui.util.imagecache;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.widget.ImageView;

import com.layer.ui.util.Log;
import com.layer.ui.util.imagecache.transformations.CircleTransform;
import com.layer.ui.util.imagecache.transformations.RoundedTransform;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Target;

import java.util.HashSet;
import java.util.Set;

import static com.layer.ui.util.Log.TAG;
import static com.layer.ui.util.Log.VERBOSE;

public class PicassoImageCacheWrapper implements ImageCacheWrapper {
    protected final static CircleTransform SINGLE_TRANSFORM = new CircleTransform(TAG + ".single");
    protected final static CircleTransform MULTI_TRANSFORM = new CircleTransform(TAG + ".multi");

    protected final Picasso mPicasso;
    /*
        Picasso keeps a weak reference to the target when you load into a target,
        hence we need to keep a strong reference to the targets to prevent Garbage Collector from
        getting rid of the Targets.
     */
    private Set<Target> mTargets;

    public PicassoImageCacheWrapper(Picasso picasso) {
        mPicasso = picasso;
        mTargets = new HashSet<>();
    }

    @Override
    public void fetchBitmap(final BitmapWrapper bitmapWrapper, final Callback callback) {

        Target target = createTarget(bitmapWrapper, callback);
        boolean isMultiTransform = bitmapWrapper.hasMultiTransform();

        RequestCreator creator = mPicasso.load(bitmapWrapper.getUrl())
                .tag(bitmapWrapper.getId())
                .noPlaceholder()
                .noFade()
                .centerCrop()
                .resize(bitmapWrapper.getWidth(), bitmapWrapper.getHeight());
        creator.transform(isMultiTransform ? MULTI_TRANSFORM : SINGLE_TRANSFORM)
                .into(target);

        mTargets.add(target);
    }

    @VisibleForTesting
    public Target createTarget(final BitmapWrapper bitmapWrapper, final Callback callback) {
        return new Target() {

            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                bitmapWrapper.setBitmap(bitmap);
                callback.onSuccess();
                mTargets.remove(this);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                if (errorDrawable != null && Log.isLoggable(VERBOSE)) {
                    Log.v("onBitMapFailed :" + errorDrawable);
                }
                bitmapWrapper.setBitmap(null);
                callback.onFailure();
                mTargets.remove(this);
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
        };
    }

    public void cancelBitmap(BitmapWrapper bitmapWrapper) {
        if (bitmapWrapper != null) {
            mPicasso.cancelTag(bitmapWrapper.getId());
        }
    }

    @Override
    public void pauseTag(String picassoTag) {
        mPicasso.pauseTag(picassoTag);
    }

    @Override
    public void resumeTag(String picassoTag) {
        mPicasso.resumeTag(picassoTag);
    }

    @Override
    public void loadImage(final ImageRequestParameters imageRequestParameters, ImageView imageView) {
        RequestCreator requestCreator;

        if (imageRequestParameters.getUri() != null) {
            requestCreator = mPicasso.load(imageRequestParameters.getUri());
        } else if (imageRequestParameters.getUrl() != null) {
            requestCreator = mPicasso.load(imageRequestParameters.getUrl());
        } else {
            requestCreator = mPicasso.load(imageRequestParameters.getResourceId());
        }

        requestCreator.config(Bitmap.Config.RGB_565);

        if (imageRequestParameters.noFade()) {
            requestCreator.noFade();
        }

        if (imageRequestParameters.getTag() != null) {
            requestCreator.tag(imageRequestParameters.getTag());
        }

        if (imageRequestParameters.getPlaceholder() > 0) {
            requestCreator.placeholder(imageRequestParameters.getPlaceholder());
        }

        if (imageRequestParameters.centerCrop()) {
            requestCreator = requestCreator.centerCrop();
        } else if (imageRequestParameters.centerInside()) {
            requestCreator.centerInside();
        }

        if (imageRequestParameters.fit()) {
            requestCreator.fit();
        }

        if (imageRequestParameters.getTargetWidth() > 0 && imageRequestParameters.getTargetHeight() > 0) {
            requestCreator.resize(imageRequestParameters.getTargetWidth(), imageRequestParameters.getTargetHeight());
        }

        float rotationAngle = imageRequestParameters.getRotationDegrees() +
                imageRequestParameters.getExifRotationInDegrees(imageView.getContext());

        requestCreator.rotate(rotationAngle);

        if (imageRequestParameters.shouldScaleDown()) {
            requestCreator.onlyScaleDown();
        }

        if (imageRequestParameters.shouldApplyCirclularTransform()) {
            RoundedTransform transformation = new RoundedTransform();
            transformation.setCornerRadius(imageRequestParameters.getCornerRadius());
            transformation.setHasRoundTopCorners(imageRequestParameters.hasRoundedTopCorners());
            transformation.setHasRoundBottomCorners(imageRequestParameters.hasRoundedBottomCorners());

            requestCreator.transform(transformation);
        }

        final Callback callback = imageRequestParameters.getCallback();
        if (callback != null) {
            requestCreator.into(imageView, new com.squareup.picasso.Callback() {
                @Override
                public void onSuccess() {
                    callback.onSuccess();
                }

                @Override
                public void onError() {
                    callback.onFailure();
                }
            });
        } else {
            requestCreator.into(imageView);
        }
    }

    @Override
    public void loadDefaultPlaceholder(ImageView imageView) {
        String path = null;
        mPicasso.load(path).into(imageView);
    }

    private boolean isLocalContent(@NonNull Uri uri) {
        return uri != null && (uri.getScheme().equals("file") || uri.getScheme().equals("content"));
    }

}