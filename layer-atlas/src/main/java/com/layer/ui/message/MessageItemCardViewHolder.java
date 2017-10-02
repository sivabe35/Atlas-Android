package com.layer.ui.message;

import android.support.annotation.LayoutRes;
import android.view.ViewGroup;

import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Message;
import com.layer.ui.R;
import com.layer.ui.adapters.ItemViewHolder;
import com.layer.ui.avatar.AvatarViewModelImpl;
import com.layer.ui.databinding.UiMessageItemBinding;
import com.layer.ui.message.MessageCluster;
import com.layer.ui.message.MessageItemLegacyViewModel;
import com.layer.ui.message.messagetypes.MessageStyle;

public class MessageItemCardViewHolder extends ItemViewHolder<Message, MessageItemLegacyViewModel, UiMessageItemBinding, MessageStyle> {
    public MessageItemCardViewHolder(ViewGroup parent, @LayoutRes int layoutId, MessageItemLegacyViewModel viewModel) {
        super(parent, R.layout.ui_message_item, viewModel);

        getBinding().avatar.init(new AvatarViewModelImpl(viewModel.getImageCacheWrapper()),
                viewModel.getIdentityFormatter());

        getBinding().setViewModel(viewModel);
    }

    public void bind(LayerClient layerClient, MessageCluster messageCluster, int position, int recipientStatusPosition, int parentWidth) {
        getViewModel().update(messageCluster, mMessageCell, position, recipientStatusPosition);
    }
}
