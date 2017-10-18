package com.layer.ui.message;

import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.databinding.Bindable;

import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Identity;
import com.layer.sdk.messaging.Message;
import com.layer.ui.R;
import com.layer.ui.util.IdentityRecyclerViewEventListener;
import com.layer.ui.util.imagecache.ImageCacheWrapper;
import com.layer.ui.viewmodel.ItemViewModel;

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Set;

public class MessageItemCardViewModel extends ItemViewModel<Message> {

    // Config
    private boolean mEnableReadReceipts;
    private IdentityRecyclerViewEventListener mIdentityEventListener;
    private boolean mShowAvatars;
    private boolean mShowPresence;
    private ImageCacheWrapper mImageCacheWrapper;

    // View related variables
    private boolean mIsClusterSpaceVisible;
    private boolean mShouldShowDisplayName;
    private boolean mShouldShowDateTimeForMessage;
    private String mTimeGroupDay;
    private float mMessageCellAlpha;
    private String mSenderName;
    private Set<Identity> mParticipants;
    private String mReadReceipt;
    private String mGroupTime;
    private boolean mIsReadReceiptVisible;
    private boolean mShouldDisplayAvatarSpace;
    private boolean mIsMyMessage;
    private boolean mIsAvatarViewVisible;
    private boolean mIsPresenceVisible;

    private LiveData<Identity> mAuthenticatedUser;
    
    public MessageItemCardViewModel(Context context, LayerClient layerClient,
                                    ImageCacheWrapper imageCacheWrapper,
                                    IdentityRecyclerViewEventListener identityEventListener) {
        super(context, layerClient);
        mEnableReadReceipts = true;
        mShowAvatars = true;
        mShowPresence = true;
        mIdentityEventListener = identityEventListener;
        mImageCacheWrapper = imageCacheWrapper;

        // TODO Handle updates
        mAuthenticatedUser = layerClient.getAuthenticatedUserLive();
    }

    public void update(MessageCluster cluster, int position, Integer recipientStatusPosition) {
        Message message = getItem();
        mParticipants = Collections.singleton(message.getSender());
        mIsMyMessage = getItem().getSender().equals(mAuthenticatedUser.getValue());

        // Clustering and dates
        updateClusteringAndDates(message, cluster);

        // Sender-dependent elements
        updateSenderDependentElements(message, cluster, position, recipientStatusPosition);
        notifyChange();
    }

    protected void updateClusteringAndDates(Message message, MessageCluster cluster) {
        if (cluster.mClusterWithPrevious == null) {
            // No previous message, so no gap
            mIsClusterSpaceVisible = false;
            updateReceivedAtDateAndTime(message);
        } else if (cluster.mDateBoundaryWithPrevious || cluster.mClusterWithPrevious == MessageCluster.Type.MORE_THAN_HOUR) {
            // Crossed into a new day, or > 1hr lull in conversation
            updateReceivedAtDateAndTime(message);
            mIsClusterSpaceVisible = false;
        } else if (cluster.mClusterWithPrevious == MessageCluster.Type.LESS_THAN_MINUTE) {
            // Same sender with < 1m gap
            mIsClusterSpaceVisible = false;
            mShouldShowDateTimeForMessage = false;
        } else if (cluster.mClusterWithPrevious == MessageCluster.Type.NEW_SENDER) {
            // New sender or > 1m gap
            mIsClusterSpaceVisible = true;
            mShouldShowDateTimeForMessage = false;
        }
    }

    protected void updateReceivedAtDateAndTime(Message message) {
        Date receivedAt = message.getReceivedAt();
        if (receivedAt == null) receivedAt = new Date();

        mTimeGroupDay = getDateFormatter().formatTimeDay(receivedAt);
        mGroupTime = getDateFormatter().formatTime(receivedAt);

        mShouldShowDateTimeForMessage = true;
    }

