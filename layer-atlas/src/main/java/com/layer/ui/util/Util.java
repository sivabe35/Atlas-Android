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
package com.layer.ui.util;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.layer.sdk.LayerClient;
import com.layer.sdk.authentication.AuthenticationListener;
import com.layer.sdk.listeners.LayerProgressListener;
import com.layer.sdk.messaging.Identity;
import com.layer.sdk.messaging.MessagePart;
import com.layer.sdk.query.Queryable;
import com.layer.ui.BuildConfig;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class Util {

    /**
     * Returns the app version name.
     *
     * @return The app version name.
     */
    public static String getVersion() {
        return BuildConfig.VERSION_NAME;
    }

    public static void copyToClipboard(Context context, int stringResId, String content) {
        copyToClipboard(context, context.getString(stringResId), content);
    }

    public static void copyToClipboard(Context context, String description, String content) {
        ClipboardManager manager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = new ClipData(description, new String[]{"text/plain"}, new ClipData.Item(content));
        manager.setPrimaryClip(clipData);
    }


    public static String getInitials(Identity user) {
        String first = user.getFirstName();
        String last = user.getLastName();
        if (!TextUtils.isEmpty(first)) {
            if (!TextUtils.isEmpty(last)) {
                return getInitials(first) + getInitials(last);
            }
            return getInitials(first);
        } else if (!TextUtils.isEmpty(last)) {
            return getInitials(last);
        } else {
            return getInitials(user.getDisplayName());
        }
    }

    private static String getInitials(String name) {
        if(TextUtils.isEmpty(name)) return "";
        if (name.contains(" ")) {
            String[] nameParts = name.split(" ");
            int count = 0;
            StringBuilder b = new StringBuilder();
            for (String part : nameParts) {
                String t = part.trim();
                if (t.isEmpty()) continue;
                b.append(("" + t.charAt(0)).toUpperCase());
                if (++count >= 2) break;
            }
            return b.toString();
        } else {
            return ("" + name.trim().charAt(0)).toUpperCase();
        }
    }

    @NonNull
    public static String getDisplayName(Identity identity) {
        if (TextUtils.isEmpty(identity.getDisplayName())) {
            String first = identity.getFirstName();
            String last = identity.getLastName();
            if (!TextUtils.isEmpty(first)) {
                if (!TextUtils.isEmpty(last)) {
                    return String.format("%s %s", first, last);
                }
                return first;
            } else if (!TextUtils.isEmpty(last)) {
                return last;
            } else {
                return identity.getUserId();
            }
        }
        return identity.getDisplayName();
    }



    /**
     * Returns int[] {scaledWidth, scaledHeight} for dimensions that fit within the given maxWidth,
     * maxHeight at the given inWidth, inHeight aspect ratio.  If the in dimensions fit fully inside
     * the max dimensions, no scaling is applied.  Otherwise, at least one scaled dimension is set
     * to a max dimension, and the other scaled dimension is scaled to fit.
     *
     * @param inWidth
     * @param inHeight
     * @param maxWidth
     * @param maxHeight
     * @return
     */
    public static int[] scaleDownInside(int inWidth, int inHeight, int maxWidth, int maxHeight) {
        int scaledWidth;
        int scaledHeight;
        if (inWidth <= maxWidth && inHeight <= maxHeight) {
            scaledWidth = inWidth;
            scaledHeight = inHeight;
        } else {
            double widthRatio = (double) inWidth / (double) maxWidth;
            double heightRatio = (double) inHeight / (double) maxHeight;
            if (widthRatio > heightRatio) {
                scaledWidth = maxWidth;
                scaledHeight = (int) Math.round((double) inHeight / widthRatio);
            } else {
                scaledHeight = maxHeight;
                scaledWidth = (int) Math.round((double) inWidth / heightRatio);
            }
        }
        return new int[]{scaledWidth, scaledHeight};
    }

    /**
     * Synchronously starts downloading the given MessagePart and waits for downloading to complete.
     * Returns `true` if the MessagePart downloaded successfully within the given period of time, or
     * `false` otherwise.
     *
     * @param layerClient LayerClient to download the MessagePart with.
     * @param part        MessagePart to download.
     * @param timeLength  Length of time to wait for downloading.
     * @param timeUnit    Unit of time to wait for downloading.
     * @return `true` if the MessagePart content is available, or `false` otherwise.
     */
    public static boolean downloadMessagePart(LayerClient layerClient, MessagePart part, int timeLength, TimeUnit timeUnit) {
        if (part.isContentReady()) return true;

        final CountDownLatch latch = new CountDownLatch(1);
        final LayerProgressListener listener = new LayerProgressListener.BackgroundThread.Weak() {
            @Override
            public void onProgressStart(MessagePart messagePart, Operation operation) {

            }

            @Override
            public void onProgressUpdate(MessagePart messagePart, Operation operation, long l) {

            }

            @Override
            public void onProgressComplete(MessagePart messagePart, Operation operation) {
                latch.countDown();
            }

            @Override
            public void onProgressError(MessagePart messagePart, Operation operation, Throwable throwable) {
                latch.countDown();
            }
        };
        part.download(listener);
        if (!part.isContentReady()) {
            try {
                latch.await(timeLength, timeUnit);
            } catch (InterruptedException e) {
                if (Log.isLoggable(Log.ERROR)) {
                    Log.e(e.getMessage(), e);
                }
            }
        }
        return part.isContentReady();
    }

    // TODO Is this the appropriate paradigm?
    public static MessagePart getMessagePartBlocking(LayerClient layerClient, Uri partUri) {
        final LiveData<MessagePart> liveData = layerClient.getLive(partUri, MessagePart.class);
        if (liveData.getValue() == null) {
            // Wait for the data to be loaded. This is okay since we're not on the main thread.
            final CountDownLatch latch = new CountDownLatch(1);

            liveData.observeForever(new Observer<MessagePart>() {
                @Override
                public void onChanged(@Nullable MessagePart layerObject) {
                    if (layerObject != null) {
                        // TODO this has to be done on the main thread
                        liveData.removeObserver(this);
                        latch.countDown();
                    }
                }
            });

            try {
                latch.await(3, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                if (Log.isLoggable(Log.ERROR)) {
                    Log.d("Failed to find message part: " + partUri, e);
                }
                return null;
            }
        }

        return liveData.getValue();
    }

    /**
     * Asynchronously deauthenticates with Layer.
     *
     * @param layerClient LayerClient to deauthenticate.
     * @param callback    Callback to report deauthentication success and failure.
     */
    public static void deauthenticate(LayerClient layerClient, final DeauthenticationCallback callback) {
        final AtomicBoolean alerted = new AtomicBoolean(false);

        AuthenticationListener listener = new AuthenticationListener() {
            @Override
            public void onAuthenticated(LayerClient client, String userId) {

            }

            @Override
            public void onDeauthenticated(LayerClient client, String userId) {
                if (alerted.compareAndSet(false, true)) {
                    callback.onDeauthenticationSuccess(client);
                }
            }

            @Override
            public void onAuthenticationError(LayerClient client, Exception exception) {
                if (alerted.compareAndSet(false, true)) {
                    callback.onDeauthenticationFailed(client, exception.getMessage());
                }
            }
        };
        layerClient.registerAuthenticationListener(listener);
        if (!layerClient.isAuthenticated()) {
            layerClient.unregisterAuthenticationListener(listener);
            if (alerted.compareAndSet(false, true)) {
                callback.onDeauthenticationSuccess(layerClient);
            }
            return;
        }
        layerClient.deauthenticate();
    }

    public interface ContentAvailableCallback {
        void onContentAvailable(LayerClient client, Queryable object);

        void onContentFailed(LayerClient client, Uri objectId, String reason);
    }


    public interface DeauthenticationCallback {
        void onDeauthenticationSuccess(LayerClient client);

        void onDeauthenticationFailed(LayerClient client, String reason);
    }
}