package com.layer.ui.message;

import android.content.Context;
import android.databinding.ViewDataBinding;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;

import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Identity;
import com.layer.sdk.messaging.Message;
import com.layer.sdk.query.ListViewController;
import com.layer.sdk.query.Query;
import com.layer.sdk.query.RecyclerViewController;
import com.layer.ui.adapters.ItemRecyclerViewAdapter;
import com.layer.ui.identity.IdentityFormatter;
import com.layer.ui.message.binder.BinderRegistry;
import com.layer.ui.message.messagetypes.CellFactory;
import com.layer.ui.message.messagetypes.MessageStyle;
import com.layer.ui.util.DateFormatter;
import com.layer.ui.util.IdentityRecyclerViewEventListener;
import com.layer.ui.util.imagecache.ImageCacheWrapper;
import com.layer.ui.viewmodel.ItemViewModel;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * MessagesAdapter drives an AtlasMessagesList.  The MessagesAdapter itself handles
 * rendering sender names, avatars, dates, left/right alignment, and message clustering, and leaves
 * rendering message content up to registered CellFactories.  Each CellFactory knows which Messages
 * it can render, can create new View hierarchies for its Message types, and can render (bind)
 * Message data with its created View hierarchies.  Typically, CellFactories are segregated by
 * MessagePart MIME types (e.g. "text/plain", "image/jpeg", and "application/vnd.geo+json").
 * <p>
 * Under the hood, the MessagesAdapter is a RecyclerView.Adapter, which automatically recycles
 * its list items within view-type "buckets".  Each registered CellFactory actually creates two such
 * view-types: one for cells sent by the authenticated user, and another for cells sent by remote
 * actors.  This allows the MessagesAdapter to efficiently render images sent by the current
 * user aligned on the left, and images sent by others aligned on the right, for example.  In case
 * this sent-by distinction is of value when rendering cells, it provided as the `isMe` argument.
 * <p>
 * When rendering Messages, the MessagesAdapter first determines which CellFactory to handle
 * the Message with calling CellFactory.isBindable() on each of its registered CellFactories. The
 * first CellFactory to return `true` is used for that Message.  Then, the adapter checks for
 * available CellHolders of that type.  If none are found, a new one is created with a call to
 * CellFactory.createCellHolder().  After creating a new CellHolder (or reusing an available one),
 * the CellHolder is rendered in the UI with Message data via CellFactory.bindCellHolder().
 *
 * @see CellFactory
 */
