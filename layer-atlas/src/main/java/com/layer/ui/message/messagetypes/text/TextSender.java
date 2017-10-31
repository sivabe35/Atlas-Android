package com.layer.ui.message.messagetypes.text;

import android.content.Context;

import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Identity;
import com.layer.sdk.messaging.Message;
import com.layer.sdk.messaging.MessageOptions;
import com.layer.sdk.messaging.MessagePart;
import com.layer.sdk.messaging.PushNotificationPayload;
import com.layer.ui.R;
import com.layer.ui.message.messagetypes.MessageSender;
import com.layer.ui.util.Log;
import com.layer.ui.util.Util;

public class TextSender extends MessageSender {
    private final int mMaxNotificationLength;

    public TextSender(Context context, LayerClient layerClient) {
        this(context, layerClient, 200);
    }

    public TextSender(Context context, LayerClient layerClient, int maxNotificationLength) {
        super(context, layerClient);
        mMaxNotificationLength = maxNotificationLength;
    }

    public boolean requestSend(String text) {
        if (text == null || text.trim().length() == 0) {
            if (Log.isLoggable(Log.ERROR)) Log.e("No text to send");
            return false;
        }
        if (Log.isLoggable(Log.VERBOSE)) Log.v("Sending text message");

        if (Log.isPerfLoggable()) {
            Log.perf("TextSender is attempting to send a message");
        }

        // Create notification string
        Identity me = getLayerClient().getAuthenticatedUser();
        String myName = me == null ? "" : Util.getDisplayName(me);
        String notificationString = getContext().getString(R.string.layer_ui_notification_text, myName, (text.length() < mMaxNotificationLength) ? text : (text.substring(0, mMaxNotificationLength) + "…"));

        // Send message
        MessagePart part = getLayerClient().newMessagePart(text);

        PushNotificationPayload payload = new PushNotificationPayload.Builder()
                .text(notificationString)
                .build();
        Message message = getLayerClient().newMessage(new MessageOptions().defaultPushNotificationPayload(payload), part);
        return send(message);
    }
}
