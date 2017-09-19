package com.layer.ui.conversationitem;

import android.databinding.Bindable;

import com.layer.sdk.messaging.Conversation;
import com.layer.sdk.messaging.Identity;
import com.layer.ui.recyclerview.OnItemClickListener;

import java.util.HashSet;
import java.util.Set;

import com.layer.ui.fourpartitem.FourPartItemViewModel;

public class ConversationItemViewModel extends FourPartItemViewModel<Conversation> {
    //View Logic
    protected ConversationItemFormatter mConversationItemFormatter;
    protected Identity mAuthenticatedUser;

    // View Data
    protected Set<Identity> mParticipantsMinusAuthenticatedUser;

    public ConversationItemViewModel() {
        super();
        mParticipantsMinusAuthenticatedUser = new HashSet<>();
    }

    @Override
    public void setItem(Conversation conversation) {
        super.setItem(conversation);

        mParticipantsMinusAuthenticatedUser.clear();

        mParticipantsMinusAuthenticatedUser.addAll(conversation.getParticipants());
        mParticipantsMinusAuthenticatedUser.remove(mAuthenticatedUser);

        notifyChange();
    }

    public void setAuthenticatedUser(Identity authenticatedUser) {
        mAuthenticatedUser = authenticatedUser;
    }

    public void setConversationItemFormatter(ConversationItemFormatter conversationItemFormatter) {
        mConversationItemFormatter = conversationItemFormatter;
    }

    @Bindable
    public String getTitle() {
        return mConversationItemFormatter.getConversationTitle(mAuthenticatedUser, getItem(), getItem().getParticipants());
    }

    @Bindable
    public String getSubtitle() {
        return mConversationItemFormatter.getLastMessagePreview(getItem());
    }

    @Override
    public String getAccessoryText() {
        return mConversationItemFormatter.getTimeStamp(getItem());
    }

    @Override
    public boolean isSecondaryState() {
        return getItem().getTotalUnreadMessageCount() > 0;
    }

    @Override
    public Set<Identity> getIdentities() {
        return mParticipantsMinusAuthenticatedUser;
    }
}
