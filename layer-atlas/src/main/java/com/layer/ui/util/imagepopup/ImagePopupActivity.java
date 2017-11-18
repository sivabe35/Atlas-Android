package com.layer.ui.util.imagepopup;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.media.ExifInterface;
import android.support.v4.widget.ContentLoadingProgressBar;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.layer.sdk.LayerClient;
import com.layer.sdk.listeners.LayerProgressListener;
import com.layer.sdk.messaging.MessagePart;
import com.layer.ui.R;
import com.layer.ui.message.messagetypes.threepartimage.ThreePartImageCellFactory;
import com.layer.ui.message.messagetypes.threepartimage.ThreePartImageConstants;
import com.layer.ui.util.Log;

import java.io.Serializable;

/**
 * ImagePopupActivity implements a ful resolution image viewer Activity.  This Activity
 * registers with the LayerClient as a LayerProgressListener to monitor progress.
 */
public class ImagePopupActivity extends Activity implements LayerProgressListener.BackgroundThread.Weak, SubsamplingScaleImageView.OnImageEventListener {
    private static LayerClient sLayerClient;

    public static final String EXTRA_PARAMS = "extra_params";

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
        if (intent == null) {
            return;
        }
        if (intent.getExtras().containsKey(EXTRA_PARAMS)) {
            Parameters parameters = (Parameters) intent.getExtras().getSerializable(EXTRA_PARAMS);
            displayImage(parameters);
        } else {
            displayThreePartImage(intent);
        }

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

    private void displayImage(Parameters parameters) {
        mMessagePartId = Uri.parse(parameters.mSourceUri);
        mProgressBar.show();
        int orientation;
        if (parameters.mOrientation == SubsamplingScaleImageView.ORIENTATION_USE_EXIF) {
            orientation = parameters.mOrientation;
        } else {
            Uri uri = Uri.parse(parameters.mPreviewUri != null ? parameters.mPreviewUri : parameters.mSourceUri);
            switch (parameters.mOrientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    orientation = SubsamplingScaleImageView.ORIENTATION_90;
                    mImageView.setImage(
                            ImageSource.uri(mMessagePartId).dimensions(parameters.mWidth, parameters.mHeight),
                            ImageSource.uri(uri));
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    orientation = SubsamplingScaleImageView.ORIENTATION_180;
                    mImageView.setImage(
                            ImageSource.uri(mMessagePartId).dimensions(parameters.mHeight, parameters.mWidth),
                            ImageSource.uri(uri));
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    orientation = SubsamplingScaleImageView.ORIENTATION_270;
                    mImageView.setImage(
                            ImageSource.uri(mMessagePartId).dimensions(parameters.mHeight, parameters.mWidth),
                            ImageSource.uri(uri));
                    break;
                case ExifInterface.ORIENTATION_NORMAL:
                default:
                    orientation = SubsamplingScaleImageView.ORIENTATION_0;
                    mImageView.setImage(
                            ImageSource.uri(mMessagePartId).dimensions(parameters.mWidth, parameters.mHeight),
                            ImageSource.uri(uri));
                    break;
            }
        }

        mImageView.setOrientation(orientation);
        mImageView.setOnImageEventListener(this);
    }

    private void displayThreePartImage(Intent intent) {
        mMessagePartId = intent.getParcelableExtra("fullId");
        Uri previewId = intent.getParcelableExtra("previewId");
        ThreePartImageCellFactory.Info info = intent.getParcelableExtra("info");

        mProgressBar.show();
        if (previewId != null && info != null) {
            // ThreePartImage
            switch (info.orientation) {
                case ThreePartImageConstants.ORIENTATION_0:
                    mImageView.setOrientation(SubsamplingScaleImageView.ORIENTATION_0);
                    mImageView.setImage(
                            ImageSource.uri(mMessagePartId).dimensions(info.width, info.height),
                            ImageSource.uri(previewId));
                    break;
                case ThreePartImageConstants.ORIENTATION_90:
                    mImageView.setOrientation(SubsamplingScaleImageView.ORIENTATION_270);
                    mImageView.setImage(
                            ImageSource.uri(mMessagePartId).dimensions(info.height, info.width),
                            ImageSource.uri(previewId));
                    break;
                case ThreePartImageConstants.ORIENTATION_180:
                    mImageView.setOrientation(SubsamplingScaleImageView.ORIENTATION_180);
                    mImageView.setImage(
                            ImageSource.uri(mMessagePartId).dimensions(info.width, info.height),
                            ImageSource.uri(previewId));
                    break;
                case ThreePartImageConstants.ORIENTATION_270:
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

    public static class Parameters implements Serializable {
        private String mSourceUri;
        private String mPreviewUri;
        private int mWidth;
        private int mHeight;
        private int mOrientation = SubsamplingScaleImageView.ORIENTATION_USE_EXIF;

        public Parameters() {
        }

        public Parameters source(Uri uri) {
            mSourceUri = uri.toString();

            return this;
        }

        public Parameters preview(Uri uri) {
            mPreviewUri = uri.toString();

            return this;
        }

        public Parameters source(String url) {
            mSourceUri = url;

            return this;
        }

        public Parameters preview(String url) {
            mPreviewUri = url;

            return this;
        }

        public Parameters dimensions(int height, int width) {
            mHeight = height;
            mWidth = width;

            return this;
        }

        public Parameters orientation(int orientation) {
            mOrientation = orientation;

            return this;
        }
    }
}