    protected void updateSenderDependentElements(Message message, MessageCluster cluster, int position,
                                                 Integer recipientStatusPosition) {
        Identity sender = message.getSender();

        if (mIsMyMessage) {
            updateWithRecipientStatus(message, position, recipientStatusPosition);
            mMessageCellAlpha = message.isSent() ? 1.0f : 0.5f;
        } else {
            mMessageCellAlpha = 1.0f;

            if (mEnableReadReceipts) {
                message.markAsRead();
            }

            // Sender name, only for first message in cluster
            if (!isInAOneOnOneConversation() &&
                    (cluster.mClusterWithPrevious == null
                            || cluster.mClusterWithPrevious == MessageCluster.Type.NEW_SENDER)) {
                if (sender != null) {
                    mSenderName = getIdentityFormatter().getDisplayName(sender);
                } else {
                    mSenderName = getIdentityFormatter().getUnknownNameString();
                }
                mShouldShowDisplayName = true;

                // Add the position to the positions map for Identity updates
                mIdentityEventListener.addIdentityPosition(position, Collections.singleton(sender));
            } else {
                mShouldShowDisplayName = false;
            }
        }

        // Avatars
        if (isInAOneOnOneConversation()) {
            if (mShowAvatars) {
                mIsAvatarViewVisible = !mIsMyMessage;
                mShouldDisplayAvatarSpace = true;
                mIsPresenceVisible = mIsAvatarViewVisible && mShowPresence;
            } else {
                mIsAvatarViewVisible = false;
                mShouldDisplayAvatarSpace = false;
                mIsPresenceVisible = false;
            }
        } else if (cluster.mClusterWithNext == null || cluster.mClusterWithNext != MessageCluster.Type.LESS_THAN_MINUTE) {
            // Last message in cluster
            mIsAvatarViewVisible = !mIsMyMessage;
            // Add the position to the positions map for Identity updates
            mIdentityEventListener.addIdentityPosition(position, Collections.singleton(message.getSender()));
            mShouldDisplayAvatarSpace = true;
            mIsPresenceVisible = mIsAvatarViewVisible && mShowPresence;
        } else {
            // Invisible for clustered messages to preserve proper spacing
            mIsAvatarViewVisible = false;
            mShouldDisplayAvatarSpace = true;
            mIsPresenceVisible = false;
        }
    }

    protected void updateWithRecipientStatus(Message message, int position, Integer recipientStatusPosition) {
        if (mEnableReadReceipts && recipientStatusPosition != null && position == recipientStatusPosition) {
            int readCount = 0;
            boolean delivered = false;
            Map<Identity, Message.RecipientStatus> statuses = message.getRecipientStatus();
            for (Map.Entry<Identity, Message.RecipientStatus> entry : statuses.entrySet()) {
                // Only show receipts for other members
                if (entry.getKey().equals(mAuthenticatedUser.getValue())) continue;
                // Skip receipts for members no longer in the conversation
                if (entry.getValue() == null) continue;

                switch (entry.getValue()) {
                    case READ:
                        readCount++;
                        break;
                    case DELIVERED:
                        delivered = true;
                        break;
                }
            }

            if (readCount > 0) {
                mIsReadReceiptVisible = true;

                // Use 2 to include one other participant plus the current user
                if (statuses.size() > 2) {
                    mReadReceipt = getContext().getResources()
                            .getQuantityString(R.plurals.layer_ui_message_item_read_muliple_participants, readCount, readCount);
                } else {
                    mReadReceipt = getContext().getString(R.string.layer_ui_message_item_read);
                }
            } else if (delivered) {
                mIsReadReceiptVisible = true;
                mReadReceipt = getContext().getString(R.string.layer_ui_message_item_delivered);
            } else {
                mIsReadReceiptVisible = false;
            }
        } else {
            mIsClusterSpaceVisible = false;
        }
    }

    protected boolean isInAOneOnOneConversation() {
        return getItem().getMessagingPattern().getParticipants().size() == 2;
    }

    // Setters

    public void setShowAvatars(boolean showAvatars) {
        mShowAvatars = showAvatars;
    }

    public void setShowPresence(boolean showPresence) {
        mShowPresence = showPresence;
    }

    public void setEnableReadReceipts(boolean enableReadReceipts) {
        mEnableReadReceipts = enableReadReceipts;
    }

    // Getters

    public ImageCacheWrapper getImageCacheWrapper() {
        return mImageCacheWrapper;
    }

    // Bindable properties

    @Bindable
    public boolean isClusterSpaceVisible() {
        return mIsClusterSpaceVisible;
    }

    @Bindable
    public boolean getAvatarVisibility() {
        return mIsAvatarViewVisible;
    }

    @Bindable
    public boolean isPresenceVisible() {
        return mIsPresenceVisible;
    }

    @Bindable
    public Set<Identity> getParticipants() {
        return mParticipants;
    }

    @Bindable
    public String getTimeGroupDay() {
        return mTimeGroupDay;
    }

    @Bindable
    public boolean getShouldShowDateTimeForMessage() {
        return mShouldShowDateTimeForMessage;
    }

    @Bindable
    public String getReadReceipt() {
        return mReadReceipt;
    }

    @Bindable
    public float getMessageCellAlpha() {
        return mMessageCellAlpha;
    }

    @Bindable
    public String getSenderName() {
        return mSenderName;
    }

    @Bindable
    public boolean getShouldShowDisplayName() {
        return mShouldShowDisplayName;
    }

    @Bindable
    public String getGroupTime() {
        return mGroupTime;
    }

    @Bindable
    public boolean isReadReceiptVisible() {
        return mIsReadReceiptVisible;
    }

    @Bindable
    public boolean isMyMessage() {
        return mIsMyMessage;
    }

    @Bindable
    public boolean getShouldDisplayAvatarSpace() {
        return mShouldDisplayAvatarSpace;
    }
}