public abstract class MessagesAdapter<VIEW_MODEL extends ItemViewModel<Message>, BINDING extends ViewDataBinding>
        extends ItemRecyclerViewAdapter<Message, VIEW_MODEL,
        BINDING, MessageStyle, MessageItemViewHolder<VIEW_MODEL, BINDING>> {

    protected static final String TAG = MessagesAdapter.class.getSimpleName();

    private final Handler mUiThreadHandler;
    private final DisplayMetrics mDisplayMetrics;
    private final IdentityRecyclerViewEventListener mIdentityEventListener;
    private final RecyclerView.OnScrollListener mOnScrollListener;

    // Dates and Clustering
    private final Map<Uri, MessageCluster> mClusterCache = new HashMap<>();
    private OnMessageAppendListener mAppendListener;

    private BinderRegistry mBinderRegistry;
    private boolean mIsOneOnOneConversation;
    private boolean mShouldShowAvatarInOneOnOneConversations;
    private boolean mShouldShowAvatarPresence = true;

    private View mHeaderView;
    private boolean mShouldShowHeader = true;

    private View mFooterView;
    private boolean mShouldShowFooter = true;

    private Integer mRecipientStatusPosition;
    private boolean mReadReceiptsEnabled = true;
    private ImageCacheWrapper mImageCacheWrapper;

    private DateFormatter mDateFormatter;
    private IdentityFormatter mIdentityFormatter;
    private Set<Identity> mUsersTyping;

    public MessagesAdapter(Context context, LayerClient layerClient,
                           ImageCacheWrapper imageCacheWrapper, DateFormatter dateFormatter,
                           IdentityFormatter identityFormatter) {
        super(context, layerClient, TAG, false);
        mImageCacheWrapper = imageCacheWrapper;
        mDateFormatter = dateFormatter;
        mIdentityFormatter = identityFormatter;
        mUiThreadHandler = new Handler(Looper.getMainLooper());
        mDisplayMetrics = context.getResources().getDisplayMetrics();
        mBinderRegistry = new BinderRegistry(context, layerClient);

        mIdentityEventListener = new IdentityRecyclerViewEventListener(this);
        getLayerClient().registerEventListener(mIdentityEventListener);

        mOnScrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                mBinderRegistry.notifyScrollStateChange(newState);
            }
        };
    }

    @Override
    public void setQuery(Query<Message> query, Collection<String> updateAttributes) {
        super.setQuery(query, updateAttributes);
        getQueryController().setPreProcessCallback(new RecyclerViewController.PreProcessCallback<Message>() {
            @Override
            public void onCache(ListViewController listViewController, Message message) {
                mBinderRegistry.cacheContent(message);
            }
        });
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        recyclerView.addOnScrollListener(mOnScrollListener);
        recyclerView.getRecycledViewPool().setMaxRecycledViews(mBinderRegistry.VIEW_TYPE_CARD, 0);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        recyclerView.removeOnScrollListener(mOnScrollListener);
    }

    /**
     * Performs cleanup when the Activity/Fragment using the adapter is destroyed.
     */
    public void onDestroy() {
        getLayerClient().unregisterEventListener(mIdentityEventListener);
    }

    protected BinderRegistry getBinderRegistry() {
        return mBinderRegistry;
    }

    protected Integer getRecipientStatusPosition() {
        return mRecipientStatusPosition;
    }

    protected DateFormatter getDateFormatter() {
        return mDateFormatter;
    }

    protected IdentityFormatter getIdentityFormatter() {
        return mIdentityFormatter;
    }

    protected IdentityRecyclerViewEventListener getIdentityEventListener() {
        return mIdentityEventListener;
    }

    public boolean getShouldShowHeader() {
        return mShouldShowHeader;
    }

    public void setShouldShowHeader(boolean shouldShowHeader) {
        mShouldShowHeader = shouldShowHeader;
    }

    public boolean getShouldShowFooter() {
        return mShouldShowFooter;
    }

    public void setShouldShowFooter(boolean shouldShowFooter) {
        mShouldShowFooter = shouldShowFooter;
    }

    public void setIsOneOnOneConversation(boolean oneOnOneConversation) {
        mIsOneOnOneConversation = oneOnOneConversation;
    }

    protected boolean isOneOnOneConversation() {
        return mIsOneOnOneConversation;
    }

    public void setBinderRegistry(BinderRegistry binderRegistry) {
        mBinderRegistry = binderRegistry;
    }

    public View getFooterView() {
        return mFooterView;
    }

    public void setFooterView(View footerView, Set<Identity> users) {
        if (mShouldShowFooter) {
            int footerPosition = getFooterPosition();
            boolean isNull = footerView == null;
            boolean wasNull = mFooterView == null;
            mFooterView = footerView;
            mUsersTyping = users;

            footerPosition = footerPosition > 0 ? footerPosition : getFooterPosition();

            if (wasNull && !isNull) {
                // Insert
                notifyItemInserted(footerPosition);
            } else if (!wasNull && isNull) {
                // Delete
                notifyItemRemoved(footerPosition);
            } else if (!wasNull && !isNull) {
                // Change
                notifyItemChanged(footerPosition);
            }
        }
    }

    protected Set<Identity> getUsersTyping() {
        return mUsersTyping;
    }

    public View getHeaderView() {
        return mHeaderView;
    }

    public void setHeaderView(View headerView) {
        if (mShouldShowHeader) {
            int headerPosition = getHeaderPosition();

            boolean isNull = headerView == null;
            boolean wasNull = mHeaderView == null;
            mFooterView = headerView;

            if (wasNull && !isNull) {
                // Insert
                notifyItemInserted(headerPosition);
            } else if (!wasNull && isNull) {
                // Delete
                notifyItemRemoved(headerPosition);
            } else if (!wasNull && !isNull) {
                // Change
                notifyItemChanged(headerPosition);
            }
        }
    }

    /**
     * @return If the AvatarViewModel for the other participant in a one on one conversation  will
     * be shown
     * or not
     */
    public boolean getShouldShowAvatarInOneOnOneConversations() {
        return mShouldShowAvatarInOneOnOneConversations;
    }

    /**
     * @param shouldShowAvatarInOneOnOneConversations Whether the AvatarViewModel for the other
     *                                                participant
     *                                                in a one on one conversation should be shown
     *                                                or not
     */
    public void setShouldShowAvatarInOneOnOneConversations(
            boolean shouldShowAvatarInOneOnOneConversations) {
        mShouldShowAvatarInOneOnOneConversations = shouldShowAvatarInOneOnOneConversations;
    }

    /**
     * @return If the AvatarViewModel for the other participant in a one on one conversation will be
     * shown
     * or not. Defaults to `true`.
     */
    public boolean getShouldShowPresence() {
        return mShouldShowAvatarPresence;
    }

    /**
     * @param shouldShowPresence Whether the AvatarView for the other participant in a one on one
     *                           conversation should be shown or not. Default is `true`.
     */
    public void setShouldShowAvatarPresence(boolean shouldShowPresence) {
        mShouldShowAvatarPresence = shouldShowPresence;
    }

    protected ImageCacheWrapper getImageCacheWrapper() {
        return mImageCacheWrapper;
    }

    public boolean areReadReceiptsEnabled() {
        return mReadReceiptsEnabled;
    }

    //==============================================================================================
    // Listeners
    //==============================================================================================

    /**
     * Set whether or not the conversation supports read receipts. This determines if the read
     * receipts should be shown in the view holders.
     *
     * @param readReceiptsEnabled true if the conversation is adapter is used for supports read
     *                            receipts
     */
    public void setReadReceiptsEnabled(boolean readReceiptsEnabled) {
        mReadReceiptsEnabled = readReceiptsEnabled;
    }


    //==============================================================================================
    // Adapter and Cells
    //==============================================================================================

    /**
     * Sets the OnAppendListener for this AtlasQueryAdapter.  The listener will be called when items
     * are appended to the end of this adapter.  This is useful for implementing a scroll-to-bottom
     * feature.
     *
     * @param listener The OnAppendListener to notify about appended items.
     * @return This AtlasQueryAdapter.
     */
    public void setOnMessageAppendListener(OnMessageAppendListener listener) {
        mAppendListener = listener;
    }

    public int getHeaderPosition() {
        if (mShouldShowHeader && mHeaderView != null) return 0;
        return -1;
    }

    public int getFooterPosition() {
        if (mShouldShowFooter && mFooterView != null) return getItemCount() - 1;
        return -1;
    }

    @Override
    public int getItemCount() {
        int itemCount;
        if (getQueryController() != null) {
            itemCount = getQueryController().getItemCount();
        } else {
            itemCount = getItems().size();
        }

        return itemCount + ((mFooterView == null) ? 0 : 1) + (mHeaderView == null ? 0 : 1);
    }

    @Override
    public int getItemViewType(int position) {
        if (mShouldShowHeader && mHeaderView != null && position == getHeaderPosition()) {
            return mBinderRegistry.VIEW_TYPE_HEADER;
        }

        if (mShouldShowFooter && mFooterView != null && position == getFooterPosition()) {
            return mBinderRegistry.VIEW_TYPE_FOOTER;
        }

        return mBinderRegistry.getViewType(getItem(position));
    }

    @Override
    public MessageItemViewHolder<VIEW_MODEL, BINDING> onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == mBinderRegistry.VIEW_TYPE_HEADER) {
            return createHeaderViewHolder(parent);
        } else if (viewType == mBinderRegistry.VIEW_TYPE_FOOTER) {
            return createFooterViewHolder(parent);
        } else if (viewType >= mBinderRegistry.VIEW_TYPE_LEGACY_START && viewType <= mBinderRegistry.VIEW_TYPE_LEGACY_END) {
            MessageCell messageCell = mBinderRegistry.getMessageCellForViewType(viewType);
            messageCell.mCellFactory.setStyle(getStyle());
            return createLegacyMessageItemViewHolder(parent, messageCell);
        } else if (viewType != mBinderRegistry.VIEW_TYPE_UNKNOWN) {
            return createCardMessageItemViewHolder(parent);
        } else {
            throw new IllegalStateException("Unknown View Type");
        }
    }

    protected abstract MessageItemViewHolder<VIEW_MODEL, BINDING> createHeaderViewHolder(ViewGroup parent);

    protected abstract MessageItemViewHolder<VIEW_MODEL, BINDING> createFooterViewHolder(ViewGroup parent);

    protected abstract MessageItemViewHolder<VIEW_MODEL, BINDING> createCardMessageItemViewHolder(ViewGroup parent);

    protected abstract MessageItemViewHolder<VIEW_MODEL, BINDING> createLegacyMessageItemViewHolder(ViewGroup parent, MessageCell messageCell);

    @Override
    public void onBindViewHolder(MessageItemViewHolder<VIEW_MODEL, BINDING> viewHolder, int position, List<Object> payloads) {
        int viewType = getItemViewType(position);
        if (viewType == mBinderRegistry.VIEW_TYPE_HEADER) {
            bindHeader(viewHolder);
        } else if (viewType == mBinderRegistry.VIEW_TYPE_FOOTER) {
            bindFooter(viewHolder);
        } else if (viewType >= mBinderRegistry.VIEW_TYPE_LEGACY_START && viewType <= mBinderRegistry.VIEW_TYPE_LEGACY_END) {
            prepareAndBindMessageItem(viewHolder, position);
        } else {
            prepareAndBindCard(viewHolder, position);
        }

        super.onBindViewHolder(viewHolder, position, payloads);
    }

    public abstract void bindHeader(MessageItemViewHolder<VIEW_MODEL, BINDING> viewHolder);

    public abstract void bindFooter(MessageItemViewHolder<VIEW_MODEL, BINDING> viewHolder);

    protected void prepareAndBindCard(MessageItemViewHolder<VIEW_MODEL, BINDING> viewHolder, int position) {
        Message message = getItem(position);
        viewHolder.setItem(message);

        MessageCluster messageCluster = getClustering(message, position);
        bindCardMessageItem(viewHolder, messageCluster, position);
    }

    public abstract void bindCardMessageItem(MessageItemViewHolder<VIEW_MODEL, BINDING> viewHolder, MessageCluster messageCluster, int position);

    protected void prepareAndBindMessageItem(MessageItemViewHolder<VIEW_MODEL, BINDING> viewHolder, int position) {
        Message message = getItem(position);
        viewHolder.setItem(message);

        MessageCluster messageCluster = getClustering(message, position);
        bindLegacyMessageItem(viewHolder, messageCluster, position);
    }

    public abstract void bindLegacyMessageItem(MessageItemViewHolder<VIEW_MODEL, BINDING> viewHolder, MessageCluster cluster, int position);

    //==============================================================================================
    // Clustering
    //==============================================================================================

    private static boolean isDateBoundary(Date d1, Date d2) {
        if (d1 == null || d2 == null) return false;
        return (d1.getYear() != d2.getYear()) || (d1.getMonth() != d2.getMonth()) || (d1.getDay()
                != d2.getDay());
    }

    // TODO: optimize by limiting search to positions in- and around- visible range
    protected MessageCluster getClustering(Message message, int position) {
        MessageCluster result = mClusterCache.get(message.getId());
        if (result == null) {
            result = new MessageCluster();
            mClusterCache.put(message.getId(), result);
        }

        int previousPosition = position - 1;
        Message previousMessage = (previousPosition >= 0) ? getItem(previousPosition) : null;
        if (previousMessage != null) {
            result.mDateBoundaryWithPrevious = isDateBoundary(previousMessage.getReceivedAt(),
                    message.getReceivedAt());
            result.mClusterWithPrevious = MessageCluster.Type.fromMessages(previousMessage, message);

            MessageCluster previousMessageCluster = mClusterCache.get(previousMessage.getId());
            if (previousMessageCluster == null) {
                previousMessageCluster = new MessageCluster();
                mClusterCache.put(previousMessage.getId(), previousMessageCluster);
            } else {
                // does the previous need to change its clustering?
                if ((previousMessageCluster.mClusterWithNext != result.mClusterWithPrevious) ||
                        (previousMessageCluster.mDateBoundaryWithNext
                                != result.mDateBoundaryWithPrevious)) {
                    requestUpdate(previousMessage, previousPosition);
                }
            }
            previousMessageCluster.mClusterWithNext = result.mClusterWithPrevious;
            previousMessageCluster.mDateBoundaryWithNext = result.mDateBoundaryWithPrevious;
        }

        int nextPosition = position + 1;
        Message nextMessage = (nextPosition < getItemCount()) ? getItem(nextPosition) : null;
        if (nextMessage != null) {
            result.mDateBoundaryWithNext = isDateBoundary(message.getReceivedAt(),
                    nextMessage.getReceivedAt());
            result.mClusterWithNext = MessageCluster.Type.fromMessages(message, nextMessage);

            MessageCluster nextMessageCluster = mClusterCache.get(nextMessage.getId());
            if (nextMessageCluster == null) {
                nextMessageCluster = new MessageCluster();
                mClusterCache.put(nextMessage.getId(), nextMessageCluster);
            } else {
                // does the next need to change its clustering?
                if ((nextMessageCluster.mClusterWithPrevious != result.mClusterWithNext) ||
                        (nextMessageCluster.mDateBoundaryWithPrevious != result.mDateBoundaryWithNext)) {
                    requestUpdate(nextMessage, nextPosition);
                }
            }
            nextMessageCluster.mClusterWithPrevious = result.mClusterWithNext;
            nextMessageCluster.mDateBoundaryWithPrevious = result.mDateBoundaryWithNext;
        }

        return result;
    }

    private void requestUpdate(final Message message, final int lastPosition) {
        mUiThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                notifyItemChanged(getPosition(message, lastPosition));
            }
        });
    }


    //==============================================================================================
    // Read and delivery receipts
    //==============================================================================================

    private void updateRecipientStatusPosition() {
        if (mReadReceiptsEnabled) {
            Integer oldPosition = mRecipientStatusPosition;
            // Set new position to last in the list
            mRecipientStatusPosition = getQueryController().getItemCount() - 1;
            if (oldPosition != null) {
                notifyItemChanged(oldPosition);
            }
        }
    }


    //==============================================================================================
    // UI update callbacks
    //==============================================================================================

    @Override
    public void onQueryDataSetChanged(RecyclerViewController controller) {
        updateRecipientStatusPosition();
        super.onQueryDataSetChanged(controller);
    }

    @Override
    public void onQueryItemChanged(RecyclerViewController controller, int position) {
        updateRecipientStatusPosition();
        super.onQueryItemChanged(controller, position);
    }

    @Override
    public void onQueryItemInserted(RecyclerViewController controller, int position) {
        updateRecipientStatusPosition();
        super.onQueryItemInserted(controller, position);

        if (mAppendListener != null && (position + 1) == getItemCount()) {
            mAppendListener.onMessageAppend(this, getItem(position));
        }
    }

    @Override
    public void onQueryItemRangeInserted(RecyclerViewController controller, int positionStart,
                                         int itemCount) {
        updateRecipientStatusPosition();
        super.onQueryItemRangeInserted(controller, positionStart, itemCount);

        int positionEnd = positionStart + itemCount;
        if (mAppendListener != null && (positionEnd + 1) == getItemCount()) {
            mAppendListener.onMessageAppend(this, getItem(positionEnd));
        }
    }

    @Override
    public void onQueryItemRemoved(RecyclerViewController controller, int position) {
        updateRecipientStatusPosition();
        super.onQueryItemRemoved(controller, position);
    }

    @Override
    public void onQueryItemRangeRemoved(RecyclerViewController controller, int positionStart,
                                        int itemCount) {
        updateRecipientStatusPosition();
        super.onQueryItemRangeRemoved(controller, positionStart, itemCount);
    }

    @Override
    public void onQueryItemMoved(RecyclerViewController controller, int fromPosition,
                                 int toPosition) {
        updateRecipientStatusPosition();
        super.onQueryItemMoved(controller, fromPosition, toPosition);
    }

    /**
     * Listens for inserts to the end of an AtlasQueryAdapter.
     */
    public interface OnMessageAppendListener {
        /**
         * Alerts the listener to inserts at the end of an AtlasQueryAdapter.  If a batch of items
         * were appended, only the last one will be alerted here.
         *
         * @param adapter The AtlasQueryAdapter which had an item appended.
         * @param message The item appended to the AtlasQueryAdapter.
         */
        void onMessageAppend(MessagesAdapter adapter, Message message);
    }
}