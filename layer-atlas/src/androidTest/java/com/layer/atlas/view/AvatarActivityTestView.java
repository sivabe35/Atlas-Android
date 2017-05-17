package com.layer.atlas.view;

import android.app.Activity;
import android.os.Bundle;

import com.layer.atlas.mock.MockLayerClient;
import com.layer.sdk.LayerClient;
import com.layer.ui.R;
import com.layer.ui.avatar.AvatarView;
import com.layer.ui.avatar.AvatarViewModel;
import com.layer.ui.util.imagecache.ImageCacheWrapper;
import com.layer.ui.util.imagecache.PicassoImageCacheWrapper;
import com.layer.ui.util.imagecache.requesthandlers.MessagePartRequestHandler;

public class AvatarActivityTestView extends Activity {

    private AvatarView mAvatarView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avatar_test);
        LayerClient layerClient = new MockLayerClient();
        mAvatarView = (AvatarView) findViewById(R.id.test_avatar);
        MessagePartRequestHandler messagePartRequestHandler = new MessagePartRequestHandler(layerClient);
        ImageCacheWrapper imageCacheWrapper = new PicassoImageCacheWrapper(messagePartRequestHandler, this);
        mAvatarView.init(new AvatarViewModel(imageCacheWrapper));
    }
}
