package com.layer.ui.message.image;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.layer.ui.databinding.UiImageMessageViewBinding;
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
            layoutParams.width = (metadata.getPreviewWidth() > 0 ? metadata.getPreviewWidth() : metadata.getWidth());
            layoutParams.width = layoutParams.width > 0 ? layoutParams.width : ViewGroup.LayoutParams.WRAP_CONTENT;
            layoutParams.height = metadata.getPreviewHeight() > 0 ? metadata.getPreviewHeight() : metadata.getHeight();
            layoutParams.height = layoutParams.height > 0 ? layoutParams.height : ViewGroup.LayoutParams.WRAP_CONTENT;
        }
    }
}
