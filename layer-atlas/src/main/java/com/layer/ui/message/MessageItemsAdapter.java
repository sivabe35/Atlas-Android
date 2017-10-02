package com.layer.ui.message;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.layer.sdk.LayerClient;
import com.layer.ui.R;
import com.layer.ui.adapters.ItemViewHolder;
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
        MessageItemViewModel messageItemViewModel = new MessageItemViewModel(parent.getContext(),
                getLayerClient(), getImageCacheWrapper(), getDateFormatter(), getIdentityFormatter(),
                getIdentityEventListener(), false, false, false);

        return new MessageItemHeaderViewHolder(parent, messageItemViewModel);
    }

    @Override
    public void bindHeader(ItemViewHolder viewHolder) {
        MessageItemHeaderViewHolder holder = (MessageItemHeaderViewHolder) viewHolder;
        View headerView = getHeaderView();
        if (headerView.getParent() != null) {
            ((ViewGroup) headerView.getParent()).removeView(headerView);
        }

        holder.bind(headerView);
    }

    @Override
    protected MessageItemViewHolder createMessageItemViewHolder(ViewGroup parent, MessageCell messageCell) {
        MessageItemViewModel messageItemViewModel = new MessageItemViewModel(parent.getContext(),
                getLayerClient(), getImageCacheWrapper(), getDateFormatter(), getIdentityFormatter(),
                getIdentityEventListener(), super.mReadReceiptsEnabled,
                getShouldShowAvatarInOneOnOneConversations(), getShouldShowAvatarPresence());

        return new MessageItemViewHolder(parent, messageItemViewModel, messageCell);
    }

    @Override
    public void bindMessageItem(ItemViewHolder holder, MessageCluster messageCluster, int position) {
        MessageItemViewHolder viewHolder = (MessageItemViewHolder) holder;

        viewHolder.bind(mLayerClient, messageCluster, position,
                getRecipientStatusPosition(), mRecyclerView.getWidth());
    }

    @Override
    protected MessageItemFooterViewHolder createFooterViewHolder(ViewGroup parent) {
        MessageItemViewModel messageItemViewModel = new MessageItemViewModel(parent.getContext(),
                getLayerClient(), getImageCacheWrapper(), getDateFormatter(), getIdentityFormatter(),
                getIdentityEventListener(), false, false, false);

        return new MessageItemFooterViewHolder(parent, messageItemViewModel, getImageCacheWrapper());
    }

    @Override
    public void bindFooter(ItemViewHolder holder) {
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
