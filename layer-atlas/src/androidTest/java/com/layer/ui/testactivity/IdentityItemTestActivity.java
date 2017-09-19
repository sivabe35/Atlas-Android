package com.layer.ui.testactivity;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.layer.sdk.messaging.Identity;
import com.layer.ui.R;
import com.layer.ui.avatar.AvatarViewModel;
import com.layer.ui.avatar.AvatarViewModelImpl;
import com.layer.ui.databinding.TestActivityFourPartItemBinding;
import com.layer.ui.identity.IdentityFormatter;
import com.layer.ui.identity.IdentityFormatterImpl;
import com.layer.ui.identity.IdentityItemViewModel;
import com.layer.ui.mock.MockIdentity;
import com.layer.ui.style.FourPartItemStyle;
import com.layer.ui.util.DateFormatterImpl;
import com.layer.ui.util.imagecache.ImageCacheWrapper;
import com.layer.ui.util.imagecache.PicassoImageCacheWrapper;
import com.squareup.picasso.Picasso;

public class IdentityItemTestActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ImageCacheWrapper imageCacheWrapper = new PicassoImageCacheWrapper(Picasso.with(this));
        IdentityFormatter identityFormatter = new IdentityFormatterImpl(getApplicationContext());
        Identity identity = new MockIdentity();

        TestActivityFourPartItemBinding binding = DataBindingUtil.setContentView(this, R.layout.test_activity_four_part_item);
        FourPartItemStyle style = new FourPartItemStyle(this, null, 0);

        IdentityItemViewModel viewModel = new IdentityItemViewModel();
        viewModel.setIdentityFormatter(identityFormatter);
        viewModel.setDateFormatter(new DateFormatterImpl(getApplicationContext()));
        viewModel.setItem(identity);

        AvatarViewModel avatarViewModel = new AvatarViewModelImpl(imageCacheWrapper);

        binding.testFourPartItem.avatar.init(avatarViewModel, identityFormatter);
        binding.testFourPartItem.avatar.setParticipants(identity);
        binding.testFourPartItem.setStyle(style);
        binding.testFourPartItem.setViewModel(viewModel);
    }
}
