package com.layer.ui.binder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import com.layer.sdk.messaging.Message;

public interface Binder {

    Object getRoot(Message message);

    String getRootMimeType();

    String getPreviewText(Context context, Message message);

    RecyclerView.ViewHolder createViewHolder();
}
