package com.layer.ui.mock;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.layer.sdk.LayerClient;
import com.layer.sdk.listeners.LayerProgressListener;
import com.layer.sdk.listeners.LayerTypingIndicatorListener;
import com.layer.sdk.messaging.Conversation;
import com.layer.sdk.messaging.Identity;
import com.layer.sdk.messaging.Message;
import com.layer.sdk.messaging.Metadata;

import java.util.HashSet;
import java.util.Set;

public class MockConversation extends Conversation {

    private Set<Identity> mIdentities;
    private Message mLastMessage;

    public MockConversation(Identity authenticatedUser, int numberOfUsers) {
        if (numberOfUsers < 1) {
            throw new IllegalArgumentException("Cant have less than 1 user");
        }
        mIdentities = new HashSet<>();
        mIdentities.add(authenticatedUser);

        for (int i = 0; i < numberOfUsers - 1; i++) {
            mIdentities.add(new MockIdentity());
        }

        mLastMessage = new MockMessageImpl(new MockMessagePart("Hello".getBytes(), "text/plain"));
    }

    @Override
    public void addParticipantsByIds(@NonNull Set<String> set) {

    }

    @Override
    public void removeParticipants(@NonNull Set<Identity> set) {

    }

    @Override
    public void delete(LayerClient.DeletionMode deletionMode) {

    }

    @Override
    public void putMetadataAtKeyPath(String s, String s1) {

    }

    @Override
    public void putMetadata(Metadata metadata, boolean b) {

    }

    @Override
    public void removeMetadataAtKeyPath(String s) {

    }

    @Override
    public void send(Message message) {

    }

    @Override
    public void send(LayerTypingIndicatorListener.TypingIndicator typingIndicator) {

    }

    @Override
    public void send(Message message, LayerProgressListener layerProgressListener) {

    }

    @Override
    public HistoricSyncStatus getHistoricSyncStatus() {
        return null;
    }

    @Override
    public void syncMoreHistoricMessages(int i) {

    }

    @Override
    public void syncFromEarliestUnreadMessage() {

    }

    @Override
    public void syncAllHistoricMessages() {

    }

    @Override
    public void markAllMessagesAsRead() {

    }

    @Override
    public Uri getId() {
        return null;
    }

    @Override
    public Set<Identity> getParticipants() {
        return mIdentities;
    }

    @Override
    public Message getLastMessage() {
        return mLastMessage;
    }

    @Override
    public boolean isDeleted() {
        return false;
    }

    @Override
    public Metadata getMetadata() {
        return null;
    }

    @Override
    public boolean isDeliveryReceiptsEnabled() {
        return false;
    }

    @Override
    public boolean isReadReceiptsEnabled() {
        return false;
    }

    @Override
    public boolean isDistinct() {
        return false;
    }

    @Override
    public Integer getTotalMessageCount() {
        return 1;
    }

    @Override
    public Integer getTotalUnreadMessageCount() {
        return 0;
    }
}
