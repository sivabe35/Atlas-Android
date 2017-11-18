package com.layer.ui.message.image;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.JsonObject;
import com.layer.ui.databinding.UiImageMessageViewBinding;
import com.layer.ui.message.action.ActionHandlerRegistry;
import com.layer.ui.message.container.StandardMessageContainer;
import com.layer.ui.message.view.MessageView;

public class ImageMessageView extends MessageView<ImageMessageModel> {

    private UiImageMessageViewBinding mBinding;

    public ImageMessageView(Context context) {
        this(context, null, 0);
    }

    public ImageMessageView(Context context, @Nullable AttributeSet attrs) {
        this(context, null, 0);
    }

    public ImageMessageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater inflater = LayoutInflater.from(getContext());
        mBinding = UiImageMessageViewBinding.inflate(inflater, this, true);
        mBinding.image.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBinding.getViewModel() != null) {
                    ImageMessageModel model = mBinding.getViewModel();
                    performAction(model.getActionEvent(), model.getActionData());
                }
            }
        });
    }

    @Override
    public void setMessageModel(ImageMessageModel model) {
        mBinding.setViewModel(model);
        setupImageViewDimensions(model);
    }

    @Override
    public Class<StandardMessageContainer> getContainerClass() {
        return StandardMessageContainer.class;
    }

    private void setupImageViewDimensions(ImageMessageModel model) {
        ImageMessageMetadata metadata = model.getMetadata();
        if (metadata != null) {
            ViewGroup.LayoutParams layoutParams = mBinding.image.getLayoutParams();

            int width = (metadata.getPreviewWidth() > 0 ? metadata.getPreviewWidth() : metadata.getWidth());
            width = width > 0 ? layoutParams.width : ViewGroup.LayoutParams.WRAP_CONTENT;
            int height = metadata.getPreviewHeight() > 0 ? metadata.getPreviewHeight() : metadata.getHeight();
            height = height > 0 ? layoutParams.height : ViewGroup.LayoutParams.WRAP_CONTENT;

            layoutParams.width = width;
            layoutParams.height = height;
        }
    }

    @Override
    public void performAction(String event, JsonObject customData) {
        if (event != null) {
            ActionHandlerRegistry.dispatchEvent(getContext(), event, customData);
        }
    }
}
