package com.layer.ui.message.image;

import android.content.Context;
import android.support.annotation.ColorRes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.MessagePart;
import com.layer.ui.R;
import com.layer.ui.message.MessagePartUtils;
import com.layer.ui.message.model.MessageModel;
import com.layer.ui.util.imagecache.ImageCacheWrapper;
import com.layer.ui.util.imagecache.ImageRequestParameters;
import com.layer.ui.util.imagecache.PicassoImageCacheWrapper;
import com.layer.ui.util.imagecache.requesthandlers.MessagePartRequestHandler;
import com.layer.ui.util.json.AndroidFieldNamingStrategy;
import com.squareup.picasso.Picasso;

import java.io.InputStreamReader;

public class ImageMessageModel extends MessageModel {

    public static final String ACTION_OPEN_URL = "open-url";

    public static final String ROOT_MIME_TYPE = "application/vnd.layer.image+json";
    private static final String ROLE_SOURCE = "source";
    private static final String ROLE_PREVIEW = "preview";

    private static ImageCacheWrapper sImageCacheWrapper;

    private static final int PLACEHOLDER = R.drawable.ui_message_item_cell_placeholder;

    private final Gson mGson;
    private ImageMessageMetadata mMetadata;
    private ImageRequestParameters mPreviewRequestParameters;

    private ImageRequestParameters mSourceRequestParameters;

    public ImageMessageModel(Context context, LayerClient layerClient) {
        super(context, layerClient);
        mGson = new GsonBuilder().setFieldNamingStrategy(new AndroidFieldNamingStrategy()).create();
    }

    @Override
    public Class<ImageMessageView> getRendererType() {
        return ImageMessageView.class;
    }

    @Override
    protected void parse(MessagePart messagePart) {
        if (MessagePartUtils.isRoleRoot(messagePart)) {
            mMetadata = parseRootMessagePart(messagePart);
        } else if (MessagePartUtils.isRole(messagePart, ROLE_PREVIEW)) {
            parsePreviewPart(messagePart);
        } else if (MessagePartUtils.isRole(messagePart, ROLE_SOURCE)) {
            parseSourcePart(messagePart);
        }
    }

    @Override
    protected boolean shouldDownloadContentIfNotReady(MessagePart messagePart) {
        if (MessagePartUtils.getMimeType(messagePart).equals(ROOT_MIME_TYPE)) {
            return true;
        } else if (MessagePartUtils.isRole(messagePart, ROLE_PREVIEW)) {
            return true;
        } else if (MessagePartUtils.isRole(messagePart, ROLE_SOURCE)) {
            return !MessagePartUtils.hasMessagePartWithRole(getMessage(), ROLE_PREVIEW);
        }

        return false;
    }

    /*
    * Private methods
    */

    private ImageMessageMetadata parseRootMessagePart(MessagePart messagePart) {
        JsonReader reader = new JsonReader(new InputStreamReader(messagePart.getDataStream()));
        return mGson.fromJson(reader, ImageMessageMetadata.class);
    }

    private void parsePreviewPart(MessagePart messagePart) {
        ImageRequestParameters.Builder builder = new ImageRequestParameters.Builder();
        if (messagePart.getId() != null) {
            builder.uri(messagePart.getId());
        } else {
            builder.url(mMetadata.getPreviewUrl());
        }

        builder.placeHolder(PLACEHOLDER)
                .resize(getMetadata().getPreviewWidth(), getMetadata().getPreviewHeight())
                .exifOrientation(mMetadata.getOrientation())
                .tag(getClass().getSimpleName());

        mPreviewRequestParameters = builder.build();
    }

    private void parseSourcePart(MessagePart messagePart) {
        ImageRequestParameters.Builder builder = new ImageRequestParameters.Builder();
        if (messagePart.getId() != null) {
            builder.uri(messagePart.getId());
        } else {
            builder.url(mMetadata.getSourceUrl());
        }

        builder.placeHolder(PLACEHOLDER)
                .resize(getMetadata().getWidth(), getMetadata().getHeight())
                .exifOrientation(mMetadata.getOrientation())
                .tag(getClass().getSimpleName());

        mSourceRequestParameters = builder.build();
    }

    public ImageMessageMetadata getMetadata() {
        return mMetadata;
    }

    /*
    * Setters, getters, bindings
    */

    public ImageCacheWrapper getImageCacheWrapper() {
        if (sImageCacheWrapper == null) {
            sImageCacheWrapper = new PicassoImageCacheWrapper(new Picasso.Builder(getContext())
                    .addRequestHandler(new MessagePartRequestHandler(getLayerClient()))
                    .build());
        }
        return sImageCacheWrapper;
    }

    public static void setImageCacheWrapper(ImageCacheWrapper imageCacheWrapper) {
        sImageCacheWrapper = imageCacheWrapper;
    }

    public ImageRequestParameters getPreviewRequestParameters() {
        return mPreviewRequestParameters;
    }

    public ImageRequestParameters getSourceRequestParameters() {
        return mSourceRequestParameters;
    }

    @Override
    public String getTitle() {
        return mMetadata != null ? mMetadata.getTitle() : null;
    }

    @Override
    public String getDescription() {
        return mMetadata != null ? mMetadata.getSubtitle() : null;
    }

    @Override
    public String getFooter() {
        return mMetadata != null ? mMetadata.getSubtitle() : null;
    }

    @Override
    @ColorRes
    public int getBackgroundColor() {
        return R.color.transparent;
    }

    @Override
    public boolean getHasContent() {
        return mMetadata != null && (mPreviewRequestParameters != null || mSourceRequestParameters != null);
    }

}
