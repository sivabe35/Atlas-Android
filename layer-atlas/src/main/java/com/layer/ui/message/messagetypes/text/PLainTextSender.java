package com.layer.ui.message.messagetypes.text;

import android.content.Context;

import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Message;
import com.layer.sdk.messaging.MessageOptions;
import com.layer.sdk.messaging.MessagePart;
import com.layer.sdk.messaging.PushNotificationPayload;
import com.layer.ui.identity.IdentityFormatter;
import com.layer.ui.message.text.TextSender;
import com.layer.ui.util.Log;

public class PlainTextSender extends TextSender {
    public PlainTextSender(Context context, LayerClient layerClient) {
        this(context, layerClient, 200);
    }

    public PlainTextSender(Context context, LayerClient layerClient, int maxNotificationLength) {
        super(context, layerClient, maxNotificationLength);
    }

    public PlainTextSender(Context context, LayerClient layerClient, int maxNotificationLength, IdentityFormatter identityFormatter) {
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

        // Send message
        MessagePart part = getLayerClient().newMessagePart(text);

        PushNotificationPayload payload = new PushNotificationPayload.Builder()
                .text(getNotificationString(text))
                .build();
        Message message = getLayerClient().newMessage(new MessageOptions().defaultPushNotificationPayload(payload), part);
        return send(message);
    }
}
