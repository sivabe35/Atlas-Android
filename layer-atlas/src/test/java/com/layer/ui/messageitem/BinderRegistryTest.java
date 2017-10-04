package com.layer.ui.messageitem;

import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Message;
import com.layer.sdk.messaging.MessagePart;
import com.layer.ui.message.binder.BinderRegistry;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;


public class BinderRegistryTest {

    @Mock
    LayerClient layerClient;

    @Mock
    Message message1, message2;

    @Mock
    MessagePart message1Part1, message1Part2;

    @Mock
    MessagePart message2Part1, message2Part2, message2Part3;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        when(message1Part1.getMimeType()).thenReturn("application/vnd.layer.card.response+json;node-id=e004b419-2e53-4680-88cf-6938b365a2c0;role=root");
        when(message1Part2.getMimeType()).thenReturn("application/vnd.layer.card.text+json;node-id=f469fcad-a59f-4bdd-af7c-044ea11f3b91;role=message;parent-node-id=e004b419-2e53-4680-88cf-6938b365a2c0");

        when(message2Part1.getMimeType()).thenReturn("image/jpeg");
        when(message2Part2.getMimeType()).thenReturn("image/jpeg+preview");
        when(message2Part3.getMimeType()).thenReturn("application/json+imageSize");

        List<MessagePart> message1Parts = new ArrayList<>();
        Collections.addAll(message1Parts, message1Part1, message1Part2);

        when(message1.getMessageParts()).thenReturn(message1Parts);

        List<MessagePart> message2Parts = new ArrayList<>();
        Collections.addAll(message2Parts, message2Part1, message2Part2, message2Part3);

        when(message2.getMessageParts()).thenReturn(message2Parts);
    }

    @Test
    public void testMessageIsCardType() {
        BinderRegistry binderRegistry = new BinderRegistry(layerClient);

        assertThat(binderRegistry.isLegacyMessageType(message1), is(false));
    }

    @Test
    public void testMessageIsLegacyType() {
        BinderRegistry binderRegistry = new BinderRegistry(layerClient);

        assertThat(binderRegistry.isLegacyMessageType(message2), is(true));
    }
}
