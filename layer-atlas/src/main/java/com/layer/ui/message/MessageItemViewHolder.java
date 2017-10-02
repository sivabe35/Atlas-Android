package com.layer.ui.message;

import android.databinding.ViewDataBinding;
import android.support.annotation.LayoutRes;
import android.view.ViewGroup;

import com.layer.sdk.messaging.Message;
import com.layer.ui.adapters.ItemViewHolder;
import com.layer.ui.message.messagetypes.MessageStyle;
import com.layer.ui.viewmodel.ItemViewModel;

public class MessageItemViewHolder<VIEW_MODEL extends ItemViewModel<Message>> extends ItemViewHolder<Message, VIEW_MODEL, ViewDataBinding, MessageStyle> {
    public MessageItemViewHolder(ViewDataBinding binding, VIEW_MODEL viewModel) {
        super(binding, viewModel);
    }

    public MessageItemViewHolder(ViewGroup parent, @LayoutRes int layoutId, VIEW_MODEL viewModel) {
        super(parent, layoutId, viewModel);
    }
}
