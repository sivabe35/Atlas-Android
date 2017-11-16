package com.layer.ui.message.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.media.ExifInterface;

import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Message;
import com.layer.sdk.messaging.MessagePart;
import com.layer.ui.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

public class RichImageMessageComposer extends ImageMessageComposer {

    public RichImageMessageComposer(@NonNull Context context, @NonNull LayerClient layerClient) {
        super(context, layerClient);
    }

    @Override
    public Message newImageMessage(@NonNull Uri imageUri) throws IOException {
        InputStream inputStream = getContext().getContentResolver().openInputStream(imageUri);
        BitmapFactory.Options bounds = getBounds(inputStream);
        ExifInterface exifData = getExifData(inputStream);

        // Create Preview message part
        if (Log.isLoggable(Log.VERBOSE)) {
            Log.v("Creating Preview from " + imageUri.toString());
        }

        inputStream = getContext().getContentResolver().openInputStream(imageUri);
        MessagePart preview = buildPreviewPart(inputStream, bounds, exifData);

        // Create Source message part
        inputStream = getContext().getContentResolver().openInputStream(imageUri);
        long fileSize = getFileSizeFromUri(getContext(), imageUri);

        //MessagePart full = getLayerClient().newMessagePart(MIME_TYPE_IMAGE_JPEG, inputStream, fileSize);
        if (Log.isLoggable(Log.VERBOSE)) {
            Log.v(String.format(Locale.US, "Full image bytes: %d, preview bytes: %d, info bytes: %d", full.getSize(), preview.getSize(), info.getSize()));
        }

        return null;
    }

    @Override
    public Message newImageMessage(@NonNull File file) throws IOException {
        return null;
    }

    private MessagePart buildRootMessagePart() {
        ImageMessageMetadata metadata = new ImageMessageMetadata();

        return null;
    }

    private MessagePart buildPreviewPart(InputStream inputStream, BitmapFactory.Options bounds,
                                         ExifInterface exifData) throws IOException {
        Bitmap previewBitmap = getPreviewBitmap(bounds, inputStream);
        File temp = new File(getContext().getCacheDir(), getClass().getSimpleName() + "." + System.nanoTime() + ".jpg");
        FileOutputStream previewStream = new FileOutputStream(temp);

        if (Log.isLoggable(Log.VERBOSE)) {
            Log.v("Compressing preview to '" + temp.getAbsolutePath() + "'");
        }

        previewBitmap.compress(Bitmap.CompressFormat.JPEG, PREVIEW_COMPRESSION_QUALITY, previewStream);
        previewBitmap.recycle();
        previewStream.close();

        if (Log.isLoggable(Log.VERBOSE)) {
            Log.v("Exif orientation preserved in preview");
        }

        return null;
    }

    private MessagePart buildSourceMessagePart() {
        return null;
    }
}
