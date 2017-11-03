package com.layer.ui.util.imagecache.transformations;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Shader;

import com.squareup.picasso.Transformation;

public class RoundedTransform implements Transformation {
    private final Paint mPaint;
    private final PorterDuffXfermode mShapeXferMode;
    private final PorterDuffXfermode mBitmapXferMode;

    private float mCornerRadius = 0f;
    private boolean mRoundTopCorners;
    private boolean mRoundBottomCorners;

    public RoundedTransform() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mShapeXferMode = null;
        mBitmapXferMode = new PorterDuffXfermode((PorterDuff.Mode.SRC_IN));
        mPaint.setAntiAlias(true);
        mRoundTopCorners = true;
        mRoundBottomCorners = true;
    }

    public void setCornerRadius(float cornerRadius) {
        mCornerRadius = cornerRadius;
    }

    public void setHasRoundTopCorners(boolean roundTopCorners) {
        mRoundTopCorners = roundTopCorners;
    }

    public void setHasRoundBottomCorners(boolean roundBottomCorners) {
        mRoundBottomCorners = roundBottomCorners;
    }

    @Override
    public Bitmap transform(Bitmap source) {
        if (mCornerRadius == 0f) return source;
        if (source == null) return source;

        mPaint.setShader(new BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
        Bitmap output = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        if (mRoundTopCorners && mRoundBottomCorners) {
            // Uses native method to draw symmetric rounded corners
            canvas.drawRoundRect(new RectF(0, 0, source.getWidth(),
                    source.getHeight()), mCornerRadius, mCornerRadius, mPaint);
        } else {
            // Uses custom path to generate rounded corner individually
            canvas.drawPath(RoundedRect(0, 0, source.getWidth(),
                    source.getHeight(), mCornerRadius, mCornerRadius, mRoundTopCorners, mRoundTopCorners,
                    mRoundBottomCorners, mRoundBottomCorners), mPaint);
        }


        if (source != output) {
            source.recycle();
        }

        return output;
    }

    @Override
    public String key() {
        StringBuilder stringBuilder = new StringBuilder(this.getClass().getSimpleName());
        stringBuilder.append(" cornerRadius: ").append(mCornerRadius)
                .append(" roundTopCorners: ").append(mRoundTopCorners)
                .append(" roundBottomCorners: ").append(mRoundBottomCorners);

        return stringBuilder.toString();
    }

    /**
     * Prepares a path for rounded corner selectively.
     * <p>
     * Source taken from http://stackoverflow.com/a/35668889/6635889 <br/>
     * Usage:
     * <pre>
     *     Path path = RoundedRect(0, 0, fwidth , fheight , 5,5, false, true, true, false);
     *     canvas.drawPath(path, myPaint);
     * </pre>
     *
     * @param leftX The X coordinate of the left side of the rectangle
     * @param topY The Y coordinate of the top of the rectangle
     * @param rightX The X coordinate of the right side of the rectangle
     * @param bottomY The Y coordinate of the bottom of the rectangle
     * @param rx The x-radius of the oval used to round the corners
     * @param ry The y-radius of the oval used to round the corners
     * @param topLeft
     * @param topRight
     * @param bottomRight
     * @param bottomLeft
     * @return
     */
    public static Path RoundedRect(float leftX, float topY, float rightX, float bottomY, float rx,
                                   float ry, boolean topLeft, boolean topRight, boolean
                                           bottomRight, boolean bottomLeft) {
        Path path = new Path();
        if (rx < 0) rx = 0;
        if (ry < 0) ry = 0;
        float width = rightX - leftX;
        float height = bottomY - topY;
        if (rx > width / 2) rx = width / 2;
        if (ry > height / 2) ry = height / 2;
        float widthMinusCorners = (width - (2 * rx));
        float heightMinusCorners = (height - (2 * ry));

        path.moveTo(rightX, topY + ry);
        if (topRight)
            path.rQuadTo(0, -ry, -rx, -ry);//top-right corner
        else {
            path.rLineTo(0, -ry);
            path.rLineTo(-rx, 0);
        }
        path.rLineTo(-widthMinusCorners, 0);
        if (topLeft)
            path.rQuadTo(-rx, 0, -rx, ry); //top-left corner
        else {
            path.rLineTo(-rx, 0);
            path.rLineTo(0, ry);
        }
        path.rLineTo(0, heightMinusCorners);

        if (bottomLeft)
            path.rQuadTo(0, ry, rx, ry);//bottom-left corner
        else {
            path.rLineTo(0, ry);
            path.rLineTo(rx, 0);
        }

        path.rLineTo(widthMinusCorners, 0);
        if (bottomRight)
            path.rQuadTo(rx, 0, rx, -ry); //bottom-right corner
        else {
            path.rLineTo(rx, 0);
            path.rLineTo(0, -ry);
        }

        path.rLineTo(0, -heightMinusCorners);

        path.close();//Given close, last lineto can be removed.

        return path;
    }

}

