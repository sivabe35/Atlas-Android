package com.layer.atlas.view;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.layer.atlas.mock.MockLayerClient;
import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Presence;
import com.layer.ui.R;
import com.layer.ui.avatar.AvatarView;
import com.layer.ui.avatar.AvatarViewModel;
import com.layer.ui.util.imagecache.ImageCacheWrapper;
import com.layer.ui.util.imagecache.PicassoImageCacheWrapper;
import com.layer.ui.util.imagecache.requesthandlers.MessagePartRequestHandler;

import java.util.ArrayList;
import java.util.List;

public class AvatarActivityTestView extends Activity implements AdapterView.OnItemSelectedListener{

    private AvatarView mAvatarView;
    private Spinner mPresenceSpinner;
    private ArrayAdapter<String> mPresenceSpinnerDataAdapter;
    private LayerClient mLayerClient;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avatar_test);
        mLayerClient = new MockLayerClient();
        mAvatarView = (AvatarView) findViewById(R.id.test_avatar);
        mPresenceSpinner = (Spinner) findViewById(R.id.test_spinner);
        MessagePartRequestHandler messagePartRequestHandler = new MessagePartRequestHandler(mLayerClient);
        ImageCacheWrapper imageCacheWrapper = new PicassoImageCacheWrapper(messagePartRequestHandler, this);
        mAvatarView.init(new AvatarViewModel(imageCacheWrapper));
        setUp();
    }

    private void setUp() {
        mPresenceSpinner.setOnItemSelectedListener(this);
        List<String> presenceStates = new ArrayList<>();
        for (Presence.PresenceStatus status : Presence.PresenceStatus.values()) {
            if (status.isUserSettable()) {
                presenceStates.add(status.toString());
            }
        }
        mPresenceSpinnerDataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, presenceStates);
        mPresenceSpinnerDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mPresenceSpinner.setAdapter(mPresenceSpinnerDataAdapter);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if (!mLayerClient.isAuthenticated()) return;

        String newSelection = mPresenceSpinnerDataAdapter.getItem(i).toString();
        Presence.PresenceStatus newStatus = Presence.PresenceStatus.valueOf(newSelection);
        if (mLayerClient.isAuthenticated()) {
            mLayerClient.setPresenceStatus(newStatus);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
