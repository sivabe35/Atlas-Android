package com.layer.ui.util.imagepopup;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.ContentLoadingProgressBar;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.layer.ui.R;
import com.layer.ui.messagetypes.threepartimage.ThreePartImageCellFactory;
import com.layer.ui.messagetypes.threepartimage.ThreePartImageUtils;
import com.layer.ui.util.Log;
import com.layer.sdk.LayerClient;
import com.layer.sdk.listeners.LayerProgressListener;
import com.layer.sdk.messaging.MessagePart;

/**
 * ImagePopupActivity implements a ful resolution image viewer Activity.  This Activity
 * registers with the LayerClient as a LayerProgressListener to monitor progress.
 */
public class ImagePopupActivity extends Activity implements LayerProgressListener.BackgroundThread.Weak, SubsamplingScaleImageView.OnImageEventListener {
    private static LayerClient sLayerClient;

    private SubsamplingScaleImageView mImageView;
    private ContentLoadingProgressBar mProgressBar;
    private Uri mMessagePartId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setBackgroundDrawableResource(R.color.layer_ui_image_popup_background);
        setContentView(R.layout.ui_image_popup);
        mImageView = (SubsamplingScaleImageView) findViewById(R.id.image_popup);
        mProgressBar = (ContentLoadingProgressBar) findViewById(R.id.image_popup_progress);

        mImageView.setPanEnabled(true);
        mImageView.setZoomEnabled(true);
        mImageView.setDoubleTapZoomDpi(160);
        mImageView.setMinimumDpi(80);
        mImageView.setBitmapDecoderClass(MessagePartDecoder.class);
        mImageView.setRegionDecoderClass(MessagePartRegionDecoder.class);

        Intent intent = getIntent();
        if (intent == null) return;
        mMessagePartId = intent.getParcelableExtra("fullId");
        Uri previewId = intent.getParcelableExtra("previewId");
        ThreePartImageCellFactory.Info info = intent.getParcelableExtra("info");

        mProgressBar.show();
        if (previewId != null && info != null) {
            // ThreePartImage
            switch (info.orientation) {
                case ThreePartImageUtils.ORIENTATION_0:
                    mImageView.setOrientation(SubsamplingScaleImageView.ORIENTATION_0);
                    mImageView.setImage(
                            ImageSource.uri(mMessagePartId).dimensions(info.width, info.height),
                            ImageSource.uri(previewId));
                    break;
                case ThreePartImageUtils.ORIENTATION_90:
                    mImageView.setOrientation(SubsamplingScaleImageView.ORIENTATION_270);
                    mImageView.setImage(
                            ImageSource.uri(mMessagePartId).dimensions(info.height, info.width),
                            ImageSource.uri(previewId));
                    break;
                case ThreePartImageUtils.ORIENTATION_180:
                    mImageView.setOrientation(SubsamplingScaleImageView.ORIENTATION_180);
                    mImageView.setImage(
                            ImageSource.uri(mMessagePartId).dimensions(info.width, info.height),
                            ImageSource.uri(previewId));
                    break;
                case ThreePartImageUtils.ORIENTATION_270:
                    mImageView.setOrientation(SubsamplingScaleImageView.ORIENTATION_90);
                    mImageView.setImage(
                            ImageSource.uri(mMessagePartId).dimensions(info.height, info.width),
                            ImageSource.uri(previewId));
                    break;
            }
        } else {
            // SinglePartImage
            mImageView.setImage(ImageSource.uri(mMessagePartId));
        }
        mImageView.setOnImageEventListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sLayerClient.registerProgressListener(null, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sLayerClient.unregisterProgressListener(null, this);
    }

    public static void init(LayerClient layerClient) {
        sLayerClient = layerClient;
        MessagePartDecoder.init(layerClient);
        MessagePartRegionDecoder.init(layerClient);
    }


    //==============================================================================================
    // SubsamplingScaleImageView.OnImageEventListener: hide progress bar when full part loaded
    //==============================================================================================

    @Override
    public void onReady() {

    }

    @Override
    public void onImageLoaded() {
        mProgressBar.hide();
    }

    @Override
    public void onPreviewLoadError(Exception e) {
        if (Log.isLoggable(Log.ERROR)) Log.e(e.getMessage(), e);
        mProgressBar.hide();
    }

    @Override
    public void onImageLoadError(Exception e) {
        if (Log.isLoggable(Log.ERROR)) Log.e(e.getMessage(), e);
        mProgressBar.hide();
    }

    @Override
    public void onTileLoadError(Exception e) {
        if (Log.isLoggable(Log.ERROR)) Log.e(e.getMessage(), e);
        mProgressBar.hide();
    }


    //==============================================================================================
    // LayerProgressListener: update progress bar while downloading
    //==============================================================================================

    @Override
    public void onProgressStart(MessagePart messagePart, Operation operation) {
        if (!messagePart.getId().equals(mMessagePartId)) return;
        mProgressBar.setProgress(0);
    }

    @Override
    public void onProgressUpdate(MessagePart messagePart, Operation operation, long bytes) {
        if (!messagePart.getId().equals(mMessagePartId)) return;
        double fraction = (double) bytes / (double) messagePart.getSize();
        int progress = (int) Math.round(fraction * mProgressBar.getMax());
        mProgressBar.setProgress(progress);
    }

    @Override
    public void onProgressComplete(MessagePart messagePart, Operation operation) {
        if (!messagePart.getId().equals(mMessagePartId)) return;
        mProgressBar.setProgress(mProgressBar.getMax());
    }

    @Override
    public void onProgressError(MessagePart messagePart, Operation operation, Throwable e) {
        if (!messagePart.getId().equals(mMessagePartId)) return;
        if (Log.isLoggable(Log.ERROR)) Log.e(e.getMessage(), e);
    }

}
