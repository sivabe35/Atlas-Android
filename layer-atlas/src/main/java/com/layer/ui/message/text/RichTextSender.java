package com.layer.ui.message.text;

import android.content.Context;

import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Message;
import com.layer.sdk.messaging.MessagePart;
import com.layer.ui.identity.IdentityFormatter;
import com.layer.ui.util.Log;

public class RichTextSender extends TextSender {
    public final static String ROOT_MIME_TYPE = TextMessageModel.ROOT_MIME_TYPE;

    public RichTextSender(Context context, LayerClient layerClient) {
        super(context, layerClient);
    }

    public RichTextSender(Context context, LayerClient layerClient, int maxNotificationLength) {
        super(context, layerClient, maxNotificationLength);
    }

    public RichTextSender(Context context, LayerClient layerClient, int maxNotificationLength, IdentityFormatter identityFormatter) {
        super(context, layerClient, maxNotificationLength, identityFormatter);
    }

    @Override
    public boolean requestSend(String text) {
        if (text == null || text.trim().length() == 0) {
            if (Log.isLoggable(Log.ERROR)) Log.e("No text to send");
            return false;
        }
        if (Log.isLoggable(Log.VERBOSE)) Log.v("Sending text message");

        if (Log.isPerfLoggable()) {
            Log.perf("PlainTextSender is attempting to send a message");
        }

        MessagePart root = getLayerClient().newMessagePart(ROOT_MIME_TYPE, text.getBytes());
        Message message = getLayerClient().newMessage(root);

        return send(message);
    }
}
