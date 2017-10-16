package com.layer.ui.messageitem;

import android.content.Context;

import com.layer.sdk.LayerClient;
import com.layer.ui.message.model.MessageModelManager;
import com.layer.ui.message.model.TextMessageModel;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;

public class MessageModelRegistryTest {
    private MessageModelManager mMessageModelManager;

    private LayerClient mLayerClient;
    private Context mContext;

    @Before
    public void setup() {
        mMessageModelManager = new MessageModelManager(mContext, mLayerClient);
    }

    @Test
    public void testTextMessageModelRegistration() {
        mMessageModelManager.registerModel("TextMessageModel", TextMessageModel.class);
        assertNotNull(mMessageModelManager.getModel("TextMessageModel"));
    }
}
