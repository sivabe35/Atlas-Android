package com.layer.ui.adapters;

import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.view.ViewGroup;

import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Conversation;
import com.layer.sdk.messaging.Identity;
import com.layer.sdk.messaging.Message;
import com.layer.sdk.query.Predicate;
import com.layer.sdk.query.Query;
import com.layer.sdk.query.RecyclerViewController;
import com.layer.ui.conversationitem.ConversationItemFormatter;
import com.layer.ui.conversationitem.ConversationItemViewModel;
import com.layer.ui.databinding.UiFourPartItemBinding;
import com.layer.ui.fourpartitem.FourPartItemViewHolder;
import com.layer.ui.identity.IdentityFormatter;
import com.layer.ui.identity.IdentityFormatterImpl;
import com.layer.ui.recyclerview.OnItemClickListener;
import com.layer.ui.style.FourPartItemStyle;
import com.layer.ui.util.DateFormatter;
import com.layer.ui.util.DateFormatterImpl;
import com.layer.ui.util.IdentityRecyclerViewEventListener;
import com.layer.ui.util.imagecache.ImageCacheWrapper;

import java.util.Collection;

public class ConversationItemsAdapter extends ItemRecyclerViewAdapter<Conversation,
        ConversationItemViewModel, UiFourPartItemBinding, FourPartItemStyle,
        FourPartItemViewHolder<Conversation, ConversationItemViewModel>> {
    private static final String TAG = "ConversationItemsAdapter";

    private long mInitialHistory = 0;
    private final IdentityRecyclerViewEventListener mIdentityEventListener;

    private ConversationItemFormatter mConversationItemFormatter;
    private ImageCacheWrapper mImageCacheWrapper;

    private IdentityFormatter mIdentityFormatter;
    private DateFormatter mDateFormatter;
    private LiveData<Identity> mAuthenticatedUser;

    public ConversationItemsAdapter(Context context, LayerClient layerClient,
                                    Query<Conversation> query,
                                    Collection<String> updateAttributes,
                                    ConversationItemFormatter conversationItemFormatter,
                                    ImageCacheWrapper imageCacheWrapper) {
        super(context, layerClient, TAG, false);
        setQuery(query, updateAttributes);
        mConversationItemFormatter = conversationItemFormatter;
        mImageCacheWrapper = imageCacheWrapper;
        mIdentityEventListener = new IdentityRecyclerViewEventListener(this);

        layerClient.registerDataObserver(mIdentityEventListener);

        mIdentityFormatter = new IdentityFormatterImpl(context);
        mDateFormatter = new DateFormatterImpl(context);
        mAuthenticatedUser = layerClient.getAuthenticatedUserLive();
    }

    //==============================================================================================
    // Superclass methods
    //==============================================================================================

    @Override
    public FourPartItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        UiFourPartItemBinding binding = UiFourPartItemBinding.inflate(getLayoutInflater(), parent, false);
        ConversationItemViewModel viewModel = new ConversationItemViewModel(getContext(), getLayerClient());
        viewModel.setIdentityFormatter(mIdentityFormatter);
        viewModel.setDateFormatter(mDateFormatter);

        viewModel.setItemClickListener(getItemClickListener());
        viewModel.setConversationItemFormatter(mConversationItemFormatter);
        viewModel.setAuthenticatedUser(mAuthenticatedUser);

        FourPartItemViewHolder itemViewHolder = new FourPartItemViewHolder<>(binding, viewModel, getStyle(), mImageCacheWrapper);

        binding.addOnRebindCallback(getOnRebindCallback());

        return itemViewHolder;
    }

    @Override
    public void onQueryDataSetChanged(RecyclerViewController recyclerViewController) {
        syncInitialMessages(0, getItemCount());
        super.onQueryDataSetChanged(recyclerViewController);
    }

    @Override
    public void onQueryItemInserted(RecyclerViewController recyclerViewController, int position) {
        syncInitialMessages(position, 1);
        super.onQueryItemInserted(recyclerViewController, position);
    }

    @Override
    public void onQueryItemRangeInserted(RecyclerViewController recyclerViewController, int positionStart, int itemCount) {
        syncInitialMessages(positionStart, itemCount);
        super.onQueryItemRangeInserted(recyclerViewController, positionStart, itemCount);
    }

    @Override
    public void onDestroy() {
        getLayerClient().unregisterDataObserver(mIdentityEventListener);
    }

    //==============================================================================================
    // Setters
    //==============================================================================================

    public void setIdentityFormatter(IdentityFormatter identityFormatter) {
        mIdentityFormatter = identityFormatter;
    }

    public void setDateFormatter(DateFormatter dateFormatter) {
        mDateFormatter = dateFormatter;
    }

    //==============================================================================================
    // UI Interactions
    //==============================================================================================

    @Override
    public void setItemClickListener(OnItemClickListener<Conversation> itemClickListener) {
        super.setItemClickListener(itemClickListener);
    }

    //==============================================================================================
    // Initial message history
    //==============================================================================================

    public void setInitialHistoricMessagesToFetch(long initialHistory) {
        mInitialHistory = initialHistory;
    }

    private void syncInitialMessages(final int start, final int length) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                long desiredHistory = mInitialHistory;
                if (desiredHistory <= 0) return;
                for (int i = start; i < start + length; i++) {
                    try {
                        final Conversation conversation = getItem(i);
                        if (conversation == null || conversation.getHistoricSyncStatus() != Conversation.HistoricSyncStatus.MORE_AVAILABLE) {
                            continue;
                        }
                        Query<Message> localCountQuery = Query.builder(Message.class)
                                .predicate(new Predicate(Message.Property.CONVERSATION, Predicate.Operator.EQUAL_TO, conversation))
                                .build();
                        long delta = desiredHistory - getLayerClient().executeQueryForCount(localCountQuery);
                        if (delta > 0) conversation.syncMoreHistoricMessages((int) delta);
                    } catch (IndexOutOfBoundsException e) {
                        // Concurrent modification
                    }
                }
            }
        }).start();
    }
}
