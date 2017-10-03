package com.layer.ui.message;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

import com.layer.sdk.LayerClient;
import com.layer.sdk.changes.LayerChange;
import com.layer.sdk.changes.LayerChangeEvent;
import com.layer.sdk.listeners.LayerChangeEventListener;
import com.layer.sdk.messaging.Conversation;
import com.layer.sdk.messaging.Identity;
import com.layer.sdk.messaging.Message;
import com.layer.sdk.query.Predicate;
import com.layer.sdk.query.Query;
import com.layer.sdk.query.SortDescriptor;
import com.layer.ui.R;
import com.layer.ui.TypingIndicatorLayout;
import com.layer.ui.message.binder.BinderRegistry;
import com.layer.ui.message.messagetypes.CellFactory;
import com.layer.ui.message.messagetypes.MessageStyle;
import com.layer.ui.recyclerview.ItemsRecyclerView;
import com.layer.ui.util.views.SwipeableItem;

import java.util.List;
import java.util.Set;

public class MessageItemsListView extends SwipeRefreshLayout implements LayerChangeEventListener.BackgroundThread.Weak {

    protected boolean mShouldShowAvatarsInOneOnOneConversations;
    protected MessageStyle mMessageStyle;
    protected ItemsRecyclerView<Message> mMessagesRecyclerView;
    protected LinearLayoutManager mLinearLayoutManager;
    protected MessagesAdapter mAdapter;

    protected LayerClient mLayerClient;
    protected Conversation mConversation;

    protected int mNumberOfItemsPerSync = 25;

    public MessageItemsListView(Context context) {
        this(context, null);
    }

    public MessageItemsListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        parseStyle(context, attrs, 0);

        inflate(getContext(), R.layout.ui_message_items_list, this);
        mMessagesRecyclerView = (ItemsRecyclerView<Message>) findViewById(R.id.ui_message_recycler);

        mLinearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mLinearLayoutManager.setStackFromEnd(true);
        mMessagesRecyclerView.setLayoutManager(mLinearLayoutManager);

