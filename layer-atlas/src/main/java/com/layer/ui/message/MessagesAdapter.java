package com.layer.ui.message;

import android.content.Context;
import android.databinding.ViewDataBinding;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;

import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Identity;
import com.layer.sdk.messaging.Message;
import com.layer.sdk.query.ListViewController;
import com.layer.sdk.query.RecyclerViewController;
import com.layer.ui.adapters.ItemRecyclerViewAdapter;
import com.layer.ui.adapters.ItemViewHolder;
import com.layer.ui.identity.IdentityFormatter;
import com.layer.ui.message.messagetypes.CellFactory;
import com.layer.ui.message.messagetypes.MessageStyle;
import com.layer.ui.util.DateFormatter;
import com.layer.ui.util.IdentityRecyclerViewEventListener;
import com.layer.ui.util.imagecache.ImageCacheWrapper;

import java.util.ArrayList;
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
public abstract class MessagesAdapter<VIEW_HOLDER extends ItemViewHolder<Message, MessageItemViewModel,
        ViewDataBinding, MessageStyle>> extends ItemRecyclerViewAdapter<Message, MessageItemViewModel,
        ViewDataBinding, MessageStyle, VIEW_HOLDER> {

    protected static final String TAG = MessagesAdapter.class.getSimpleName();
    protected final static int VIEW_TYPE_HEADER = 0;
    protected final static int VIEW_TYPE_FOOTER = 1;
    protected final static int VIEW_TYPE_MESSAGE_ITEM = 2;

    protected final Handler mUiThreadHandler;
    protected final DisplayMetrics mDisplayMetrics;
    protected final List<CellFactory> mCellFactories = new ArrayList<>();
    protected final Map<Integer, MessageCell> mCellTypesByViewType;
    protected final Map<CellFactory, Integer> mMyViewTypesByCell =
            new HashMap<CellFactory, Integer>();
    protected final Map<CellFactory, Integer> mTheirViewTypesByCell =
            new HashMap<CellFactory, Integer>();
    protected final IdentityRecyclerViewEventListener mIdentityEventListener;
    // Dates and Clustering
    protected final Map<Uri, MessageCluster> mClusterCache = new HashMap<>();
    protected OnMessageAppendListener mAppendListener;
    // Cells
    protected int mViewTypeCount = VIEW_TYPE_MESSAGE_ITEM;
    protected boolean mIsOneOnOneConversation;
    protected boolean mShouldShowAvatarInOneOnOneConversations;
    protected boolean mShouldShowAvatarPresence = true;

    protected View mHeaderView;
    protected boolean mShouldShowHeader = true;

    protected View mFooterView;
    protected boolean mShouldShowFooter = true;

    protected Integer mRecipientStatusPosition;
    protected boolean mReadReceiptsEnabled = true;
    protected ImageCacheWrapper mImageCacheWrapper;

    protected DateFormatter mDateFormatter;
    protected IdentityFormatter mIdentityFormatter;
    protected Set<Identity> mUsersTyping;

    public MessagesAdapter(Context context, LayerClient layerClient,
                           ImageCacheWrapper imageCacheWrapper, DateFormatter dateFormatter,
                           IdentityFormatter identityFormatter) {
        super(context, layerClient, TAG, false);
        mImageCacheWrapper = imageCacheWrapper;
        mDateFormatter = dateFormatter;
        mIdentityFormatter = identityFormatter;
        mUiThreadHandler = new Handler(Looper.getMainLooper());
        mDisplayMetrics = context.getResources().getDisplayMetrics();
        mCellTypesByViewType = new HashMap<>();

        mQueryController = layerClient.newRecyclerViewController(null, null, this);
        mQueryController.setPreProcessCallback(
                new ListViewController.PreProcessCallback<Message>() {
                    @Override
                    public void onCache(ListViewController listViewController, Message message) {
                        for (CellFactory factory : mCellFactories) {
                            if (factory.isBindable(message)) {
                                factory.getParsedContent(mLayerClient, message);
                                break;
                            }
                        }
                    }
                });
        mIdentityEventListener = new IdentityRecyclerViewEventListener(this);
        mLayerClient.registerEventListener(mIdentityEventListener);
    }

    /**
     * Registers one or more CellFactories for the MessagesAdapter to manage.  CellFactories
     * know which Messages they can render, and handle View caching, creation, and mBinding.
     *
     * @param cellFactories Cells to register.
     */
    public void addCellFactories(List<CellFactory> cellFactories) {
        for (CellFactory cellFactory : cellFactories) {
            cellFactory.setStyle(getStyle());
            mCellFactories.add(cellFactory);

            mViewTypeCount++;
            MessageCell me = new MessageCell(true, cellFactory);
            mCellTypesByViewType.put(mViewTypeCount, me);
            mMyViewTypesByCell.put(cellFactory, mViewTypeCount);

            mViewTypeCount++;
            MessageCell notMe = new MessageCell(false, cellFactory);
            mCellTypesByViewType.put(mViewTypeCount, notMe);
            mTheirViewTypesByCell.put(cellFactory, mViewTypeCount);
        }
    }

    public List<CellFactory> getCellFactories() {
        return mCellFactories;
    }

    protected Map<Integer, MessageCell> getCellTypesByViewType() {
        return mCellTypesByViewType;
    }

    private static boolean isDateBoundary(Date d1, Date d2) {
        if (d1 == null || d2 == null) return false;
        return (d1.getYear() != d2.getYear()) || (d1.getMonth() != d2.getMonth()) || (d1.getDay()
                != d2.getDay());
    }

    /**
     * Performs cleanup when the Activity/Fragment using the adapter is destroyed.
     */
    public void onDestroy() {
        mLayerClient.unregisterEventListener(mIdentityEventListener);
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

    @Override
    public int getItemCount() {
        int itemCount;
        if (mQueryController != null) {
            itemCount = mQueryController.getItemCount();
        } else {
            itemCount = mItems.size();
        }

        return itemCount + ((mFooterView == null) ? 0 : 1);
    }

    public int getHeaderPosition() {
        if (mShouldShowHeader) return 0;
        return -1;
    }

    public int getFooterPosition() {
        if (mShouldShowFooter && mFooterView != null) return getItemCount() - 1;
        return -1;
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
    public boolean getShouldShowAvatarPresence() {
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

    @Override
    public int getItemViewType(int position) {
        if (mShouldShowHeader && mHeaderView != null && position == getHeaderPosition()) {
            return VIEW_TYPE_HEADER;
        }

        if (mShouldShowFooter && mFooterView != null && position == getFooterPosition()) {
            return VIEW_TYPE_FOOTER;
        }

        Message message = getItem(position);
        Identity authenticatedUser = mLayerClient.getAuthenticatedUser();
        boolean isMe = authenticatedUser != null && authenticatedUser.equals(message.getSender());
        for (CellFactory factory : mCellFactories) {
            if (!factory.isBindable(message)) continue;
            return isMe ? mMyViewTypesByCell.get(factory) : mTheirViewTypesByCell.get(factory);
        }
        return -1;
    }

    @Override
    public VIEW_HOLDER onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_HEADER) {
            return createHeaderViewHolder(parent);
        } else if (viewType == VIEW_TYPE_FOOTER) {
            return createFooterViewHolder(parent);
        } else {
            MessageCell messageCell = mCellTypesByViewType.get(viewType);
            return createMessageItemViewHolder(parent, messageCell);
        }
    }

    protected abstract VIEW_HOLDER createHeaderViewHolder(ViewGroup parent);

    protected abstract VIEW_HOLDER createFooterViewHolder(ViewGroup parent);

    protected abstract VIEW_HOLDER createMessageItemViewHolder(ViewGroup parent, MessageCell messageCell);

    @Override
    public void onBindViewHolder(VIEW_HOLDER viewHolder, int position, List<Object> payloads) {
        switch (getItemViewType(position)) {
            case VIEW_TYPE_HEADER:
                bindHeader(viewHolder);
                break;
            case VIEW_TYPE_FOOTER:
                bindFooter(viewHolder);
                break;
            case VIEW_TYPE_MESSAGE_ITEM:
            default:
                prepareAndBindMessageItem(viewHolder, position);
        }
        super.onBindViewHolder(viewHolder, position, payloads);
    }

    public abstract void bindHeader(VIEW_HOLDER viewHolder);

    public abstract void bindFooter(VIEW_HOLDER viewHolder);

    protected void prepareAndBindMessageItem(VIEW_HOLDER viewHolder, int position) {
        Message message = getItem(position);
        viewHolder.setItem(message);

        MessageCluster messageCluster = getClustering(message, position);
        bindMessageItem(viewHolder, messageCluster, position);
    }

    public abstract void bindMessageItem(VIEW_HOLDER viewHolder, MessageCluster cluster, int position);

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

    @Override
    public MessageStyle getStyle() {
        return super.getStyle();
    }

    @Override
    public void setItems(List<Message> messages) {
        super.setItems(messages);
    }

    @Override
    public void setItems(Set<Message> messages) {
        super.setItems(messages);
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

    public boolean isOneOnOneConversation() {
        return mIsOneOnOneConversation;
    }

    //==============================================================================================
    // Clustering
    //==============================================================================================

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
            mRecipientStatusPosition = mQueryController.getItemCount() - 1;
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
