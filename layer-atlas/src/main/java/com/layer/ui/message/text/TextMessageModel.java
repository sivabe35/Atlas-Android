package com.layer.ui.message.text;

import android.content.Context;
import android.databinding.Bindable;
import android.text.TextUtils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.MessagePart;
import com.layer.ui.R;
import com.layer.ui.message.model.MessageModel;
import com.layer.ui.message.view.MessageView;

public class TextMessageModel extends MessageModel {

    public final static String ROOT_MIME_TYPE = "application/vnd.layer.text+json";
    private final JsonParser mJsonParser;

    private String mText;
    private String mTitle;
    private String mSubtitle;
    private String mAuthor;
    private String mActionEvent;
    private JsonObject mCustomData;

    public TextMessageModel(Context context, LayerClient layerClient) {
        super(context, layerClient);
        mJsonParser = new JsonParser();
    }

    @Override
    public Class<? extends MessageView> getRendererType() {
        return TextMessageView.class;
    }

    @Override
    protected boolean shouldDownloadContentIfNotReady(MessagePart messagePart) {
        return true;
    }

    @Override
    protected void parse(MessagePart messagePart) {
        String data = new String(messagePart.getData());
        JsonObject jsonObject = mJsonParser.parse(data).getAsJsonObject();
        mText = jsonObject.has("text") ? jsonObject.get("text").getAsString() : null;
        mSubtitle = jsonObject.has("subtitle") ? jsonObject.get("subtitle").getAsString().trim() : null;
        mTitle = jsonObject.has("title") ? jsonObject.get("title").getAsString().trim() : null;
        mAuthor = jsonObject.has("author") ? jsonObject.get("author").getAsString().trim() : null;
        if (jsonObject.has("action")) {
            JsonObject action = jsonObject.getAsJsonObject("action");
            mActionEvent = action.get("event").getAsString();
            mCustomData = action.get("data").getAsJsonObject();
        } else {
            mActionEvent = null;
            mCustomData = null;
        }
    }

    @Bindable
    public String getText() {
        return mText;
    }

    @Override
    @Bindable
    public String getTitle() {
        return mTitle;
    }

    @Override
    @Bindable
    public String getDescription() {
        return mSubtitle;
    }

    @Override
    public String getActionEvent() {
        return mActionEvent;
    }

    @Override
    public JsonObject getActionData() {
        if (mCustomData != null) {
            return mCustomData;
        } else {
            return super.getActionData();
        }
    }

    @Override
    public String getFooter() {
        return mAuthor;
    }

    @Override
    public boolean getHasContent() {
        return !TextUtils.isEmpty(mText);
    }

    @Override
    public int getBackgroundColor() {
        return isMessageFromMe() ? R.color.ui_text_message_view_background_me : R.color.ui_text_message_view_background_them;
    }

    @Bindable
    public String getAuthorName() {
        return getIdentityFormatter().getDisplayName(getMessage().getSender());
    }

    @Bindable
    public boolean isDownloadingParts() {
        return getNumberOfPartsCurrentlyDownloading() > 0;
    }
}
