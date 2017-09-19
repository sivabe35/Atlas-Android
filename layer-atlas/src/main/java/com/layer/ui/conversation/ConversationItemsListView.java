package com.layer.ui.conversation;

import android.content.Context;
import android.util.AttributeSet;

import com.layer.sdk.messaging.Conversation;
import com.layer.ui.adapters.ConversationItemsAdapter;

import com.layer.ui.fourpartitem.FourPartItemsListView;

/**
 * ConversationItemsListView list Conversations, the adapter, itemHeight and adapter are set from the
 * {@link ConversationItemsListViewModel}. ConversationItemsListView adjusts itself to the
 * item height specified.
 */

public class ConversationItemsListView extends FourPartItemsListView<Conversation, ConversationItemsAdapter> {

    public ConversationItemsListView(Context context) {
        super(context);
    }

    public ConversationItemsListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
}