        DefaultItemAnimator noChangeAnimator = new DefaultItemAnimator() {
            @Override
            public boolean canReuseUpdatedViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, @NonNull List<Object> payloads) {
                return true;
            }
        };
        noChangeAnimator.setSupportsChangeAnimations(false);
        mMessagesRecyclerView.setItemAnimator(noChangeAnimator);

        setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (mConversation.getHistoricSyncStatus() == Conversation.HistoricSyncStatus.MORE_AVAILABLE) {
                    mConversation.syncMoreHistoricMessages(mNumberOfItemsPerSync);
                }
            }
        });
    }

    public void setItemSwipeListener(SwipeableItem.OnItemSwipeListener<Message> itemSwipeListener) {
        mMessagesRecyclerView.setItemSwipeListener(itemSwipeListener);
    }

    public void onDestroy() {
        mMessagesRecyclerView.onDestroy();
        mLayerClient.unregisterEventListener(this);
    }

    public void setAdapter(final MessagesAdapter adapter) {
        adapter.setStyle(mMessageStyle);
        mAdapter = adapter;
        mMessagesRecyclerView.setAdapter(adapter);
        setShouldShowAvatarInOneOnOneConversations(mShouldShowAvatarsInOneOnOneConversations);

        // Create an adapter that auto-scrolls if we're already at the bottom
        adapter.setOnMessageAppendListener(new MessagesAdapter.OnMessageAppendListener() {
            @Override
            public void onMessageAppend(MessagesAdapter adapter, Message message) {
                autoScroll();
            }
        });
    }

    //============================================================================================
    // LayerChangeEventListener.BackgroundThread.Weak Methods
    //============================================================================================

    @Override
    public void onChangeEvent(LayerChangeEvent layerChangeEvent) {
        for (LayerChange change : layerChangeEvent.getChanges()) {
            if (change.getObject() != mConversation) continue;
            if (change.getChangeType() != LayerChange.Type.UPDATE) continue;
            if (!change.getAttributeName().equals("historicSyncStatus")) continue;
            refresh();
        }
    }

    //============================================================================================
    // SwipeRefreshLayout Methods
    //============================================================================================

    /**
     * Automatically refresh on resume.
     */
    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility != View.VISIBLE) return;
        refresh();
    }

    /**
     * Refreshes the state of the underlying recycler view
     */
    private void refresh() {
        post(new Runnable() {
            @Override
            public void run() {
                if (mConversation == null) {
                    setEnabled(false);
                    setRefreshing(false);
                    return;
                }
                Conversation.HistoricSyncStatus status = mConversation.getHistoricSyncStatus();
                setEnabled(status == Conversation.HistoricSyncStatus.MORE_AVAILABLE);
                setRefreshing(status == Conversation.HistoricSyncStatus.SYNC_PENDING);
            }
        });
    }

    //============================================================================================
    // Public Methods
    //============================================================================================


    public int getNumberOfItemsPerSync() {
        return mNumberOfItemsPerSync;
    }

    public void setNumberOfItemsPerSync(int numberOfItemsPerSync) {
        mNumberOfItemsPerSync = numberOfItemsPerSync;
    }

    /**
     * Convenience pass-through to this list's MessagesAdapter.
     *
     * @see MessagesAdapter#getShouldShowAvatarInOneOnOneConversations()
     */
    public boolean getShouldShowAvatarInOneOnOneConversations() {
        return mAdapter.getShouldShowAvatarInOneOnOneConversations();
    }

    /**
     * Convenience pass-through to this list's MessagesAdapter.
     *
     * @see MessagesAdapter#setShouldShowAvatarInOneOnOneConversations(boolean)
     */
    public void setShouldShowAvatarInOneOnOneConversations(boolean shouldShowAvatarInOneOnOneConversations) {
        mAdapter.setShouldShowAvatarInOneOnOneConversations(shouldShowAvatarInOneOnOneConversations);
    }

    /**
     * Convenience pass-through to this list's MessagesAdapter's BinderRegistry.
     *
     * @see BinderRegistry#setCellFactories(List)
     */
    public void setCellFactories(List<CellFactory> cellFactories) {
        mAdapter.getBinderRegistry().setCellFactories(cellFactories);
    }

    public void setTextTypeface(Typeface myTypeface, Typeface otherTypeface) {
        mMessageStyle.setMyTextTypeface(myTypeface);
        mMessageStyle.setOtherTextTypeface(otherTypeface);
    }

    /**
     * Scrolls if the user is at the end
     */
    private void autoScroll() {
        int end = mAdapter.getItemCount() - 1;
        if (end <= 0) return;
        int visible = findLastVisibleItemPosition();
        // -3 because -1 seems too finicky
        if (visible >= (end - 3)) mMessagesRecyclerView.scrollToPosition(end);
    }

    /**
     * Convenience pass-through to this list's MessagesAdapter.
     *
     * @see MessagesAdapter#setFooterView(View, Set)
     */
    public void setFooterView(TypingIndicatorLayout footerView, Set<Identity> users) {
        mAdapter.setFooterView(footerView, users);
        autoScroll();
    }

    /**
     * Convenience pass-through to this list's LinearLayoutManager.
     *
     * @see LinearLayoutManager#findLastVisibleItemPosition()
     */
    private int findLastVisibleItemPosition() {
        return mLinearLayoutManager.findLastVisibleItemPosition();
    }

    /**
     * Updates the underlying MessagesAdapter with a Query for Messages in the given
     * Conversation.
     *
     * @param layerClient  LayerClient currently in use
     * @param conversation Conversation to display Messages for.
     */
    public void setConversation(LayerClient layerClient, Conversation conversation) {
        setConversation(layerClient, conversation, Query.builder(Message.class)
                .predicate(new Predicate(Message.Property.CONVERSATION, Predicate.Operator.EQUAL_TO, conversation))
                .sortDescriptor(new SortDescriptor(Message.Property.POSITION, SortDescriptor.Order.ASCENDING))
                .build());
    }


    /**
     * Updates the underlying MessagesAdapter with the supplied Query for Messages in the given
     * Conversation.
     *
     * @param layerClient  LayerClient currently in use
     * @param conversation Conversation to display Messages for.
     * @param query        Query to be used with the specified conversation
     */
    public void setConversation(LayerClient layerClient, Conversation conversation, Query<Message> query) {
        if (conversation != null) {
            mAdapter.setReadReceiptsEnabled(conversation.isReadReceiptsEnabled());
        }

        mConversation = conversation;
        mLayerClient = layerClient;
        mLayerClient.registerEventListener(this);

        mAdapter.setQuery(query, null);
        Set<Identity> participants = conversation.getParticipants();
        if (participants != null) {
            mAdapter.setIsOneOnOneConversation(conversation.getParticipants().size() == 2);
        }

        mAdapter.refresh();
    }

    protected void parseStyle(Context context, AttributeSet attrs, int defStyle) {
        TypedArray ta = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MessageItemsListView, R.attr.MessageItemListView, defStyle);
        MessageStyle.Builder messageStyleBuilder = new MessageStyle.Builder();
        messageStyleBuilder.myTextColor(ta.getColor(R.styleable.MessageItemsListView_myTextColor, context.getResources().getColor(R.color.layer_ui_text_black)));
        int myTextStyle = ta.getInt(R.styleable.MessageItemsListView_myTextStyle, Typeface.NORMAL);
        messageStyleBuilder.myTextStyle(myTextStyle);
        String myTextTypefaceName = ta.getString(R.styleable.MessageItemsListView_myTextTypeface);
        messageStyleBuilder.myTextTypeface(myTextTypefaceName != null ? Typeface.create(myTextTypefaceName, myTextStyle) : null);
        messageStyleBuilder.myTextSize(ta.getDimensionPixelSize(R.styleable.MessageItemsListView_myTextSize, context.getResources().getDimensionPixelSize(R.dimen.layer_ui_text_size_message_item)));

        messageStyleBuilder.otherTextColor(ta.getColor(R.styleable.MessageItemsListView_theirTextColor, context.getResources().getColor(R.color.layer_ui_color_primary_blue)));
        int otherTextStyle = ta.getInt(R.styleable.MessageItemsListView_theirTextStyle, Typeface.NORMAL);
        messageStyleBuilder.otherTextStyle(otherTextStyle);
        String otherTextTypefaceName = ta.getString(R.styleable.MessageItemsListView_theirTextTypeface);
        messageStyleBuilder.otherTextTypeface(otherTextTypefaceName != null ? Typeface.create(otherTextTypefaceName, otherTextStyle) : null);
        messageStyleBuilder.otherTextSize(ta.getDimensionPixelSize(R.styleable.MessageItemsListView_theirTextSize, context.getResources().getDimensionPixelSize(R.dimen.layer_ui_text_size_message_item)));

        messageStyleBuilder.myBubbleColor(ta.getColor(R.styleable.MessageItemsListView_myBubbleColor, context.getResources().getColor(R.color.layer_ui_color_primary_blue)));
        messageStyleBuilder.otherBubbleColor(ta.getColor(R.styleable.MessageItemsListView_theirBubbleColor, context.getResources().getColor(R.color.layer_ui_color_primary_gray)));

        mShouldShowAvatarsInOneOnOneConversations = ta.getBoolean(R.styleable.MessageItemsListView_shouldShowAvatarsInOneOnOneConversations, false);
        ta.recycle();
        mMessageStyle = messageStyleBuilder.build();
    }
}
