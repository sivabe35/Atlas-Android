package com.layer.ui.message;

import android.view.ViewGroup;

import com.layer.ui.R;
import com.layer.ui.avatar.AvatarViewModelImpl;
import com.layer.ui.databinding.UiMessageItemCardBinding;
import com.layer.ui.message.model.MessageModelManager;

public class MessageItemCardViewHolder extends MessageItemViewHolder<MessageItemCardViewModel, UiMessageItemCardBinding> {
    public MessageItemCardViewHolder(ViewGroup parent, MessageItemCardViewModel viewModel, MessageModelManager modelRegistry) {
        super(parent, R.layout.ui_message_item_card, viewModel);

        getBinding().avatar.init(new AvatarViewModelImpl(viewModel.getImageCacheWrapper()),
                viewModel.getIdentityFormatter());

        getBinding().setViewModel(viewModel);
        getBinding().messageViewer.setMessageModelManager(modelRegistry);
    }

    public void bind(MessageCluster messageCluster, int position, int recipientStatusPosition, int parentWidth) {
        getViewModel().update(messageCluster, position, recipientStatusPosition);
    }
}
