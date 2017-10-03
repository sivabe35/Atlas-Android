package com.layer.ui.message;

import android.view.View;
import android.view.ViewGroup;

import com.layer.ui.R;
import com.layer.ui.databinding.UiMessageItemHeaderBinding;

public class MessageItemHeaderViewHolder extends MessageItemViewHolder<MessageItemLegacyViewModel, UiMessageItemHeaderBinding> {
    public MessageItemHeaderViewHolder(ViewGroup parent, MessageItemLegacyViewModel viewModel) {
        super(parent, R.layout.ui_message_item_header, viewModel);
    }

    public void bind(View headerView) {
        getBinding().content.addView(headerView);
    }
}
