package com.layer.ui.message.model;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import com.layer.sdk.LayerClient;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


import static org.junit.Assert.assertNotNull;

public class MessageModelManagerTest {

    private Context mContext;
    private MessageModelManager mMessageModelManager;

    @Mock
    private LayerClient mLayerClient;

    @Before
    public void setup() {
        mContext = InstrumentationRegistry.getTargetContext();
        mMessageModelManager = new MessageModelManager(mContext, mLayerClient);
    }

    @Test
    public void testTextMessageModelRegistration() {
        mMessageModelManager.registerModel("TextMessageModel", TextMessageModel.class);
        assertNotNull(mMessageModelManager.getModel("TextMessageModel"));
    }
}
