package com.layer.ui.message.model;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.ColorRes;
import android.text.TextUtils;

import com.layer.sdk.LayerClient;
import com.layer.sdk.listeners.LayerProgressListener;
import com.layer.sdk.messaging.Message;
import com.layer.sdk.messaging.MessagePart;
import com.layer.ui.identity.IdentityFormatter;
import com.layer.ui.identity.IdentityFormatterImpl;
import com.layer.ui.message.view.MessageView;
import com.layer.ui.util.DateFormatter;
import com.layer.ui.util.DateFormatterImpl;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class MessageModel extends BaseObservable implements LayerProgressListener.Weak {
    private final AtomicInteger mDownloadingPartCounter;

    private IdentityFormatter mIdentityFormatter;
    private DateFormatter mDateFormatter;

    private final Context mContext;
    private final LayerClient mLayerClient;

    private Message mMessage;

    public MessageModel(Context context, LayerClient layerClient) {
        mIdentityFormatter = new IdentityFormatterImpl(context);
        mDateFormatter = new DateFormatterImpl(context);
        mDownloadingPartCounter = new AtomicInteger();
        mContext = context;
        mLayerClient = layerClient;
    }

    public void setMessage(Message message) {
        mMessage = message;
        for (MessagePart messagePart : message.getMessageParts()) {
            if (messagePart.isContentReady()) {
                parse(messagePart);
            } else {
                messagePart.download(this);
            }
        }
    }

    public abstract Class<? extends MessageView> getRendererType();

    protected abstract void parse(MessagePart messagePart);

    @Override
    public void onProgressStart(MessagePart messagePart, Operation operation) {
        incrementDownloadingPartCounter();
    }

    @Override
    public void onProgressUpdate(MessagePart messagePart, Operation operation, long l) {

    }

    @Override
    public void onProgressComplete(MessagePart messagePart, Operation operation) {
        decrementDownloadingPartCounter();
        parse(messagePart);
        notifyChange();
    }

    @Override
    public void onProgressError(MessagePart messagePart, Operation operation, Throwable throwable) {
        decrementDownloadingPartCounter();
    }

    @Bindable
    public abstract String getTitle();

    @Bindable
    public abstract String getDescription();

    @Bindable
    public abstract String getFooter();

    @Bindable
    public abstract
    @ColorRes
    int getBackgroundColor();

    @Bindable
    public boolean getHasMetadata() {
        return (!TextUtils.isEmpty(getTitle()))
                || !TextUtils.isEmpty(getDescription())
                || !TextUtils.isEmpty(getFooter());
    }

    @Bindable
    public abstract boolean getHasContent();

    private void incrementDownloadingPartCounter() {
        mDownloadingPartCounter.getAndIncrement();
    }

    private void decrementDownloadingPartCounter() {
        if (mDownloadingPartCounter.intValue() == 0) return;
        mDownloadingPartCounter.getAndDecrement();
    }

    protected Context getContext() {
        return mContext;
    }

    protected LayerClient getLayerClient() {
        return mLayerClient;
    }

    protected IdentityFormatter getIdentityFormatter() {
        return mIdentityFormatter;
    }

    public void setIdentityFormatter(IdentityFormatter identityFormatter) {
        mIdentityFormatter = identityFormatter;
    }

    protected DateFormatter getDateFormatter() {
        return mDateFormatter;
    }

    public void setDateFormatter(DateFormatter dateFormatter) {
        mDateFormatter = dateFormatter;
    }

    protected Message getMessage() {
        return mMessage;
    }

    protected int getNumberOfPartsCurrentlyDownloading() {
        return mDownloadingPartCounter.intValue();
    }
}
