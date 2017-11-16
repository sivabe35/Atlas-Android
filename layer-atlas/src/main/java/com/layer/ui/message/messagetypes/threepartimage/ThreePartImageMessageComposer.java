package com.layer.ui.message.messagetypes.threepartimage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.media.ExifInterface;

import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Message;
import com.layer.sdk.messaging.MessagePart;
import com.layer.ui.message.image.ImageMessageComposer;
import com.layer.ui.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Locale;

public class ThreePartImageMessageComposer extends ImageMessageComposer {
    public static final int ORIENTATION_0 = 0;
    public static final int ORIENTATION_180 = 1;
    public static final int ORIENTATION_90 = 2;
    public static final int ORIENTATION_270 = 3;

    public static final int PART_INDEX_FULL = 0;
    public static final int PART_INDEX_PREVIEW = 1;
    public static final int PART_INDEX_INFO = 2;

    public static final String MIME_TYPE_PREVIEW = "image/jpeg+preview";
    public static final String MIME_TYPE_INFO = "application/json+imageSize";

    public static final String MIME_TYPE_FILTER_IMAGE = "image/*";
    public static final String MIME_TYPE_IMAGE_JPEG = "image/jpeg";

    public ThreePartImageMessageComposer(@NonNull Context context, @NonNull LayerClient layerClient) {
        super(context, layerClient);
    }

