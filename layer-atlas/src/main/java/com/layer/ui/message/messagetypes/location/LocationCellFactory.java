package com.layer.ui.message.messagetypes.location;

import android.content.Context;
import android.content.Intent;
import android.databinding.ViewDataBinding;
import android.net.Uri;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Message;
import com.layer.ui.R;
import com.layer.ui.databinding.UiMessageItemCellImageBinding;
import com.layer.ui.message.messagetypes.CellFactory;
import com.layer.ui.util.Log;
import com.layer.ui.util.Util;
import com.layer.ui.util.imagecache.ImageCacheWrapper;
import com.layer.ui.util.imagecache.ImageRequestParameters;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;

public class LocationCellFactory extends
        CellFactory<LocationCellFactory.CellHolder, LocationCellFactory.Location> implements View.OnClickListener {
    private static final String IMAGE_CACHING_TAG = LocationCellFactory.class.getSimpleName();
    public static final String MIME_TYPE = "location/coordinate";
    public static final String KEY_LATITUDE = "lat";
    public static final String KEY_LONGITUDE = "lon";
    public static final String KEY_LABEL = "label";

    private static final int PLACEHOLDER = R.drawable.ui_message_item_cell_placeholder;
    private static final double GOLDEN_RATIO = (1.0 + Math.sqrt(5.0)) / 2.0;
    private static final int CACHE_SIZE_BYTES = 256 * 1024;

    private final ImageCacheWrapper mImageCacheWrapper;

    public LocationCellFactory(ImageCacheWrapper imageCacheWrapper) {
        super(CACHE_SIZE_BYTES);
        this.mImageCacheWrapper = imageCacheWrapper;
    }

    public boolean isType(Message message) {
        return message.getMessageParts().size() == 1 && message.getMessageParts().get(0).getMimeType().equals(MIME_TYPE);
    }

    @Override
    public String getPreviewText(Context context, Message message) {
        if (isType(message)) {
            return context.getString(R.string.layer_ui_message_preview_location);
        }
        else {
            throw new IllegalArgumentException("Message is not of the correct type - Location");
        }
    }

    @Override
    public boolean isBindable(Message message) {
        return isType(message);
    }

    @Override
    public CellHolder createCellHolder(ViewGroup cellView, boolean isMe, LayoutInflater layoutInflater) {

        return new CellHolder(UiMessageItemCellImageBinding.inflate(layoutInflater, cellView, true));
    }

    @Override
    public Location parseContent(LayerClient layerClient, Message message) {
        try {
            JSONObject o = new JSONObject(new String(message.getMessageParts().get(0).getData()));
            Location c = new Location();
            c.mLatitude = o.optDouble(KEY_LATITUDE, 0);
            c.mLongitude = o.optDouble(KEY_LONGITUDE, 0);
            c.mLabel = o.optString(KEY_LABEL, null);
            return c;
        } catch (JSONException e) {
            if (Log.isLoggable(Log.ERROR)) {
                Log.e(e.getMessage(), e);
            }
        }
        return null;
    }

    @Override
    public void bindCellHolder(final CellHolder cellHolder, final Location location, Message message, CellHolderSpecs specs) {
        cellHolder.mImageView.setTag(location);
        cellHolder.mImageView.setOnClickListener(this);

        // Google Static Map API has max dimension 640
        int mapWidth = Math.min(640, specs.maxWidth);
        int mapHeight = (int) Math.round((double) mapWidth / GOLDEN_RATIO);
        int[] cellDims = Util.scaleDownInside(specs.maxWidth, (int) Math.round((double) specs.maxWidth / GOLDEN_RATIO), specs.maxWidth, specs.maxHeight);
        ViewGroup.LayoutParams params = cellHolder.mImageView.getLayoutParams();
        params.width = cellDims[0];
        params.height = cellDims[1];
        cellHolder.mProgressBar.show();

        ImageCacheWrapper.Callback callback = new ImageCacheWrapper.Callback() {
            @Override
            public void onSuccess() {
                cellHolder.mProgressBar.hide();
            }

            @Override
            public void onFailure() {
                cellHolder.mProgressBar.hide();
            }
        };

        String url = "https://maps.googleapis.com/maps/api/staticmap?zoom=16&maptype=roadmap&scale=2&center=" + location.mLatitude + "," + location.mLongitude + "&markers=color:red%7C" + location.mLatitude + "," + location.mLongitude + "&size=" + mapWidth + "x" + mapHeight;

        ImageRequestParameters imageRequestParameters = new ImageRequestParameters
                .Builder(Uri.parse(url))
                .placeHolder(PLACEHOLDER)
                .resize(cellDims[0], cellDims[1])
                .tag(IMAGE_CACHING_TAG)
                .centerCrop(false)
                .onlyScaleDown(false)
                .defaultCircularTransform(true)
                .rotate(0)
                .callback(callback)
                .build();

        mImageCacheWrapper.loadImage(imageRequestParameters, cellHolder.mImageView);
    }

    @Override
    public void onClick(View v) {
        Location location = (Location) v.getTag();
        String encodedLabel = (location.mLabel == null) ? URLEncoder.encode("Shared Marker") : URLEncoder.encode(location.mLabel);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + location.mLatitude + "," + location.mLongitude + "(" + encodedLabel + ")&z=16"));
        v.getContext().startActivity(intent);
    }

    @Override
    public void onScrollStateChanged(int newState) {
        switch (newState) {
            case RecyclerView.SCROLL_STATE_DRAGGING:
                mImageCacheWrapper.pauseTag(IMAGE_CACHING_TAG);
                break;
            case RecyclerView.SCROLL_STATE_IDLE:
            case RecyclerView.SCROLL_STATE_SETTLING:
                mImageCacheWrapper.resumeTag(IMAGE_CACHING_TAG);
                break;
        }
    }

    static class Location implements CellFactory.ParsedContent {
        double mLatitude;
        double mLongitude;
        String mLabel;

        @Override
        public int sizeOf() {
            return (mLabel == null ? 0 : mLabel.getBytes().length) + ((Double.SIZE + Double.SIZE) / Byte.SIZE);
        }
    }

    static class CellHolder extends CellFactory.CellHolder {
        ImageView mImageView;
        ContentLoadingProgressBar mProgressBar;

        public CellHolder(ViewDataBinding viewDataBinding) {
            if (viewDataBinding instanceof  UiMessageItemCellImageBinding) {
                mImageView = ((UiMessageItemCellImageBinding) viewDataBinding).cellImage;
                mProgressBar = ((UiMessageItemCellImageBinding) viewDataBinding).cellProgress;
            }
        }
    }
}
