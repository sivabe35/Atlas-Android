package com.layer.ui.util;

import static com.layer.ui.util.Log.TAG;

import com.layer.ui.util.picasso.ImageCacheWrapper;
import com.layer.ui.util.picasso.transformations.CircleTransform;
import com.squareup.picasso.Transformation;

public class ImageTransformImpl implements ImageCacheWrapper.ImageTransform {
    private final static CircleTransform SINGLE_TRANSFORM = new CircleTransform(TAG + ".single");
    private final static CircleTransform MULTI_TRANSFORM = new CircleTransform(TAG + ".multi");

    @Override
    public Transformation getTransformation(boolean isTrue) {
        return isTrue ? MULTI_TRANSFORM : SINGLE_TRANSFORM;
    }
}
