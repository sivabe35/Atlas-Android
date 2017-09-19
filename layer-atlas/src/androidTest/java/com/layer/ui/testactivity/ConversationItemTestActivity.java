package com.layer.ui.testactivity;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Conversation;
import com.layer.sdk.messaging.Identity;
import com.layer.ui.R;
import com.layer.ui.avatar.AvatarViewModel;
import com.layer.ui.avatar.AvatarViewModelImpl;
import com.layer.ui.conversationitem.ConversationItemFormatter;
import com.layer.ui.conversationitem.ConversationItemViewModel;
import com.layer.ui.databinding.TestActivityFourPartItemBinding;
import com.layer.ui.identity.IdentityFormatter;
import com.layer.ui.identity.IdentityFormatterImpl;
import com.layer.ui.message.messagetypes.CellFactory;
import com.layer.ui.message.messagetypes.generic.GenericCellFactory;
import com.layer.ui.message.messagetypes.text.TextCellFactory;
import com.layer.ui.mock.MockConversation;
import com.layer.ui.mock.MockLayerClient;
import com.layer.ui.style.FourPartItemStyle;
import com.layer.ui.util.imagecache.ImageCacheWrapper;
import com.layer.ui.util.imagecache.PicassoImageCacheWrapper;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ConversationItemTestActivity extends Activity {

    private ConversationItemFormatter mConversationItemFormatter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ImageCacheWrapper imageCacheWrapper = new PicassoImageCacheWrapper(Picasso.with(this));
        LayerClient layerClient = new MockLayerClient();

        DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getApplicationContext());
        DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(getApplicationContext());
        mConversationItemFormatter = new ConversationItemFormatter(this, dateFormat, timeFormat, getCellFactories(layerClient));

        IdentityFormatter identityFormatter = new IdentityFormatterImpl(getApplicationContext());
        Identity authenticatedUser = layerClient.getAuthenticatedUser();
        Conversation conversation = new MockConversation(authenticatedUser, 3);

        TestActivityFourPartItemBinding binding = DataBindingUtil.setContentView(this, R.layout.test_activity_four_part_item);
        FourPartItemStyle style = new FourPartItemStyle(this, null, 0);

        ConversationItemViewModel viewModel = new ConversationItemViewModel();
        viewModel.setConversationItemFormatter(mConversationItemFormatter);
        viewModel.setAuthenticatedUser(layerClient.getAuthenticatedUser());
        viewModel.setItem(conversation);

        AvatarViewModel avatarViewModel = new AvatarViewModelImpl(imageCacheWrapper);
        binding.testFourPartItem.avatar.init(avatarViewModel, identityFormatter);

        binding.testFourPartItem.avatar.setParticipants(conversation.getParticipants());
        binding.testFourPartItem.setStyle(style);
        binding.testFourPartItem.setViewModel(viewModel);
    }

    public List<CellFactory> getCellFactories(LayerClient layerClient) {
        List<CellFactory> cellFactories = new ArrayList<>();

        cellFactories.add(new TextCellFactory());
        cellFactories.add(new GenericCellFactory());

        return cellFactories;
    }
}
