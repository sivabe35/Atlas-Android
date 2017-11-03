package com.layer.ui.message.container;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.layer.ui.R;
import com.layer.ui.databinding.UiStandardMessageContainerBinding;
import com.layer.ui.message.model.MessageModel;
import com.layer.ui.message.view.MessageView;

public class StandardMessageContainer extends MessageContainer {

    private UiStandardMessageContainerBinding mBinding;
    private MessageView mMessageView;

    public StandardMessageContainer(@NonNull Context context) {
        this(context, null, 0);
    }

    public StandardMessageContainer(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, null, 0);
    }

    public StandardMessageContainer(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater inflater = LayoutInflater.from(getContext());
        mBinding = UiStandardMessageContainerBinding.inflate(inflater, this, true);
        mBinding.uiStandardMessageContainerTitle.setVisibility(GONE);
        mBinding.uiStandardMessageContainerSubtitle.setVisibility(GONE);
        setCornerRadius(context.getResources().getDimension(R.dimen.ui_standard_message_container_corner_radius));
    }

    public void setMessageView(MessageView view) {
        mMessageView = view;
        view.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        mBinding.uiStandardMessageContainerContentView.addView(view);
    }

    @Override
    public <T extends MessageModel> void setMessageModel(T model) {
        mMessageView.setMessageModel(model);
        mBinding.setViewModel(model);
        mBinding.executePendingBindings();
        setContentBackground(model);
    }

    @Override
    protected <T extends MessageModel> void setContentBackground(T model) {
        GradientDrawable background = (GradientDrawable) ContextCompat.getDrawable(getContext(), R.drawable.ui_standard_message_container_content_background);
        background.setColor(ContextCompat.getColor(getContext(), model.getBackgroundColor()));
        mBinding.uiStandardMessageContainerContentView.setBackgroundDrawable(background);
    }
}
