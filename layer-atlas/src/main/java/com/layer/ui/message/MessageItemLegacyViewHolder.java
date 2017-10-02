package com.layer.ui.message;

import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Message;
import com.layer.ui.R;
import com.layer.ui.adapters.ItemViewHolder;
import com.layer.ui.avatar.AvatarViewModelImpl;
import com.layer.ui.databinding.UiMessageItemBinding;
import com.layer.ui.message.messagetypes.CellFactory;
import com.layer.ui.message.messagetypes.MessageStyle;

public class MessageItemLegacyViewHolder extends ItemViewHolder<Message, MessageItemLegacyViewModel, UiMessageItemBinding, MessageStyle> {

    // Cell
    protected MessageCell mMessageCell;
    protected CellFactory.CellHolder mCellHolder;
    protected CellFactory.CellHolderSpecs mCellHolderSpecs;

    public MessageItemLegacyViewHolder(ViewGroup parent, MessageItemLegacyViewModel messageItemViewModel, MessageCell messageCell) {
        super(parent, R.layout.ui_message_item, messageItemViewModel);

        getBinding().avatar.init(new AvatarViewModelImpl(messageItemViewModel.getImageCacheWrapper()),
                messageItemViewModel.getIdentityFormatter());

        getBinding().setViewModel(messageItemViewModel);

        mMessageCell = messageCell;
        mCellHolder = messageCell.mCellFactory.createCellHolder(getBinding().cell,
                messageCell.mMe, getLayoutInflater());

        mCellHolderSpecs = new CellFactory.CellHolderSpecs();
    }

    private void updateCellHolderSpecs(int parentWidth) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) getBinding().cell.getLayoutParams();
        View rootView = getBinding().getRoot();

        int maxWidth = parentWidth - rootView.getPaddingLeft() - rootView.getPaddingRight() - params.leftMargin - params.rightMargin;

        if (!getViewModel().isInAOneOnOneConversation() && !mMessageCell.mMe) {
            // Subtract off avatar width if needed
            ViewGroup.MarginLayoutParams avatarParams = (ViewGroup.MarginLayoutParams) getBinding().avatar.getLayoutParams();
            maxWidth -= avatarParams.width + avatarParams.rightMargin + avatarParams.leftMargin;
        }

        int maxHeight = (int) rootView.getContext().getResources().getDimension(R.dimen.layer_ui_messages_max_cell_height);

        mCellHolderSpecs.isMe = mMessageCell.mMe;
        mCellHolderSpecs.maxWidth = maxWidth;
        mCellHolderSpecs.maxHeight = maxHeight;
    }

    public void bind(LayerClient layerClient, MessageCluster messageCluster, int position, int recipientStatusPosition, int parentWidth) {

        getViewModel().update(messageCluster, mMessageCell, position, recipientStatusPosition);
        updateCellHolderSpecs(parentWidth);

        mCellHolder.setMessage(getItem());

        mMessageCell.mCellFactory.bindCellHolder(mCellHolder,
                mMessageCell.mCellFactory.getParsedContent(layerClient, getItem()),
                getItem(), mCellHolderSpecs);
    }
}
