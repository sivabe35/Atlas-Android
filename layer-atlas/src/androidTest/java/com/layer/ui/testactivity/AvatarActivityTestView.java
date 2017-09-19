package com.layer.ui.testactivity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.layer.sdk.messaging.Presence;
import com.layer.ui.R;
import com.layer.ui.avatar.AvatarView;
import com.layer.ui.avatar.AvatarViewModelImpl;
import com.layer.ui.identity.IdentityFormatterImpl;
import com.layer.ui.mock.MockIdentity;
import com.layer.ui.mock.MockLayerClient;
import com.layer.ui.presence.PresenceView;
import com.layer.ui.util.imagecache.ImageCacheWrapper;
import com.layer.ui.util.imagecache.PicassoImageCacheWrapper;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class AvatarActivityTestView extends Activity implements AdapterView.OnItemSelectedListener {

    private AvatarView mAvatarView;
    private Spinner mPresenceSpinner;
    private ArrayAdapter<String> mPresenceSpinnerDataAdapter;
    private MockLayerClient mLayerClient;
    private PresenceView mPresenceView;
    private MockIdentity mMockIdentity;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avatar_test);
        mLayerClient = new MockLayerClient();
        mMockIdentity = new MockIdentity();
        mAvatarView = (AvatarView) findViewById(R.id.test_avatar);
        mPresenceSpinner = (Spinner) findViewById(R.id.test_spinner);
        mPresenceView = (PresenceView) findViewById(R.id.test_presence);
        mPresenceView.setParticipants(mMockIdentity);
        ImageCacheWrapper imageCacheWrapper = new PicassoImageCacheWrapper(Picasso.with(this));
        mAvatarView.init(new AvatarViewModelImpl(imageCacheWrapper), new IdentityFormatterImpl(getApplicationContext()));
        mAvatarView.setParticipants(mMockIdentity);
        mAvatarView.init(new AvatarViewModelImpl(imageCacheWrapper), new IdentityFormatterImpl(getApplicationContext()));
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
        mMockIdentity.getPresenceStatus(i);
        mPresenceView.invalidate();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
