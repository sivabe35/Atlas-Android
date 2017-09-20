package com.layer.ui.conversation;

import android.content.Context;
import android.util.AttributeSet;

import com.layer.sdk.messaging.Conversation;
import com.layer.ui.adapters.ConversationItemsAdapter;

import com.layer.ui.adapters.ItemRecyclerViewAdapter;
import com.layer.ui.fourpartitem.FourPartItemsListView;
import com.layer.ui.util.views.SwipeableItem;

/**
 * ConversationItemsListView list Conversations, to use ConversationItemsListView, an object of the
 * {@link ConversationItemsAdapter} must be set on {@link ConversationItemsListView#setAdapter(ItemRecyclerViewAdapter)}.
 * To register a swipe Listener, set {@link com.layer.ui.util.views.SwipeableItem.OnItemSwipeListener}
 * on {@link ConversationItemsListView#setItemSwipeListener(SwipeableItem.OnItemSwipeListener)}
 * We set these values in the xml by binding an object of {@link ConversationItemsListViewModel} to
 * the view
 */

public class ConversationItemsListView extends FourPartItemsListView<Conversation, ConversationItemsAdapter> {

    public ConversationItemsListView(Context context) {
        super(context);
    }

    public ConversationItemsListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
}
