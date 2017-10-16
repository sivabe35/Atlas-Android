package com.layer.ui.message.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import com.layer.ui.databinding.UiTextMessageViewBinding;
import com.layer.ui.message.container.MessageContainer;
import com.layer.ui.message.container.StandardMessageContainer;
import com.layer.ui.message.model.TextMessageModel;

public class TextMessageView extends MessageView<TextMessageModel> {
    private UiTextMessageViewBinding mBinding;

    public TextMessageView(Context context) {
        this(context, null, 0);
    }

    public TextMessageView(Context context, @Nullable AttributeSet attrs) {
        this(context, null, 0);
    }

    public TextMessageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater inflater = LayoutInflater.from(getContext());
        mBinding = UiTextMessageViewBinding.inflate(inflater, this, true);
    }

    @Override
    public void setMessageModel(TextMessageModel model) {
        mBinding.setViewModel(model);
    }

    @Override
    public Class<? extends MessageContainer> getContainerClass() {
        return StandardMessageContainer.class;
    }
}
