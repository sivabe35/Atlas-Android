package com.layer.ui.util.imagecache.requesthandlers;

import static com.squareup.picasso.Picasso.LoadedFrom;

import android.net.Uri;

import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.MessagePart;
import com.layer.ui.util.Util;
import com.squareup.picasso.Request;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Handles Picasso load requests for Layer MessagePart content.  If the content is not ready
 * (e.g. MessagePart.isContentReady() is `false`), registers a LayerProgressListener, downloads
 * the part, and waits for completion.
 */
public class MessagePartRequestHandler extends com.squareup.picasso.RequestHandler {
    private final LayerClient mLayerClient;

    public MessagePartRequestHandler(LayerClient layerClient) {
        mLayerClient = layerClient;
    }

    @Override
    public boolean canHandleRequest(Request data) {
        Uri uri = data.uri;
        if (!"layer".equals(uri.getScheme())) return false;
        List<String> segments = uri.getPathSegments();
        if (segments.size() != 4) return false;
        if (!segments.get(2).equals("parts")) return false;
        return true;
    }

    @Override
    public Result load(Request request, int networkPolicy) throws IOException {
        MessagePart part = Util.getMessagePartBlocking(mLayerClient, request.uri);
        if (part == null) return null;
        if (part.isContentReady()) return new Result(part.getDataStream(), LoadedFrom.DISK);
        if (!Util.downloadMessagePart(mLayerClient, part, 3, TimeUnit.MINUTES)) return null;
        return new Result(part.getDataStream(), LoadedFrom.NETWORK);
    }
}
