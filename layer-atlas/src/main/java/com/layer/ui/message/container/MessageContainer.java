package com.layer.ui.message.container;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.layer.ui.message.model.MessageModel;
import com.layer.ui.message.view.MessageView;

public abstract class MessageContainer extends FrameLayout {
    public MessageContainer(@NonNull Context context) {
        this(context, null, 0);
    }

    public MessageContainer(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, null, 0);
    }

    public MessageContainer(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public abstract void setMessageView(MessageView view);

    public abstract <T extends MessageModel> void setMessageModel(T model);

    protected abstract <T extends MessageModel> void setContentBackground(T model);
}
