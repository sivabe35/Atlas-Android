package com.layer.ui.avatar;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import android.graphics.Canvas;

import com.layer.sdk.messaging.Identity;
import com.layer.sdk.messaging.Presence;
import com.layer.ui.util.imagecache.BitmapWrapper;
import com.layer.ui.util.imagecache.ImageCacheWrapper;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class AvatarViewViewModelTest {
    @Mock
    Identity mMockIdentity, mMockIdentity2;

    @Mock
    Avatar.View mMockView;

    AvatarViewModel mAvatarViewModel;

    @Mock
    ImageCacheWrapper mMockImageCacheWrapper;
    @Mock Canvas mMockCanvas;


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mAvatarViewModel = new AvatarViewModel(mMockImageCacheWrapper);
        mAvatarViewModel.setView(mMockView);
    }

    @Test
    public void testIfIdentityIsSet() {
        mAvatarViewModel.update();
        verify(mMockView).setClusterSizes(ArgumentMatchers.<Identity, String>anyMap(), ArgumentMatchers.<BitmapWrapper>anyList());
        verify(mMockView).revalidateView();
    }

    @Test
    public void testIfAvailableIsDrawn() {
        mAvatarViewModel.checkPresence(Presence.PresenceStatus.AVAILABLE, mMockCanvas);
        verify(mMockView).drawAvailable(mMockCanvas);
        verify(mMockView, never()).drawAway(mMockCanvas);
        verify(mMockView, never()).drawBusy(mMockCanvas);
        verify(mMockView, never()).drawInvisible(mMockCanvas);
        verify(mMockView, never()).drawOffline(mMockCanvas);
    }

    @Test
    public void testIfAwayIsDrawn() {
        mAvatarViewModel.checkPresence(Presence.PresenceStatus.AWAY, mMockCanvas);
        verify(mMockView).drawAway(mMockCanvas);
        verify(mMockView, never()).drawAvailable(mMockCanvas);
        verify(mMockView, never()).drawBusy(mMockCanvas);
        verify(mMockView, never()).drawInvisible(mMockCanvas);
        verify(mMockView, never()).drawOffline(mMockCanvas);
    }

    @Test
    public void testIfBusyIsDrawn() {
        mAvatarViewModel.checkPresence(Presence.PresenceStatus.BUSY, mMockCanvas);
        verify(mMockView).drawBusy(mMockCanvas);
        verify(mMockView, never()).drawAvailable(mMockCanvas);
        verify(mMockView, never()).drawAway(mMockCanvas);
        verify(mMockView, never()).drawInvisible(mMockCanvas);
        verify(mMockView, never()).drawOffline(mMockCanvas);
    }

    @Test
    public void testIfInvisibleIsDrawn() {
        mAvatarViewModel.checkPresence(Presence.PresenceStatus.INVISIBLE, mMockCanvas);
        verify(mMockView).drawInvisible(mMockCanvas);
        verify(mMockView, never()).drawAvailable(mMockCanvas);
        verify(mMockView, never()).drawAway(mMockCanvas);
        verify(mMockView, never()).drawBusy(mMockCanvas);
        verify(mMockView, never()).drawOffline(mMockCanvas);
    }

    @Test
    public void testIfOfflineIsDrawn() {
        mAvatarViewModel.checkPresence(Presence.PresenceStatus.OFFLINE, mMockCanvas);
        verify(mMockView).drawOffline(mMockCanvas);
        verify(mMockView, never()).drawAvailable(mMockCanvas);
        verify(mMockView, never()).drawAway(mMockCanvas);
        verify(mMockView, never()).drawBusy(mMockCanvas);
        verify(mMockView, never()).drawInvisible(mMockCanvas);
    }

}