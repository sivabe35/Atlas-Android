package com.layer.ui.util.imagepopup;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;

import com.davemorrissey.labs.subscaleview.decoder.ImageRegionDecoder;
import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.MessagePart;
import com.layer.ui.util.Log;
import com.layer.ui.util.Util;

import java.util.concurrent.TimeUnit;

public class MessagePartRegionDecoder implements ImageRegionDecoder {
    private final Object mLock = new Object();
    private BitmapRegionDecoder mDecoder;
    private static LayerClient sLayerClient;
    private MessagePart mMessagePart;

    public static void init(LayerClient layerClient) {
        sLayerClient = layerClient;
    }

    @Override
    public Point init(Context context, Uri messagePartId) throws Exception {
        MessagePart part = Util.getMessagePartBlocking(sLayerClient, messagePartId);
        if (part == null) {
            if (Log.isLoggable(Log.ERROR)) {
                Log.e("No message part with ID: " + messagePartId);
            }
            return null;
        }
        if (part.getMessage().isDeleted()) {
            if (Log.isLoggable(Log.ERROR)) {
                Log.e("Message part is deleted: " + messagePartId);
            }
            return null;
        }

        mMessagePart = part;
        if (!Util.downloadMessagePart(sLayerClient, mMessagePart, 3, TimeUnit.MINUTES)) {
            if (Log.isLoggable(Log.ERROR)) {
                Log.e("Timed out while downloading: " + messagePartId);
            }
            return null;
        }

        synchronized (mLock) {
            mDecoder = BitmapRegionDecoder.newInstance(mMessagePart.getDataStream(), false);
            return new Point(mDecoder.getWidth(), mDecoder.getHeight());
        }
    }

    @Override
    public Bitmap decodeRegion(Rect rect, int sampleSize) {
        synchronized (mLock) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = sampleSize;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = mDecoder.decodeRegion(rect, options);
            if (bitmap == null) throw new IllegalStateException("Could not decode bitmap region");
            return bitmap;
        }
    }

    @Override
    public boolean isReady() {
        return mDecoder != null && !mDecoder.isRecycled() && mMessagePart.isContentReady();
    }

    @Override
    public void recycle() {
        mDecoder.recycle();
    }
}
