package com.layer.ui.message;

import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.layer.ui.R;
import com.layer.ui.avatar.AvatarViewModelImpl;
import com.layer.ui.databinding.UiMessageItemLegacyBinding;
import com.layer.ui.message.messagetypes.CellFactory;

public class MessageItemLegacyViewHolder extends MessageItemViewHolder<MessageItemLegacyViewModel, UiMessageItemLegacyBinding> {

    // Cell
    protected MessageCell mMessageCell;
    protected CellFactory.CellHolder mCellHolder;
    protected CellFactory.CellHolderSpecs mCellHolderSpecs;

    public MessageItemLegacyViewHolder(ViewGroup parent, MessageItemLegacyViewModel messageItemViewModel, MessageCell messageCell) {
        super(parent, R.layout.ui_message_item_legacy, messageItemViewModel);

        getBinding().avatar.init(new AvatarViewModelImpl(messageItemViewModel.getImageCacheWrapper()),
                messageItemViewModel.getIdentityFormatter());

        getBinding().setViewModel(messageItemViewModel);

        mMessageCell = messageCell;
        mCellHolder = messageCell.mCellFactory.createCellHolder(getBinding().cell,
                messageCell.mMe, getLayoutInflater());

        mCellHolderSpecs = new CellFactory.CellHolderSpecs();
    }

    private void updateCellHolderSpecs(int parentWidth) {
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) getBinding().cell.getLayoutParams();
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

    public void bind(MessageCluster messageCluster, int position, int recipientStatusPosition, int parentWidth) {

        getViewModel().update(messageCluster, mMessageCell, position, recipientStatusPosition);
        updateCellHolderSpecs(parentWidth);

        mCellHolder.setMessage(getItem());
        CellFactory.ParsedContent parsedContent = mMessageCell.mCellFactory
                .getParsedContent(getViewModel().getLayerClient(), getItem());

        mMessageCell.mCellFactory.bindCellHolder(mCellHolder, parsedContent, getItem(), mCellHolderSpecs);
    }
}
