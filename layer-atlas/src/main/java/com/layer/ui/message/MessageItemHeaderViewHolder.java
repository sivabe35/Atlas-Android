package com.layer.ui.message;

import android.view.View;
import android.view.ViewGroup;

import com.layer.sdk.messaging.Message;
import com.layer.ui.R;
import com.layer.ui.adapters.ItemViewHolder;
import com.layer.ui.databinding.UiMessageItemHeaderBinding;
import com.layer.ui.message.messagetypes.MessageStyle;

public class MessageItemHeaderViewHolder extends ItemViewHolder<Message, MessageItemViewModel, UiMessageItemHeaderBinding, MessageStyle> {
    public MessageItemHeaderViewHolder(ViewGroup parent, MessageItemViewModel viewModel) {
        super(parent, R.layout.ui_message_item_header, viewModel);
    }

    public void bind(View headerView) {
        getBinding().content.addView(headerView);
    }
}