    /**
     * The methods by which the ThreePartImageMessage is constructed differs by Android API versions
     * since the way in which Android handles files from ContentProviders has changed across versions
     * <p>
     * Nougat: Makes some changes to how files are handled.  It is no longer allowed to attach a "file://"
     * URI to an intent any longer.
     * <p>
     * Kitkat - Marshmallow : On these versions of Android, the URI you get back may or may not be a "file://"
     * URI, depending on whether the file in question is on disk or is a remote file. Using a {@link ParcelFileDescriptor}
     * is the simplest way of handling these variations, without having to parse URIs.
     * <p>
     * Jelly Bean to <KitKat: {@link #getPath(Context, Uri) getPath} handles potential edge cases arising from files picked
     * from the Gallery vs files selected using a third party file explorer
     */
    @Override
    public Message newImageMessage(@NonNull Uri imageUri) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return newThreePartImageMessageFromUri(imageUri);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            ParcelFileDescriptor parcelFileDescriptor = null;
            try {
                parcelFileDescriptor = getContext().getContentResolver().openFileDescriptor(imageUri, "r");
                FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                return newThreePartImageMessageFromFileDescriptor(fileDescriptor);
            } finally {
                if (parcelFileDescriptor != null) {
                    parcelFileDescriptor.close();
                }
            }
        } else {
            String path = getPath(getContext(), imageUri);
            File imageFile = new File(path);
            return newImageMessage(imageFile);
        }
    }

    /**
     * Creates a new ThreePartImage Message.  The full image is attached untouched, while the
     * preview is created from the full image by loading, resizing, and compressing.
     *
     * @param file Image file
     * @return
     */
    @Override
    public Message newImageMessage(@NonNull File file) throws IOException {
        if (file == null) throw new IllegalArgumentException("Null image file");
        if (!file.exists()) throw new IllegalArgumentException("No image file");
        if (!file.canRead()) throw new IllegalArgumentException("Cannot read image file");

        BitmapFactory.Options bounds = getBounds(new FileInputStream(file.getAbsolutePath()));
        ExifInterface exifData = getExifData(file);

        // Create info message part
        MessagePart info = buildInfoMessagePart(bounds, exifData);

        // Create Preview message part
        if (Log.isLoggable(Log.VERBOSE)) {
            Log.v("Creating Preview from '" + file.getAbsolutePath() + "'");
        }
        MessagePart preview = buildPreviewMessagePart(new FileInputStream(file.getAbsolutePath()), bounds, exifData);

        // Create Full message part
        MessagePart full = getLayerClient().newMessagePart(MIME_TYPE_IMAGE_JPEG, new FileInputStream(file), file.length());
        if (Log.isLoggable(Log.VERBOSE)) {
            Log.v(String.format(Locale.US, "Full image bytes: %d, preview bytes: %d, info bytes: %d", full.getSize(), preview.getSize(), info.getSize()));
        }

        MessagePart[] parts = new MessagePart[3];
        parts[PART_INDEX_FULL] = full;
        parts[PART_INDEX_PREVIEW] = preview;
        parts[PART_INDEX_INFO] = info;
        return getLayerClient().newMessage(parts);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private Message newThreePartImageMessageFromUri(@NonNull Uri uri) throws IOException {
        InputStream inputStream = getContext().getContentResolver().openInputStream(uri);
        ExifInterface exifData = getExifData(inputStream);

        Context context = getContext();

        inputStream = context.getContentResolver().openInputStream(uri);
        BitmapFactory.Options bounds = getBounds(inputStream);

        // Create info message part
        MessagePart info = buildInfoMessagePart(bounds, exifData);

        // Create Preview message part
        if (Log.isLoggable(Log.VERBOSE)) {
            Log.v("Creating Preview from " + uri.toString());
        }
        inputStream = context.getContentResolver().openInputStream(uri);
        MessagePart preview = buildPreviewMessagePart(inputStream, bounds, exifData);

        // Create Full message part
        inputStream = context.getContentResolver().openInputStream(uri);
        long fileSize = getFileSizeFromUri(context, uri);
        MessagePart full = getLayerClient().newMessagePart(MIME_TYPE_IMAGE_JPEG, inputStream, fileSize);
        if (Log.isLoggable(Log.VERBOSE)) {
            Log.v(String.format(Locale.US, "Full image bytes: %d, preview bytes: %d, info bytes: %d", full.getSize(), preview.getSize(), info.getSize()));
        }

        MessagePart[] parts = new MessagePart[3];
        parts[PART_INDEX_FULL] = full;
        parts[PART_INDEX_PREVIEW] = preview;
        parts[PART_INDEX_INFO] = info;
        return getLayerClient().newMessage(parts);
    }

    /**
     * Need to copy the file into the cache since the APIs to read exifdata from
     * {@link FileDescriptor} & {@link InputStream} are not available on Android versions < Nougat.
     * <p>
     * The only option is to read that information from a file, which necessitates the use of
     * {@link #writeStreamToFile(String, InputStream)}, after which the file handling is standard
     * for all Android versions < Nougat
     */
    private Message newThreePartImageMessageFromFileDescriptor(@NonNull FileDescriptor fileDescriptor) throws IOException {
        InputStream inputStream = new FileInputStream(fileDescriptor);
        String filePath = getContext().getCacheDir() + "/img_" + Calendar.getInstance().getTimeInMillis();
        writeStreamToFile(filePath, inputStream);
        return newImageMessage(new File(filePath));
    }

    private MessagePart buildPreviewMessagePart(InputStream inputStream,
                                                BitmapFactory.Options bounds, ExifInterface exifData) throws IOException {

        Bitmap previewBitmap = getPreviewBitmap(bounds, inputStream);
        File temp = new File(getContext().getCacheDir(), getClass().getSimpleName() + "." + System.nanoTime() + ".jpg");
        FileOutputStream previewStream = new FileOutputStream(temp);

        if (Log.isLoggable(Log.VERBOSE)) {
            Log.v("Compressing preview to '" + temp.getAbsolutePath() + "'");
        }

        previewBitmap.compress(Bitmap.CompressFormat.JPEG, PREVIEW_COMPRESSION_QUALITY, previewStream);
        previewBitmap.recycle();
        previewStream.close();

        // Preserve exif orientation
        ExifInterface preserver = new ExifInterface(temp.getAbsolutePath());
        int[] orientationData = getOrientationData(exifData);
        int exifOrientation = orientationData[1];
        preserver.setAttribute(ExifInterface.TAG_ORIENTATION, Integer.toString(exifOrientation));
        preserver.saveAttributes();
        if (Log.isLoggable(Log.VERBOSE)) {
            Log.v("Exif orientation preserved in preview");
        }

        return getLayerClient().newMessagePart(MIME_TYPE_PREVIEW, new FileInputStream(temp), temp.length());
    }

    private MessagePart buildInfoMessagePart(BitmapFactory.Options bounds, ExifInterface exifData) throws IOException {
        int[] orientationData = getOrientationData(exifData);
        int orientation = orientationData[0];

        boolean isSwap = orientation == ORIENTATION_270 || orientation == ORIENTATION_90;

        String intoString = "{\"orientation\":" + orientation + ", \"width\":"
                + (!isSwap ? bounds.outWidth : bounds.outHeight) + ", \"height\":"
                + (!isSwap ? bounds.outHeight : bounds.outWidth) + "}";

        if (Log.isLoggable(Log.VERBOSE)) {
            Log.v("Creating image info: " + intoString);
        }

        return getLayerClient().newMessagePart(MIME_TYPE_INFO, intoString.getBytes());
    }

    private int[] getOrientationData(ExifInterface exifInterface) {
        // Try parsing Exif data.
        int orientation = ORIENTATION_0;
        int exifOrientation = ExifInterface.ORIENTATION_UNDEFINED;

        exifOrientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
        if (Log.isLoggable(Log.VERBOSE)) {
            Log.v("Found Exif orientation: " + exifOrientation);
        }
        switch (exifOrientation) {
            case ExifInterface.ORIENTATION_UNDEFINED:
            case ExifInterface.ORIENTATION_NORMAL:
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                orientation = ORIENTATION_0;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                orientation = ORIENTATION_180;
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
            case ExifInterface.ORIENTATION_ROTATE_90:
                orientation = ORIENTATION_270;
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
            case ExifInterface.ORIENTATION_ROTATE_270:
                orientation = ORIENTATION_90;
                break;
        }
        int[] orientationData = new int[]{orientation, exifOrientation};
        return orientationData;
    }
}
