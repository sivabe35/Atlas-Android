/*
 * Copyright (c) 2015 Layer. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.layer.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.AttributeSet;
import android.view.View;

import com.layer.ui.adapters.MessagesAdapter;
import com.layer.ui.messagetypes.CellFactory;
import com.layer.ui.messagetypes.MessageStyle;
import com.layer.ui.util.imagecache.ImageCacheWrapper;
import com.layer.ui.util.itemanimators.NoChangeAnimator;
import com.layer.ui.util.views.SwipeableItem;
import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Conversation;
import com.layer.sdk.messaging.Message;
import com.layer.sdk.query.Predicate;
import com.layer.sdk.query.Query;
import com.layer.sdk.query.SortDescriptor;

public class MessagesRecyclerView extends RecyclerView {
    private MessagesAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private ItemTouchHelper mSwipeItemTouchHelper;
    private boolean mShouldShowAvatarsInOneOnOneConversations;

    private MessageStyle mMessageStyle;

    public MessagesRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        parseStyle(getContext(), attrs, defStyle);
    }

    public MessagesRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MessagesRecyclerView(Context context) {
        super(context);
    }

    public MessagesRecyclerView init(LayerClient layerClient, ImageCacheWrapper imageCacheWrapper) {
        mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mLayoutManager.setStackFromEnd(true);
        setLayoutManager(mLayoutManager);

        // Create an adapter that auto-scrolls if we're already at the bottom
        mAdapter = new MessagesAdapter(getContext(), layerClient, imageCacheWrapper)
                .setRecyclerView(this)
                .setOnMessageAppendListener(new MessagesAdapter.OnMessageAppendListener() {
                    @Override
                    public void onMessageAppend(MessagesAdapter adapter, Message message) {
                        autoScroll();
                    }
                });
        mAdapter.setStyle(mMessageStyle);
        super.setAdapter(mAdapter);

        // Don't flash items when changing content
        setItemAnimator(new NoChangeAnimator());

        addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                for (CellFactory factory : mAdapter.getCellFactories()) {
                    factory.onScrollStateChanged(newState);
                }
            }
        });

        setShouldShowAvatarInOneOnOneConversations(mShouldShowAvatarsInOneOnOneConversations);
        return this;
    }

    @Override
    public void setAdapter(Adapter adapter) {
        throw new RuntimeException("AtlasMessagesRecyclerView sets its own Adapter");
    }

    /**
     * Performs cleanup when the Activity/Fragment using the adapter is destroyed.
     */
    public void onDestroy() {
        if (mAdapter != null) {
            mAdapter.onDestroy();
        }
    }

    /**
     * Automatically refresh on resume
     */
    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility != View.VISIBLE) return;
        refresh();
    }

    public MessagesRecyclerView refresh() {
        if (mAdapter != null) mAdapter.refresh();
        return this;
    }

    /**
     * Updates the underlying MessagesAdapter with a Query for Messages in the given
     * Conversation.
     *
     * @param conversation Conversation to display Messages for.
     * @return This AtlasMessagesRecyclerView.
     */
    public MessagesRecyclerView setConversation(Conversation conversation) {
        if (conversation != null) {
            mAdapter.setReadReceiptsEnabled(conversation.isReadReceiptsEnabled());
        }
        mAdapter.setQuery(Query.builder(Message.class)
                .predicate(new Predicate(Message.Property.CONVERSATION, Predicate.Operator.EQUAL_TO, conversation))
                .sortDescriptor(new SortDescriptor(Message.Property.POSITION, SortDescriptor.Order.ASCENDING))
                .build()).refresh();
        return this;
    }

    public MessagesRecyclerView setOnMessageSwipeListener(SwipeableItem.OnSwipeListener<Message> listener) {
        if (mSwipeItemTouchHelper != null) {
            mSwipeItemTouchHelper.attachToRecyclerView(null);
        }
        if (listener == null) {
            mSwipeItemTouchHelper = null;
        } else {
            listener.setBaseAdapter((MessagesAdapter) getAdapter());
            mSwipeItemTouchHelper = new ItemTouchHelper(listener);
            mSwipeItemTouchHelper.attachToRecyclerView(this);
        }
        return this;
    }

    /**
     * Convenience pass-through to this list's MessagesAdapter.
     *
     * @see MessagesAdapter#addCellFactories(CellFactory...)
     */
    public MessagesRecyclerView addCellFactories(CellFactory... cellFactories) {
        mAdapter.addCellFactories(cellFactories);
        return this;
    }

    public MessagesRecyclerView setTextTypeface(Typeface myTypeface, Typeface otherTypeface) {
        mMessageStyle.setMyTextTypeface(myTypeface);
        mMessageStyle.setOtherTextTypeface(otherTypeface);
        return this;
    }

    /**
     * Convenience pass-through to this list's LinearLayoutManager.
     *
     * @see LinearLayoutManager#findLastVisibleItemPosition()
     */
    private int findLastVisibleItemPosition() {
        return mLayoutManager.findLastVisibleItemPosition();
    }

    /**
     * Convenience pass-through to this list's MessagesAdapter.
     *
     * @see MessagesAdapter#setFooterView(View)
     */
    public MessagesRecyclerView setFooterView(View footerView) {
        mAdapter.setFooterView(footerView);
        autoScroll();
        return this;
    }

    /**
     * Convenience pass-through to this list's MessagesAdapter.
     *
     * @see MessagesAdapter#getFooterView()
     */
    public View getFooterView() {
        return mAdapter.getFooterView();
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
    public MessagesRecyclerView setShouldShowAvatarInOneOnOneConversations(boolean shouldShowAvatarInOneOnOneConversations) {
        mAdapter.setShouldShowAvatarInOneOnOneConversations(shouldShowAvatarInOneOnOneConversations);
        return this;
    }

    /**
     * Convenience pass-through to this list's MessagesAdapter.
     *
     * @see MessagesAdapter#getShouldShowAvatarPresence()
     */
    public boolean getShouldShowAvatarPresence() {
        return mAdapter.getShouldShowAvatarPresence();
    }

    /**
     * Convenience pass-through to this list's MessagesAdapter.
     *
     * @see MessagesAdapter#setShouldShowAvatarPresence(boolean)
     */
    public MessagesRecyclerView setShouldShowAvatarPresence(boolean shouldShowAvatarPresence) {
        mAdapter.setShouldShowAvatarPresence(shouldShowAvatarPresence);
        return this;
    }

    /**
     * Scrolls if the user is at the end
     */
    private void autoScroll() {
        int end = mAdapter.getItemCount() - 1;
        if (end <= 0) return;
        int visible = findLastVisibleItemPosition();
        // -3 because -1 seems too finicky
        if (visible >= (end - 3)) scrollToPosition(end);
    }

    public void parseStyle(Context context, AttributeSet attrs, int defStyle) {
        TypedArray ta = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MessagesRecyclerView, R.attr.MessagesRecyclerView, defStyle);
        MessageStyle.Builder messageStyleBuilder = new MessageStyle.Builder();
        messageStyleBuilder.myTextColor(ta.getColor(R.styleable.MessagesRecyclerView_myTextColor, context.getResources().getColor(R.color.layer_ui_text_black)));
        int myTextStyle = ta.getInt(R.styleable.MessagesRecyclerView_myTextStyle, Typeface.NORMAL);
        messageStyleBuilder.myTextStyle(myTextStyle);
        String myTextTypefaceName = ta.getString(R.styleable.MessagesRecyclerView_myTextTypeface);
        messageStyleBuilder.myTextTypeface(myTextTypefaceName != null ? Typeface.create(myTextTypefaceName, myTextStyle) : null);
        messageStyleBuilder.myTextSize(ta.getDimensionPixelSize(R.styleable.MessagesRecyclerView_myTextSize, context.getResources().getDimensionPixelSize(R.dimen.layer_ui_text_size_message_item)));

        messageStyleBuilder.otherTextColor(ta.getColor(R.styleable.MessagesRecyclerView_theirTextColor, context.getResources().getColor(R.color.layer_ui_color_primary_blue)));
        int otherTextStyle = ta.getInt(R.styleable.MessagesRecyclerView_theirTextStyle, Typeface.NORMAL);
        messageStyleBuilder.otherTextStyle(otherTextStyle);
        String otherTextTypefaceName = ta.getString(R.styleable.MessagesRecyclerView_theirTextTypeface);
        messageStyleBuilder.otherTextTypeface(otherTextTypefaceName != null ? Typeface.create(otherTextTypefaceName, otherTextStyle) : null);
        messageStyleBuilder.otherTextSize(ta.getDimensionPixelSize(R.styleable.MessagesRecyclerView_theirTextSize, context.getResources().getDimensionPixelSize(R.dimen.layer_ui_text_size_message_item)));

        messageStyleBuilder.myBubbleColor(ta.getColor(R.styleable.MessagesRecyclerView_myBubbleColor, context.getResources().getColor(R.color.layer_ui_color_primary_blue)));
        messageStyleBuilder.otherBubbleColor(ta.getColor(R.styleable.MessagesRecyclerView_theirBubbleColor, context.getResources().getColor(R.color.layer_ui_color_primary_gray)));

        mShouldShowAvatarsInOneOnOneConversations = ta.getBoolean(R.styleable.MessagesRecyclerView_shouldShowAvatarsInOneOnOneConversations, false);
        ta.recycle();
        this.mMessageStyle = messageStyleBuilder.build();
    }
}
