package com.layer.ui.util.imagecache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.VisibleForTesting;
import android.widget.ImageView;

import com.layer.ui.util.Log;
import com.layer.ui.util.imagecache.transformations.CircleTransform;
import com.layer.ui.util.imagecache.transformations.RoundedTransform;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Target;
import com.squareup.picasso.Transformation;

import java.util.HashSet;
import java.util.Set;

import static com.layer.ui.util.Log.TAG;
import static com.layer.ui.util.Log.VERBOSE;

public class PicassoImageCacheWrapper implements ImageCacheWrapper {
    protected final static CircleTransform SINGLE_TRANSFORM = new CircleTransform(TAG + ".single");
    protected final static CircleTransform MULTI_TRANSFORM = new CircleTransform(TAG + ".multi");
    protected final Picasso mPicasso;
    protected Transformation mTransform;
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
    public void loadImage(final ImageRequestParameters imageRequestParameters,
                          ImageView imageView) {
        RequestCreator requestCreator = mPicasso.load(imageRequestParameters.getUri())
                .tag(imageRequestParameters.getTag())
                .placeholder(imageRequestParameters.getPlaceholder());

        if (imageRequestParameters.isShouldCenterImage()) {
            requestCreator = requestCreator.centerCrop();
        }

        if (imageRequestParameters.getRotateAngleTo() == 0) {
            requestCreator.resize(imageRequestParameters.getResizeWidthTo(),
                    imageRequestParameters.getResizeHeightTo());
        } else {
            requestCreator.resize(imageRequestParameters.getResizeWidthTo(),
                    imageRequestParameters.getResizeHeightTo()).rotate(
                    imageRequestParameters.getRotateAngleTo());
        }

        if (imageRequestParameters.shouldScaleDownTo()) {
            requestCreator.onlyScaleDown();
        }

        if (imageRequestParameters.shouldTransformIntoRound()) {
            requestCreator.transform(getTransform(imageView.getContext()));
        }

        requestCreator.into(imageView, new com.squareup.picasso.Callback() {
            @Override
            public void onSuccess() {
                imageRequestParameters.getCallback().onSuccess();
            }

            @Override
            public void onError() {
                imageRequestParameters.getCallback().onFailure();
            }
        });
    }


    //==============================================================================================
    // private methods
    //==============================================================================================

    private Transformation getTransform(Context context) {
        if (mTransform == null) {
            float radius = context.getResources().getDimension(
                    com.layer.ui.R.dimen.layer_ui_message_item_cell_radius);
            mTransform = new RoundedTransform(radius);
        }
        return mTransform;
    }
}