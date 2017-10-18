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
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.layer.sdk.LayerClient;
import com.layer.sdk.listeners.LayerTypingIndicatorListener;
import com.layer.sdk.messaging.Conversation;
import com.layer.sdk.messaging.Identity;
import com.layer.sdk.messaging.MessagingPattern;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AtlasTypingIndicator provides feedback about typists within a Conversation.  When initialized
 * and registered with a LayerClient as a LayerTypingIndicatorListener, AtlasTypingIndicator
 * maintains a set of typists for the given Conversation, providing callbacks when UI updates are
 * needed.  AtlasTypingIndicator can provide a default UI updater if desired.
 */
public class TypingIndicatorLayout extends FrameLayout implements LayerTypingIndicatorListener.Weak {
    private final ConcurrentHashMap<Identity, TypingIndicator> mTypists = new ConcurrentHashMap<>();

    private volatile Conversation mConversation;
    private volatile TypingActivityListener mActivityListener;
    private volatile TypingIndicatorFactory mTypingIndicatorFactory;
    private volatile boolean mActive = false;
    private volatile View mIndicatorView;

    public TypingIndicatorLayout(Context context) {
        super(context);
    }

    public TypingIndicatorLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TypingIndicatorLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Sets the Conversation to listen for typing on.  If `null`, no typing will be listened to.
     *
     * @param layerClient  LayerClient currently in use
     * @param conversation Conversation to listen for typing on
     */
    public void setConversation(LayerClient layerClient, Conversation conversation) {
        if (layerClient == null) throw new IllegalArgumentException("LayerClient cannot be null");
        layerClient.registerTypingIndicator(this);
        mConversation = conversation;
    }

    /**
     * Sets the TypingIndicatorFactory used to generate the TypingIndicator View.
     *
     * @param factory TypingIndicatorFactory used to generate the TypingIndicator View.
     * @see TypingIndicatorLayout.TypingIndicatorFactory
     */
    public void setTypingIndicatorFactory(TypingIndicatorFactory factory) {
        mTypingIndicatorFactory = factory;
        removeAllViews();
        if (factory != null) {
            mIndicatorView = factory.onCreateView(getContext());
            addView(mIndicatorView);
        } else {
            mIndicatorView = null;
        }
    }

    /**
     * Sets the TypingActivityListener for receiving TypingIndicator activity state changes.
     *
     * @param listener TypingActivityListener for receiving TypingIndicator activity state changes.
     */
    public void setTypingActivityListener(TypingActivityListener listener) {
        mActivityListener = listener;
    }

    /**
     * Clears the current list of typists and calls refresh().
     */
    public void clear() {
        mTypists.clear();
        refresh();
    }

    /**
     * Calls Callback.onBindView() with the current list of typists.
     */
    private void refresh() {
        if (mTypingIndicatorFactory == null) return;
        mTypingIndicatorFactory.onBindView(mIndicatorView, mTypists);
    }

    /**
     * Refresh when the typing indicator gets attached (e.g. scrolling into view).
     */
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        refresh();
    }

    @Override
    public void onTypingIndicator(LayerClient layerClient, MessagingPattern messagingPattern, Identity user, TypingIndicator typingIndicator) {
        // Only monitor typing in this indicator's conversation.
        if (mConversation != messagingPattern) return;

        // Notify ActivityListener to active/inactive typists.
        boolean isEmpty;
        if (typingIndicator == TypingIndicator.FINISHED) {
            mTypists.remove(user);
        } else {
            mTypists.put(user, typingIndicator);
        }

        isEmpty = mTypists.isEmpty();
        Set<Identity> identitySet = mTypists.keySet();

        if (isEmpty && mActive) {
            mActive = false;
            if (mActivityListener != null) mActivityListener.onTypingActivityChange(this, false, identitySet);
        } else if (!isEmpty && !mActive) {
            mActive = true;
            if (mActivityListener != null) mActivityListener.onTypingActivityChange(this, true, identitySet);
        } else if (!isEmpty) {
            if (mActivityListener != null) mActivityListener.onTypingActivityChange(this, true, identitySet);
        }

        // Refresh the indicator view.
        refresh();
    }

    /**
     * TypingIndicatorFactory allows an external class to set indicator text, visibility,
     * etc. based on the current typists.
     */
    public interface TypingIndicatorFactory<T extends View> {
        /**
         * Returns the View for this typing indicator.
         *
         * @param context Context to create the View.
         * @return View to display as a typing indicator.
         */
        T onCreateView(Context context);

        /**
         * Notifies the callback to typist updates.
         *
         * @param view          The View previously created by onCreateView
         * @param typingUserIds The set of currently-active typist users
         */
        void onBindView(T view, Map<Identity, TypingIndicator> typingUserIds);
    }

    /**
     * TypingActivityListener alerts a listener to active and inactive typing.  This is useful for
     * adding and removing a typing indicator view from a layout, for example.
     */
    public interface TypingActivityListener {
        /**
         * Typists transitioned from inactive to active.
         *
         * @param typingIndicator AtlasTypingIndicator notifying this listener.
         * @param active          `true` if typists are now active, or `false` if inactive.
         */
        void onTypingActivityChange(TypingIndicatorLayout typingIndicator, boolean active, Set<Identity> users);
    }
}
