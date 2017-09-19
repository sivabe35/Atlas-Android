package com.layer.ui.avatar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.layer.sdk.messaging.Identity;
import com.layer.ui.R;
import com.layer.ui.identity.IdentityFormatter;
import com.layer.ui.util.AvatarStyle;
import com.layer.ui.util.imagecache.BitmapWrapper;
import com.layer.ui.util.imagecache.ImageCacheWrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * AtlasAvatar can be used to show information about one user, or as a cluster of multiple users.
 * <p>
 * AtlasAvatar uses Picasso to render the avatar image. So, you need to init
 */
public class AvatarView extends View {

    private static final Paint PAINT_TRANSPARENT = new Paint();
    private static final Paint PAINT_BITMAP = new Paint();

    private final Paint mPaintInitials = new Paint();
    private final Paint mPaintBorder = new Paint();
    private final Paint mPaintBackground = new Paint();

    private int mMaxAvatar = 2;
    private static final float BORDER_SIZE_DP = 1f;
    private static final float MULTI_FRACTION = 26f / 40f;

    static {
        PAINT_TRANSPARENT.setARGB(0, 255, 255, 255);
        PAINT_TRANSPARENT.setAntiAlias(true);

        PAINT_BITMAP.setARGB(255, 255, 255, 255);
        PAINT_BITMAP.setAntiAlias(true);
    }

    private Set<Identity> mParticipants;

    private final Map<Identity, BitmapWrapper> mIdentityBitmapWrapperMap = new HashMap<>();
    private final Map<Identity, String> mInitials = new HashMap<>();
    private final List<BitmapWrapper> mPendingLoads = new ArrayList<>();

    // Sizing set in setClusterSizes() and used in onDraw()
    private float mOuterRadius;
    private float mInnerRadius;
    private float mCenterX;
    private float mCenterY;
    private float mDeltaX;
    private float mDeltaY;
    private float mTextSize;

    private Rect mRect = new Rect();
    private RectF mContentRect = new RectF();
    private AvatarViewModel mViewModel;
    private int mParticipantsInitialSize;

    public AvatarView(Context context) {
        super(context);
        mParticipants = new LinkedHashSet<>();
    }

