package com.layer.ui.message;

import android.view.View;
import android.view.ViewGroup;

import com.layer.sdk.messaging.Message;
import com.layer.ui.R;
import com.layer.ui.databinding.UiMessageItemHeaderBinding;
import com.layer.ui.viewmodel.ItemViewModel;

public class MessageItemHeaderViewHolder extends MessageItemViewHolder<ItemViewModel<Message>, UiMessageItemHeaderBinding> {
    public MessageItemHeaderViewHolder(ViewGroup parent, ItemViewModel<Message> viewModel) {
        super(parent, R.layout.ui_message_item_header, viewModel);
    }

    public void bind(View headerView) {
        getBinding().content.addView(headerView);
    }
}
