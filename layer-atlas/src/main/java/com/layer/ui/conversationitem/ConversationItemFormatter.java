package com.layer.ui.conversationitem;

import android.content.Context;

import com.layer.sdk.messaging.Conversation;
import com.layer.sdk.messaging.Identity;
import com.layer.sdk.messaging.Message;
import com.layer.ui.R;
import com.layer.ui.message.messagetypes.CellFactory;
import com.layer.ui.message.messagetypes.generic.GenericCellFactory;
import com.layer.ui.util.Util;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * A formatter that enables us to modify conversation recyclerView items in the UI
 */

public class ConversationItemFormatter {
    protected static final String METADATA_KEY_CONVERSATION_TITLE = "conversationName";
    protected static final int TIME_HOURS_24 = 24 * 60 * 60 * 1000;

    protected Context mContext;
    protected DateFormat mTimeFormat;
    protected DateFormat mDateFormat;
    // TODO : This is a bad place for this to exist. Need to find a better way in round 2
    protected List<CellFactory> mCellFactories;

    public ConversationItemFormatter(Context context, DateFormat timeFormat, DateFormat dateFormat, List<CellFactory> cellFactories) {
        mContext = context;
        mTimeFormat = timeFormat;
        mDateFormat = dateFormat;
        mCellFactories = cellFactories;
    }

    public void setMetaDataTitleOnConversation(Conversation conversation, String title) {
        if (title == null || title.trim().isEmpty()) {
            conversation.removeMetadataAtKeyPath(METADATA_KEY_CONVERSATION_TITLE);
        } else {
            conversation.putMetadataAtKeyPath(METADATA_KEY_CONVERSATION_TITLE, title.trim());
        }
    }

    public String getConversationTitle(Identity authenticatedUser, Conversation conversation) {
        return getConversationTitle(authenticatedUser, conversation, conversation.getParticipants());
    }

    public String getConversationTitle(Identity authenticatedUser, Conversation conversation, Set<Identity> participants) {
        String metadataTitle = getConversationMetadataTitle(conversation);
        if (metadataTitle != null) return metadataTitle.trim();

        StringBuilder sb = new StringBuilder();
        for (Identity participant : participants) {
            if (participant.equals(authenticatedUser)) continue;
            String initials = participants.size() > 2 ? Util.getInitials(participant) : Util.getDisplayName(participant);
            if (sb.length() > 0) sb.append(", ");
            sb.append(initials);
        }
        return sb.toString();
    }

    public String getConversationMetadataTitle(Conversation conversation) {
        if (conversation.getMetadata() != null) {
            String metadataTitle = (String) conversation.getMetadata().get(METADATA_KEY_CONVERSATION_TITLE);
            if (metadataTitle != null && !metadataTitle.trim().isEmpty())
                return metadataTitle.trim();
        }
        return null;
    }

    public String getTimeStamp(Conversation conversation) {
        Message lastMessage = conversation.getLastMessage();

        if (lastMessage != null && lastMessage.getReceivedAt() != null) {
            return formatTime(lastMessage.getReceivedAt());
        }

        return null;
    }

    public String getLastMessagePreview(Conversation conversation) {
        Message message = conversation.getLastMessage();

        if (message!= null && mCellFactories != null && !mCellFactories.isEmpty()) {
            for (CellFactory cellFactory : mCellFactories) {
                if (cellFactory.isType(message)) {
                    return cellFactory.getPreviewText(mContext, message);
                }
            }
        }

        return GenericCellFactory.getPreview(mContext, message);
    }

    protected String formatTime(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        long todayMidnight = cal.getTimeInMillis();
        long yesterMidnight = todayMidnight - TIME_HOURS_24;
        long weekAgoMidnight = todayMidnight - TIME_HOURS_24 * 7;

        String timeText;
        if (date.getTime() > todayMidnight) {
            timeText = mTimeFormat.format(date.getTime());
        } else if (date.getTime() > yesterMidnight) {
            timeText = mContext.getString(R.string.layer_ui_time_yesterday);
        } else if (date.getTime() > weekAgoMidnight) {
            cal.setTime(date);
            timeText = mContext.getResources().getStringArray(R.array.layer_ui_time_days_of_week)[cal.get(Calendar.DAY_OF_WEEK) - 1];
        } else {
            timeText = mDateFormat.format(date);
        }
        return timeText;
    }

    public void setCellFactories(List<CellFactory> cellFactories) {
        mCellFactories = cellFactories;
    }
}

