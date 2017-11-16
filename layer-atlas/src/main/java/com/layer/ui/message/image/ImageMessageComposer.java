package com.layer.ui.message.image;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.media.ExifInterface;

import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Message;
import com.layer.ui.util.Log;
import com.layer.ui.util.Util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class ImageMessageComposer {

    public static final int PREVIEW_COMPRESSION_QUALITY = 75;
    public static final int PREVIEW_MAX_WIDTH = 512;
    public static final int PREVIEW_MAX_HEIGHT = 512;

    private Context mContext;
    private LayerClient mLayerClient;

    public ImageMessageComposer(@NonNull Context context, @NonNull LayerClient layerClient) {
        mContext = context;
        mLayerClient = layerClient;
    }

    public abstract Message newImageMessage(@NonNull Uri imageUri) throws IOException;

    public abstract Message newImageMessage(@NonNull File file) throws IOException;

    public Context getContext() {
        return mContext;
    }

    public LayerClient getLayerClient() {
        return mLayerClient;
    }

    protected String getPath(Context context, Uri uri) {
        Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.Media.DATA}, null, null, null);

        try {
            // Images in the MediaStore
            if (cursor != null) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                return cursor.getString(columnIndex);
            } else {
                // Fallback to available path in the Uri
                return uri.getPath();
            }
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    protected ExifInterface getExifData(File imageFile) throws IOException {
        if (imageFile == null) throw new IllegalArgumentException("Null image file");
        if (!imageFile.exists()) throw new IllegalArgumentException("Image file does not exist");
        if (!imageFile.canRead()) throw new IllegalArgumentException("Cannot read image file");
        if (imageFile.length() <= 0) throw new IllegalArgumentException("Image file is empty");

        try {
            ExifInterface exifInterface = new ExifInterface(imageFile.getAbsolutePath());
            return exifInterface;
        } catch (IOException e) {
            if (Log.isLoggable(Log.ERROR)) {
                Log.e(e.getMessage(), e);
            }
            throw e;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    protected ExifInterface getExifData(@NonNull InputStream inputStream) throws IOException {
        try {
            ExifInterface exifInterface = new ExifInterface(inputStream);
            return exifInterface;
        } catch (IOException e) {
            if (Log.isLoggable(Log.ERROR)) {
                Log.e(e.getMessage(), e);
            }
            throw e;
        }
    }

    protected BitmapFactory.Options getBounds(InputStream inputStream) {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(inputStream, null, bounds);

        return bounds;
    }

    protected Bitmap getPreviewBitmap(BitmapFactory.Options bounds, InputStream inputStream) {
        // Determine preview size
        int[] previewDimensions = Util.scaleDownInside(bounds.outWidth, bounds.outHeight, PREVIEW_MAX_WIDTH, PREVIEW_MAX_HEIGHT);
        if (Log.isLoggable(Log.VERBOSE)) {
            Log.v("Preview size: " + previewDimensions[0] + "x" + previewDimensions[1]);
        }

        // Determine sample size for preview
        int sampleSize = 1;
        int sampleWidth = bounds.outWidth;
        int sampleHeight = bounds.outHeight;
        while (sampleWidth > previewDimensions[0] && sampleHeight > previewDimensions[1]) {
            sampleWidth >>= 1;
            sampleHeight >>= 1;
            sampleSize <<= 1;
        }
        if (sampleSize != 1) sampleSize >>= 1; // Back off 1 for scale-down instead of scale-up

        BitmapFactory.Options previewOptions = new BitmapFactory.Options();
        previewOptions.inSampleSize = sampleSize;

        if (Log.isLoggable(Log.VERBOSE)) {
            Log.v("Preview sampled size: " + (sampleWidth << 1) + "x" + (sampleHeight << 1));
        }

        // Create previewBitmap if sample size and preview size are different
        Bitmap sampledBitmap = BitmapFactory.decodeStream(inputStream, null, previewOptions);
        if (previewDimensions[0] != sampleWidth && previewDimensions[1] != sampleHeight) {
            Bitmap previewBitmap = Bitmap.createScaledBitmap(sampledBitmap, previewDimensions[0], previewDimensions[1], true);
            sampledBitmap.recycle();
            return previewBitmap;
        } else {
            return sampledBitmap;
        }
    }

    protected long getFileSizeFromUri(Context context, Uri uri) {
        Cursor cursor = context.getContentResolver().query(uri,
                null, null, null, null);
        cursor.moveToFirst();
        long size = cursor.getLong(cursor.getColumnIndex(OpenableColumns.SIZE));
        cursor.close();

        return size;
    }

    protected void writeStreamToFile(String filePath, InputStream inputStream) throws IOException {
        OutputStream stream = new BufferedOutputStream(new FileOutputStream(filePath));
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            stream.write(buffer, 0, len);
        }
        if (stream != null)
            stream.close();
    }
}
