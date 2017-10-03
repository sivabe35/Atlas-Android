package com.layer.ui.message;

import android.support.annotation.LayoutRes;
import android.view.ViewGroup;

import com.layer.sdk.LayerClient;
import com.layer.ui.R;
import com.layer.ui.avatar.AvatarViewModelImpl;
import com.layer.ui.databinding.UiMessageItemBinding;

public class MessageItemCardViewHolder extends MessageItemViewHolder<MessageItemLegacyViewModel, UiMessageItemBinding> {
    public MessageItemCardViewHolder(ViewGroup parent, @LayoutRes int layoutId, MessageItemLegacyViewModel viewModel) {
        super(parent, R.layout.ui_message_item, viewModel);

        getBinding().avatar.init(new AvatarViewModelImpl(viewModel.getImageCacheWrapper()),
                viewModel.getIdentityFormatter());

        getBinding().setViewModel(viewModel);
    }

    public void bind(LayerClient layerClient, MessageCluster messageCluster, int position, int recipientStatusPosition, int parentWidth) {
        getViewModel().update(messageCluster, null, position, recipientStatusPosition);
    }
}
