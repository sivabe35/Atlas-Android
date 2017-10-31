package com.layer.ui.message.viewer;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.layer.sdk.messaging.Message;
import com.layer.ui.message.MessagePartUtils;
import com.layer.ui.message.container.MessageContainer;
import com.layer.ui.message.model.MessageModel;
import com.layer.ui.message.model.MessageModelManager;
import com.layer.ui.message.view.MessageView;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class MessageViewer extends FrameLayout {
    private MessageModelManager mMessageModelManager;

    private Message mMessage;

    private MessageContainer mMessageContainer;
    private MessageView mMessageView;

    public MessageViewer(@NonNull Context context) {
        this(context, null);
    }

    public MessageViewer(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MessageViewer(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setMessage(Message message) {
        mMessage = message;
        bindMessageToView();
    }

    public void setMessageModelManager(MessageModelManager messageModelManager) {
        mMessageModelManager = messageModelManager;
    }

    // TODO : This only works for one message part... fix to add more
    // TODO : Handle these nulls appropriately
    @Nullable
    private MessageModel getMessageTypeModel() {
        String mimeType = MessagePartUtils.getMimeType(mMessage.getMessageParts().get(0));
        return mMessageModelManager.getModel(mimeType);
    }

    //==============================================================================================
    //  View Processing
    //==============================================================================================

    protected void bindMessageToView() {
        // Get the model
        MessageModel model = getMessageTypeModel();
        model.setMessage(mMessage);
        Class<? extends MessageView> modelType = model.getRendererType();

        try {
            if (mMessageView == null) {
                mMessageView = instantiateView(modelType);
            }

            if (mMessageContainer == null) {
                mMessageContainer = instantiateContainer(mMessageView.getContainerClass());
                addContainer(mMessageContainer);
                mMessageContainer.setMessageView(mMessageView);
            }

            mMessageContainer.setMessageModel(model);

        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Cannot instantiate view with class: " + modelType.getName() + " : " + e.getMessage());
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot instantiate view with class: " + modelType.getName() + " : " + e.getMessage());
        } catch (InvocationTargetException e) {
            throw new IllegalStateException("Cannot instantiate view with class: " + modelType.getName() + " : " + e.getMessage());
        } catch (InstantiationException e) {
            throw new IllegalStateException("Cannot instantiate view with class: " + modelType.getName() + " : " + e.getMessage());
        }
    }

    protected MessageView instantiateView(Class<? extends MessageView> viewTypeClass) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<? extends MessageView> constructor = viewTypeClass.getConstructor(Context.class);
        return constructor.newInstance(getContext());
    }

    protected MessageContainer instantiateContainer(Class<? extends MessageContainer> containerTypeClass) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<? extends MessageContainer> constructor = containerTypeClass.getConstructor(Context.class);
        return constructor.newInstance(getContext());
    }

    protected void addContainer(MessageContainer container) {
        container.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        addView(container);
    }
}