    public AvatarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        mParticipants = new LinkedHashSet<>();
    }

    public AvatarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        parseStyle(getContext(), attrs, defStyleAttr);
        mParticipants = new LinkedHashSet<>();
    }

    public AvatarView init(@NonNull AvatarViewModel avatarViewModel, @NonNull IdentityFormatter identityFormatter) {
        mViewModel = avatarViewModel;
        mViewModel.setIdentityFormatter(identityFormatter);

        mParticipants = new LinkedHashSet<>();

        mPaintInitials.setAntiAlias(true);
        mPaintInitials.setSubpixelText(true);
        mPaintBorder.setAntiAlias(true);
        mPaintBackground.setAntiAlias(true);

        mPaintBackground.setColor(getResources().getColor(R.color.layer_ui_avatar_background));
        mPaintBorder.setColor(getResources().getColor(R.color.layer_ui_avatar_border));
        mPaintInitials.setColor(getResources().getColor(R.color.layer_ui_avatar_text));

        return this;
    }

    public void setStyle(AvatarStyle avatarStyle) {
        mPaintBackground.setColor(avatarStyle.getAvatarBackgroundColor());
        mPaintBorder.setColor(avatarStyle.getAvatarBorderColor());
        mPaintInitials.setColor(avatarStyle.getAvatarTextColor());
        mPaintInitials.setTypeface(avatarStyle.getAvatarTextTypeface());
    }

    public void setParticipants(Identity... participants) {
        mParticipants.clear();
        mParticipants.addAll(Arrays.asList(participants));
        mParticipantsInitialSize = mParticipants.size();
        update();
    }

    /**
     * Should be called from UI thread.
     */
    public void setParticipants(Set<Identity> participants) {
        mParticipants = participants;
        mParticipantsInitialSize = mParticipants != null ? mParticipants.size() : 0;
        update();
    }

    public Set<Identity> getParticipants() {
        return new LinkedHashSet<>(mParticipants);
    }

    private void update() {
        // Limit to mMaxAvatar valid avatars, prioritizing participants with avatars.
        if (mParticipants.size() > mMaxAvatar) {
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
            int numWithout = Math.min(mMaxAvatar - withAvatars.size(), withoutAvatars.size());
            for (int i = 0; i < numWithout; i++) {
                mParticipants.add(withoutAvatars.remove());
            }
            int numWith = Math.min(mMaxAvatar, withAvatars.size());
            for (int i = 0; i < numWith; i++) {
                mParticipants.add(withAvatars.remove());
            }
        }

        Diff diff = diff(mInitials.keySet(), mParticipants);
        List<BitmapWrapper> toLoad = new ArrayList<>();

        List<BitmapWrapper> recyclableBitmapWrappers = new ArrayList<>();
        for (Identity removed : diff.removed) {
            mInitials.remove(removed);
            BitmapWrapper bitmapWrapper = mIdentityBitmapWrapperMap.remove(removed);
            if (bitmapWrapper != null && removed.getAvatarImageUrl() != null) {
                mViewModel.getImageCacheWrapper().cancelBitmap(bitmapWrapper);
                recyclableBitmapWrappers.add(bitmapWrapper);
            }
        }

        for (Identity added : diff.added) {
            if (added == null) return;
            mInitials.put(added, mViewModel.getInitialsForAvatarView(added));

            final BitmapWrapper bitmapWrapper;
            if (recyclableBitmapWrappers.isEmpty()) {
                bitmapWrapper = new BitmapWrapper(added.getAvatarImageUrl(), 0, 0, false);
            } else {
                bitmapWrapper = recyclableBitmapWrappers.remove(0);
            }
            bitmapWrapper.setUrl(added.getAvatarImageUrl());
            mIdentityBitmapWrapperMap.put(added, bitmapWrapper);
            toLoad.add(bitmapWrapper);
        }

        // Cancel existing in case the size or anything else changed.
        // TODO: make caching intelligent wrt sizing
        for (Identity existing : diff.existing) {
            if (existing == null) continue;
            mInitials.put(existing, mViewModel.getInitialsForAvatarView(existing));
            String url = existing.getAvatarImageUrl() != null ? existing.getAvatarImageUrl() : "";
            if (!url.isEmpty()) {
                BitmapWrapper existingBitmapWrapper = mIdentityBitmapWrapperMap.get(existing);
                mViewModel.getImageCacheWrapper().cancelBitmap(existingBitmapWrapper);
                toLoad.add(existingBitmapWrapper);
            }

        }
        for (BitmapWrapper bitmapWrapper : mPendingLoads) {
            mViewModel.getImageCacheWrapper().cancelBitmap(bitmapWrapper);
        }
        mPendingLoads.clear();
        mPendingLoads.addAll(toLoad);

        setClusterSizes();

        // Invalidate the current view, so it refreshes with new value.
        postInvalidate();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (!changed) return;
        setClusterSizes();
    }

    private boolean setClusterSizes() {
        int avatarCount = mInitials.size();
        if (avatarCount == 0) return false;
        ViewGroup.LayoutParams params = getLayoutParams();
        if (params == null) return false;
        boolean hasBorder = (avatarCount != 1);

        int drawableWidth = params.width - (getPaddingLeft() + getPaddingRight());
        int drawableHeight = params.height - (getPaddingTop() + getPaddingBottom());
        float dimension = Math.min(drawableWidth, drawableHeight);
        float density = getContext().getResources().getDisplayMetrics().density;
        float fraction = (avatarCount > 1) ? MULTI_FRACTION : 1;

        mOuterRadius = fraction * dimension / 2f;
        mInnerRadius = mOuterRadius - (density * BORDER_SIZE_DP);

        mTextSize = mInnerRadius * 4f / 5f;
        mCenterX = getPaddingLeft() + mOuterRadius;
        mCenterY = getPaddingTop() + mOuterRadius;

        float outerMultiSize = fraction * dimension;
        mDeltaX = (drawableWidth - outerMultiSize) / (avatarCount - 1);
        mDeltaY = (drawableHeight - outerMultiSize) / (avatarCount - 1);

        synchronized (mPendingLoads) {
            if (!mPendingLoads.isEmpty()) {
                int size = Math.round(hasBorder ? (mInnerRadius * 2f) : (mOuterRadius * 2f));
                for (BitmapWrapper bitmapWrapper : mPendingLoads) {
                    String url = bitmapWrapper.getUrl();
                    // Handle empty paths just like null paths. This ensures empty paths will go
                    // through the normal Picasso flow and the bitmap is set.
                    if (url != null && url.trim().length() == 0) {
                        url = null;
                    }

                    if (url != null) {
                        bitmapWrapper.setWidth(size);
                        bitmapWrapper.setHeight(size);
                        bitmapWrapper.setMultiTransform(avatarCount > 1);
                        mViewModel.getImageCacheWrapper().fetchBitmap(bitmapWrapper,
                                new ImageCacheWrapper.Callback() {
                                    @Override
                                    public void onSuccess() {
                                        updateView();
                                    }

                                    @Override
                                    public void onFailure() {
                                        updateView();
                                    }
                                });
                    }
                }
                mPendingLoads.clear();
            }
        }
        return true;
    }

    private void updateView() {
        Handler handler = getHandler();
        if (handler != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    invalidate();
                }
            });
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Clear canvas
        int avatarCount = mInitials.size();
        canvas.drawRect(0f, 0f, canvas.getWidth(), canvas.getHeight(), PAINT_TRANSPARENT);
        if (avatarCount == 0) return;
        boolean hasBorder = (avatarCount != 1);
        float contentRadius = hasBorder ? mInnerRadius : mOuterRadius;

        // Draw avatar cluster
        float cx = mCenterX;
        float cy = mCenterY;
        mContentRect.set(cx - contentRadius, cy - contentRadius, cx + contentRadius, cy + contentRadius);

        boolean hasDrawnGroupAvatarResource = false;
        for (Map.Entry<Identity, String> entry : mInitials.entrySet()) {
            // Border / background
            if (hasBorder) canvas.drawCircle(cx, cy, mOuterRadius, mPaintBorder);

            // Initials or bitmap
            Identity identity = entry.getKey();
            BitmapWrapper bitmapWrapper = mIdentityBitmapWrapperMap.get(identity);
            Bitmap bitmap = (bitmapWrapper == null) ? null : bitmapWrapper.getBitmap();

            //Check if the participants are more than two and display the group avatar placeholder
            if (mParticipantsInitialSize > 2 && !hasDrawnGroupAvatarResource) {
                hasDrawnGroupAvatarResource = true;
                Drawable avatarPlaceholder = getContext().getResources().getDrawable(R.mipmap.avatar_placeholder);
                Bitmap avatarPlaceholderBitmap = ((BitmapDrawable) avatarPlaceholder).getBitmap();
                canvas.drawBitmap(avatarPlaceholderBitmap, mContentRect.left, mContentRect.top, PAINT_BITMAP);
            } else {
                if (bitmap != null && identity.getAvatarImageUrl() != null) {
                    canvas.drawBitmap(bitmap, mContentRect.left, mContentRect.top, PAINT_BITMAP);
                } else {
                    String initials = entry.getValue();
                    mPaintInitials.setTextSize(mTextSize);
                    mPaintInitials.getTextBounds(initials, 0, initials.length(), mRect);
                    canvas.drawCircle(cx, cy, contentRadius, mPaintBackground);
                    canvas.drawText(initials, cx - mRect.centerX(), cy - mRect.centerY() - 1f, mPaintInitials);
                }
            }

            // Translate for next avatar
            cx += mDeltaX;
            cy += mDeltaY;
            mContentRect.offset(mDeltaX, mDeltaY);
        }
    }

    private void parseStyle(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray ta = context.getTheme().obtainStyledAttributes(attrs, R.styleable.AvatarView, R.attr.AvatarView, defStyleAttr);
        mMaxAvatar = ta.getInt(R.styleable.AvatarView_maximumAvatars, 2);
        ta.recycle();
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