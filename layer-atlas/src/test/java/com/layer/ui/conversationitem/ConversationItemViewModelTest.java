package com.layer.ui.conversationitem;

import android.content.Context;

import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Conversation;
import com.layer.sdk.messaging.Identity;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;


public class ConversationItemViewModelTest {
    private static final String CONVERSATION_TITLE = "P2, P3";
    private static final String CONVERSATION_SUBTITLE = "The last message";
    private static final String CONVERSATION_TIMESTAMP = "3 hours ago";

    @Mock
    ConversationItemFormatter mConversationItemFormatter;
    @Mock
    Conversation conversation;
    @Mock
    Context context;
    @Mock
    LayerClient layerClient;
    @Mock
    Identity participant1, participant2, participant3;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        when(mConversationItemFormatter.getConversationTitle(any(Identity.class), any(Conversation.class), any((Set.class)))).thenReturn(CONVERSATION_TITLE);
        when(mConversationItemFormatter.getLastMessagePreview(any(Conversation.class))).thenReturn(CONVERSATION_SUBTITLE);
        when(mConversationItemFormatter.getTimeStamp(any(Conversation.class))).thenReturn(CONVERSATION_TIMESTAMP);

        when(conversation.getTotalUnreadMessageCount()).thenReturn(5);

        Set<Identity> participants = new HashSet<>();
        participants.addAll(Arrays.asList(participant1, participant2, participant3));
        when(conversation.getParticipants()).thenReturn(participants);

        when(layerClient.newConversation(any(Identity.class))).thenReturn(conversation);
        when(layerClient.getAuthenticatedUser()).thenReturn(participant1);
    }

    @Test
    public void testGetTitle() {
        ConversationItemViewModel viewModel = new ConversationItemViewModel();
        viewModel.setConversationItemFormatter(mConversationItemFormatter);
        viewModel.setAuthenticatedUser(layerClient.getAuthenticatedUser());
        viewModel.setItem(conversation);

        assertThat(viewModel.getSubtitle(), is(CONVERSATION_SUBTITLE));
    }

    @Test
    public void testGetSubtitle() {
        ConversationItemViewModel viewModel = new ConversationItemViewModel();
        viewModel.setConversationItemFormatter(mConversationItemFormatter);
        viewModel.setAuthenticatedUser(layerClient.getAuthenticatedUser());
        viewModel.setItem(conversation);

        assertThat(viewModel.getSubtitle(), is(CONVERSATION_SUBTITLE));
    }

    @Test
    public void testAccessoryText() {
        ConversationItemViewModel viewModel = new ConversationItemViewModel();
        viewModel.setConversationItemFormatter(mConversationItemFormatter);
        viewModel.setAuthenticatedUser(layerClient.getAuthenticatedUser());
        viewModel.setItem(conversation);

        assertThat(viewModel.getAccessoryText(), is(CONVERSATION_TIMESTAMP));
    }

    @Test
    public void testIsUnread() {
        ConversationItemViewModel viewModel = new ConversationItemViewModel();
        viewModel.setConversationItemFormatter(mConversationItemFormatter);
        viewModel.setAuthenticatedUser(layerClient.getAuthenticatedUser());
        viewModel.setItem(conversation);

        assertThat(viewModel.isSecondaryState(), is(true));
    }

    @Test
    public void testGetParticipantsDoesNotContainAuthenticatedUser() {
        ConversationItemViewModel viewModel = new ConversationItemViewModel();
        viewModel.setConversationItemFormatter(mConversationItemFormatter);
        viewModel.setAuthenticatedUser(layerClient.getAuthenticatedUser());
        viewModel.setItem(conversation);

        assertThat(viewModel.getIdentities().contains(participant1), is(false));
        assertThat(viewModel.getIdentities().contains(participant2), is(true));
        assertThat(viewModel.getIdentities().contains(participant3), is(true));
    }
}
