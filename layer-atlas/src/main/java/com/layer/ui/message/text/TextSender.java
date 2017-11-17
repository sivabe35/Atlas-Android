package com.layer.ui.message.text;

import android.content.Context;

import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Identity;
import com.layer.ui.R;
import com.layer.ui.identity.IdentityFormatter;
import com.layer.ui.identity.IdentityFormatterImpl;
import com.layer.ui.message.messagetypes.MessageSender;

public abstract class TextSender extends MessageSender {
    private int mMaxNotificationLength;
    private IdentityFormatter mIdentityFormatter;

    public TextSender(Context context, LayerClient layerClient) {
        this(context, layerClient, 200);
    }

    public TextSender(Context context, LayerClient layerClient, int maxNotificationLength) {
        this(context, layerClient, maxNotificationLength, new IdentityFormatterImpl(context));
    }

    public TextSender(Context context, LayerClient layerClient, int maxNotificationLength, IdentityFormatter identityFormatter) {
        super(context, layerClient);
        mMaxNotificationLength = maxNotificationLength;
        mIdentityFormatter = identityFormatter;
    }

    public abstract boolean requestSend(String text);

    public String getNotificationString(String text) {
        Identity me = getLayerClient().getAuthenticatedUser();
        String myName = me == null ? "" : mIdentityFormatter.getDisplayName(me);
        return getContext().getString(R.string.layer_ui_notification_text, myName,
                (text.length() < mMaxNotificationLength) ?
                        text : (text.substring(0, mMaxNotificationLength) + "…"));
    }

    public int getMaxNotificationLength() {
        return mMaxNotificationLength;
    }

    public void setMaxNotificationLength(int maxNotificationLength) {
        mMaxNotificationLength = maxNotificationLength;
    }

    public void setIdentityFormatter(IdentityFormatter identityFormatter) {
        mIdentityFormatter = identityFormatter;
    }
}
