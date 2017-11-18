package com.layer.ui.message.text;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import com.google.gson.JsonObject;
import com.layer.ui.databinding.UiTextMessageViewBinding;
import com.layer.ui.message.action.ActionHandlerRegistry;
import com.layer.ui.message.container.StandardMessageContainer;
import com.layer.ui.message.view.MessageView;

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
        mBinding.getRoot().setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBinding.getViewModel() != null) {
                    TextMessageModel model = mBinding.getViewModel();
                    performAction(model.getActionEvent(), model.getActionData());
                }
            }
        });
    }

    @Override
    public void setMessageModel(TextMessageModel model) {
        mBinding.setViewModel(model);
    }

    @Override
    public Class<StandardMessageContainer> getContainerClass() {
        return StandardMessageContainer.class;
    }

    @Override
    public void performAction(String event, JsonObject customData) {
        if (!TextUtils.isEmpty(event)) {
            ActionHandlerRegistry.dispatchEvent(getContext(), event, customData);
        }
    }
}
