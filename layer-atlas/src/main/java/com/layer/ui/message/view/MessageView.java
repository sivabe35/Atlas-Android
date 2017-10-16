package com.layer.ui.message.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.layer.ui.message.container.MessageContainer;
import com.layer.ui.message.model.MessageModel;

public abstract class MessageView<VIEW_MODEL extends MessageModel> extends FrameLayout {
    public MessageView(Context context) {
        super(context);
    }

    public MessageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MessageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public abstract void setMessageModel(VIEW_MODEL model);

    public abstract Class<? extends MessageContainer> getContainerClass();
}
