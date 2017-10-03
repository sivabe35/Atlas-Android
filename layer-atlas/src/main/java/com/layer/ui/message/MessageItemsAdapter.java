package com.layer.ui.message;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.layer.sdk.LayerClient;
import com.layer.ui.identity.IdentityFormatter;
import com.layer.ui.util.DateFormatter;
import com.layer.ui.util.imagecache.ImageCacheWrapper;

public class MessageItemsAdapter extends MessagesAdapter {

    public MessageItemsAdapter(Context context, LayerClient layerClient,
                               ImageCacheWrapper imageCacheWrapper, DateFormatter dateFormatter,
                               IdentityFormatter identityFormatter) {
        super(context, layerClient, imageCacheWrapper, dateFormatter, identityFormatter);
    }

    @Override
    protected MessageItemHeaderViewHolder createHeaderViewHolder(ViewGroup parent) {
        MessageItemLegacyViewModel viewModel = new MessageItemLegacyViewModel(parent.getContext(),
                getLayerClient(), getImageCacheWrapper(), getDateFormatter(), getIdentityFormatter(),
                getIdentityEventListener(), false, false, false);

        return new MessageItemHeaderViewHolder(parent, viewModel);
    }

    @Override
    public void bindHeader(MessageItemViewHolder viewHolder) {
        MessageItemHeaderViewHolder holder = (MessageItemHeaderViewHolder) viewHolder;
        View headerView = getHeaderView();
        if (headerView.getParent() != null) {
            ((ViewGroup) headerView.getParent()).removeView(headerView);
        }

        holder.bind(headerView);
    }

    @Override
    protected MessageItemCardViewHolder createCardMessageItemViewHolder(ViewGroup parent) {
        return null;
    }

    @Override
    public void bindCardMessageItem(MessageItemViewHolder viewHolder, MessageCluster messageCluster, int position) {
        MessageItemCardViewHolder holder = (MessageItemCardViewHolder) viewHolder;

    }

    @Override
    protected MessageItemLegacyViewHolder createLegacyMessageItemViewHolder(ViewGroup parent, MessageCell messageCell) {
        MessageItemLegacyViewModel viewModel = new MessageItemLegacyViewModel(parent.getContext(),
                getLayerClient(), getImageCacheWrapper(), getDateFormatter(), getIdentityFormatter(),
                getIdentityEventListener(), super.mReadReceiptsEnabled,
                getShouldShowAvatarInOneOnOneConversations(), getShouldShowAvatarPresence());

        return new MessageItemLegacyViewHolder(parent, viewModel, messageCell);
    }

    @Override
    public void bindLegacyMessageItem(MessageItemViewHolder holder, MessageCluster messageCluster, int position) {
        MessageItemLegacyViewHolder viewHolder = (MessageItemLegacyViewHolder) holder;

        viewHolder.bind(mLayerClient, messageCluster, position,
                getRecipientStatusPosition(), mRecyclerView.getWidth());
    }

    @Override
    protected MessageItemFooterViewHolder createFooterViewHolder(ViewGroup parent) {
        MessageItemLegacyViewModel viewModel = new MessageItemLegacyViewModel(parent.getContext(),
                getLayerClient(), getImageCacheWrapper(), getDateFormatter(), getIdentityFormatter(),
                getIdentityEventListener(), false, false, false);

        return new MessageItemFooterViewHolder(parent, viewModel, getImageCacheWrapper());
    }

    @Override
    public void bindFooter(MessageItemViewHolder holder) {
        MessageItemFooterViewHolder viewHolder = (MessageItemFooterViewHolder) holder;
        viewHolder.clear();

        View footerView = getFooterView();

        if (footerView.getParent() != null) {
            ((ViewGroup) footerView.getParent()).removeView(footerView);
        }

        boolean shouldAvatarViewBeVisible = !(isOneOnOneConversation() & !getShouldShowAvatarInOneOnOneConversations());
        viewHolder.bind(mUsersTyping, footerView, shouldAvatarViewBeVisible);
    }
}
