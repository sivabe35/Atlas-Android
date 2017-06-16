package com.layer.ui.avatar;

import android.graphics.Canvas;
import android.text.TextUtils;

import com.layer.sdk.messaging.Identity;
import com.layer.sdk.messaging.Presence;
import com.layer.ui.util.Util;
import com.layer.ui.util.picasso.ImageCacheWrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;


public class AvatarViewModel implements AvatarContract.ViewModel  {

    private Set<Identity> mParticipants = new LinkedHashSet<>();
    private final Map<Identity, String> mInitials = new HashMap<>();
    private final Map<Identity, UiImageTarget> mImageTargets = new HashMap<>();
    // Initials and Picasso image targets by user ID
    private final List<UiImageTarget> mPendingLoads = new ArrayList<>();

    private AvatarContract.View mView;

    // TODO: make these styleable
    private static final int MAX_AVATARS = 3;
    private ImageCacheWrapper mImageCacheWrapper;

    public AvatarViewModel(ImageCacheWrapper imageCacheWrapper) {
        mImageCacheWrapper = imageCacheWrapper;
    }

    @Override
    public void update() {
        // Limit to MAX_AVATARS valid avatars, prioritizing participants with avatars.
        if (mParticipants.size() > MAX_AVATARS) {
            Queue<Identity> withAvatars = new LinkedList<>();
            Queue<Identity> withoutAvatars = new LinkedList<>();
            for (Identity participant : mParticipants) {
                if (participant == null) continue;
                if (!TextUtils.isEmpty(participant.getAvatarImageUrl())) {
                    withAvatars.add(participant);
                } else {
                    withoutAvatars.add(participant);
                }
            }

            mParticipants = new LinkedHashSet<>();
            int numWithout = Math.min(MAX_AVATARS - withAvatars.size(), withoutAvatars.size());
            for (int i = 0; i < numWithout; i++) {
                mParticipants.add(withoutAvatars.remove());
            }
            int numWith = Math.min(MAX_AVATARS, withAvatars.size());
            for (int i = 0; i < numWith; i++) {
                mParticipants.add(withAvatars.remove());
            }
        }

        Diff diff = diff(mInitials.keySet(), mParticipants);
        List<UiImageTarget> toLoad = new ArrayList<>();

        List<UiImageTarget> recyclableTargets = new ArrayList<UiImageTarget>();
        for (Identity removed : diff.removed) {
            mInitials.remove(removed);
            UiImageTarget target = mImageTargets.remove(removed);
            if (target != null) {
                mImageCacheWrapper.cancelRequest(target);
                recyclableTargets.add(target);
            }
        }

        for (Identity added : diff.added) {
            if (added == null) return;
            mInitials.put(added, mView.getInitials(added));

            final UiImageTarget target;
            if (recyclableTargets.isEmpty()) {
                target = new UiImageTarget(mView.getAvatar());
            } else {
                target = recyclableTargets.remove(0);
            }
            target.setUrl(added.getAvatarImageUrl());
            mImageTargets.put(added, target);
            toLoad.add(target);
        }

        // Cancel existing in case the size or anything else changed.
        // TODO: make caching intelligent wrt sizing
        for (Identity existing : diff.existing) {
            if (existing == null) continue;
            mInitials.put(existing, Util.getInitials(existing));

            UiImageTarget existingTarget = mImageTargets.get(existing);
            mImageCacheWrapper.cancelRequest(existingTarget);
            toLoad.add(existingTarget);
        }

        for (UiImageTarget target : mPendingLoads) {
            mImageCacheWrapper.cancelRequest(target);
        }
        mPendingLoads.clear();
        mPendingLoads.addAll(toLoad);

        mView.setClusterSizes(mInitials,mPendingLoads);
        mView.revalidateView();
    }

    @Override
    public void setParticipants(Identity[] participants) {
        mParticipants.clear();
        mParticipants.addAll(Arrays.asList(participants));
        update();
    }

    @Override
    public void setParticipants(Set<Identity> participants) {
        mParticipants.clear();
        mParticipants.addAll(participants);
        update();
    }

    @Override
    public Set<Identity> getParticipants() {
        return new LinkedHashSet<>(mParticipants);
    }

    @Override
    public int getInitialSize() {
        return mInitials.size();
    }

    @Override
    public Set<Map.Entry<Identity, String>> getEntrySet() {
        return mInitials.entrySet();
    }

    @Override
    public UiImageTarget getImageTarget(Identity key) {
        return  mImageTargets.get(key);
    }

    @Override
    public void setClusterSizes() {
        mView.setClusterSizes(mInitials,mPendingLoads);
    }

    @Override
    public void loadImage(String targetUrl, String tag, Object placeHolder, Object fade, int size,
            int size1, boolean flag, ImageCacheWrapper.ImageTarget imageTarget) {
        mImageCacheWrapper.load(targetUrl,tag,null,null,size,size1,flag,imageTarget);
    }

    @Override
    public void checkPresence(Presence.PresenceStatus currentStatus, Canvas canvas) {
        switch (currentStatus) {
            case AVAILABLE:
                mView.drawAvailable(canvas);
                break;
            case AWAY:
                mView.drawAway(canvas);
                break;
            case OFFLINE:
                mView.drawOffline(canvas);
                break;
            case INVISIBLE:
               mView.drawInvisible(canvas);
                break;
            case BUSY:
                mView.drawBusy(canvas);
                break;
            default:
                mView.drawDefault(canvas);
                break;
        }
    }

    @Override
    public void setView(AvatarContract.View avatar) {
        mView = avatar;
    }

    private static Diff diff(Set<Identity> oldSet, Set<Identity> newSet) {
        Diff diff = new Diff();
        for (Identity old : oldSet) {
            if (newSet.contains(old)) {
                diff.existing.add(old);
            } else {
                diff.removed.add(old);
            }
        }
        for (Identity newItem : newSet) {
            if (!oldSet.contains(newItem)) {
                diff.added.add(newItem);
            }
        }
        return diff;
    }

    private static class Diff {
        public List<Identity> existing = new ArrayList<>();
        public List<Identity> added = new ArrayList<>();
        public List<Identity> removed = new ArrayList<>();
    }
}
