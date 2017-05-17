package com.layer.ui.avatar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.layer.ui.Injection;
import com.layer.ui.R;
import com.layer.ui.util.AvatarStyle;
import com.layer.sdk.messaging.Identity;
import com.layer.sdk.messaging.Presence;
import com.layer.ui.util.Util;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * AtlasAvatar can be used to show information about one user, or as a cluster of multiple users.
 *
 * AtlasAvatar uses Picasso to render the avatar image. So, you need to init
 */
public class AvatarView extends View implements AvatarContract.View {
    public static final String TAG = AvatarView.class.getSimpleName();

    private static final Paint PAINT_TRANSPARENT = new Paint();
    private static final Paint PAINT_BITMAP = new Paint();

    private final Paint mPaintInitials = new Paint();
    private final Paint mPaintBorder = new Paint();
    private final Paint mPaintBackground = new Paint();
    private final Paint mPresencePaint = new Paint();
    private final Paint mBackgroundPaint = new Paint();

    private boolean mShouldShowPresence = true;
    private AvatarInitials mAvatarInitials;

    // TODO: make these styleable
    private static final float BORDER_SIZE_DP = 1f;
    private static final float MULTI_FRACTION = 26f / 40f;

    //TODO: Inject this into View
    private AvatarContract.ViewModel mAvatarViewModel;

    static {
        PAINT_TRANSPARENT.setARGB(0, 255, 255, 255);
        PAINT_TRANSPARENT.setAntiAlias(true);

        PAINT_BITMAP.setARGB(255, 255, 255, 255);
        PAINT_BITMAP.setAntiAlias(true);
    }


    // Sizing set in setClusterSizes() and used in onDraw()
    private float mOuterRadius;
    private float mInnerRadius;
    private float mCenterX;
    private float mCenterY;
    private float mDeltaX;
    private float mDeltaY;
    private float mTextSize;
    private float mPresenceOuterRadius;
    private float mPresenceInnerRadius;
    private float mPresenceCenterX;
    private float mPresenceCenterY;

    private Rect mRect = new Rect();
    private RectF mContentRect = new RectF();

    public AvatarView(Context context) {
        super(context);
    }

    public AvatarView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AvatarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AvatarView init() {
        mAvatarViewModel = Injection.provideAvatarViewModel(getContext().getApplicationContext());
        setUpAvatarViewModel();
        mPaintInitials.setAntiAlias(true);
        mPaintInitials.setSubpixelText(true);
        mPaintBorder.setAntiAlias(true);
        mPaintBackground.setAntiAlias(true);

        mPaintBackground.setColor(getResources().getColor(R.color.layer_ui_avatar_background));
        mPaintBorder.setColor(getResources().getColor(R.color.layer_ui_avatar_border));
        mPaintInitials.setColor(getResources().getColor(R.color.layer_ui_avatar_text));

        return this;
    }

    public AvatarView init(AvatarInitials avatarInitials) {
        return init().setAvatarInitials(avatarInitials);
    }

    public AvatarView setAvatarInitials(AvatarInitials avatarInitials) {
        mAvatarInitials = avatarInitials;
        return this;
    }

    private void setUpAvatarViewModel() {
        mAvatarViewModel.setView(this);
    }

    public AvatarView setStyle(AvatarStyle avatarStyle) {
        mPaintBackground.setColor(avatarStyle.getAvatarBackgroundColor());
        mPaintBorder.setColor(avatarStyle.getAvatarBorderColor());
        mPaintInitials.setColor(avatarStyle.getAvatarTextColor());
        mPaintInitials.setTypeface(avatarStyle.getAvatarTextTypeface());
        return this;
    }

    @Override
    public AvatarView setParticipants(Identity... participants) {
        mAvatarViewModel.setParticipants(participants);
        mAvatarViewModel.update();
        return this;
    }

    /**
     * Enable or disable showing presence information for this avatar. Presence is shown only for
     * single user Avatars. If avatar is a cluster, presence will not be shown.
     *
     * Default is `true`, to show presence.
     *
     * @param shouldShowPresence set to `true` to show presence, `false` otherwise.
     * @return
     */
    public AvatarView setShouldShowPresence(boolean shouldShowPresence) {
        mShouldShowPresence = shouldShowPresence;
        return this;
    }

    /**
     * Returns if `shouldShowPresence` flag is enabled for this avatar.
     *
     * Default is `true`
     *
     * @return `true` if `shouldShowPresence` is set to `true`, `false` otherwise.
     */
    public boolean getShouldShowPresence() {
        return mShouldShowPresence;
    }

    /**
     * Should be called from UI thread.
     */
    @Override
    public AvatarView setParticipants(Set<Identity> participants) {
        mAvatarViewModel.setParticipants(participants);
        return this;
    }

    public Set<Identity> getParticipants() {
        return mAvatarViewModel.getParticipants();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (!changed) return;
        mAvatarViewModel.setClusterSizes();
    }

    @Override
    public boolean setClusterSizes( Map<Identity, String> mInitials,List<UiImageTarget> mPendingLoads ) {
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

        // Presence
        mPresenceOuterRadius = mOuterRadius / 3f;
        mPresenceInnerRadius = mInnerRadius / 3f;
        mPresenceCenterX = mCenterX + mOuterRadius - mPresenceOuterRadius;
        mPresenceCenterY = mCenterY + mOuterRadius - mPresenceOuterRadius;

        synchronized (mPendingLoads) {
            if (!mPendingLoads.isEmpty()) {
                int size = Math.round(hasBorder ? (mInnerRadius * 2f) : (mOuterRadius * 2f));
                for (UiImageTarget uiImageTarget : mPendingLoads) {
                    String targetUrl = uiImageTarget.getUrl();
                    // Handle empty paths just like null paths. This ensures empty paths will go
                    // through the normal Picasso flow and the bitmap is set.
                    if (targetUrl != null && targetUrl.trim().length() == 0) {
                        targetUrl = null;
                    }
                    //TODO: Turn into builder like partern
                    mAvatarViewModel.loadImage(targetUrl, AvatarView.TAG,null,null,size,size,
                            (avatarCount > 1),
                            uiImageTarget);
                }
                mPendingLoads.clear();
            }
        }
        return true;
    }

    @Override
    public void revalidateView() {
        // Invalidate the current view, so it refreshes with new value.
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Clear canvas
        int avatarCount = mAvatarViewModel.getInitialSize();
        canvas.drawRect(0f, 0f, canvas.getWidth(), canvas.getHeight(), PAINT_TRANSPARENT);
        if (avatarCount == 0) return;
        boolean hasBorder = (avatarCount != 1);
        float contentRadius = hasBorder ? mInnerRadius : mOuterRadius;

        // Draw avatar cluster
        float cx = mCenterX;
        float cy = mCenterY;
        mContentRect.set(cx - contentRadius, cy - contentRadius, cx + contentRadius, cy + contentRadius);
        for (Map.Entry<Identity, String> entry : mAvatarViewModel.getEntrySet()) {
            // Border / background
            if (hasBorder) canvas.drawCircle(cx, cy, mOuterRadius, mPaintBorder);

            // Initials or bitmap
            UiImageTarget uiImageTarget = mAvatarViewModel.getImageTarget(entry.getKey());
            Bitmap bitmap = (uiImageTarget == null) ? null : uiImageTarget.getBitmap();
            if (bitmap == null) {
                String initials = entry.getValue();
                mPaintInitials.setTextSize(mTextSize);
                mPaintInitials.getTextBounds(initials, 0, initials.length(), mRect);
                canvas.drawCircle(cx, cy, contentRadius, mPaintBackground);
                canvas.drawText(initials, cx - mRect.centerX(), cy - mRect.centerY() - 1f, mPaintInitials);
            } else {
                canvas.drawBitmap(bitmap, mContentRect.left, mContentRect.top, PAINT_BITMAP);
            }

            // Presence
            if (mShouldShowPresence && avatarCount == 1) { // Show only for single user avatars
                drawPresence(canvas, entry.getKey());
            }

            // Translate for next avatar
            cx += mDeltaX;
            cy += mDeltaY;
            mContentRect.offset(mDeltaX, mDeltaY);
        }
    }


    private void drawPresence(Canvas canvas, Identity identity) {
        Presence.PresenceStatus currentStatus = identity.getPresenceStatus();
        if (currentStatus == null) {
            return;
        }
        mAvatarViewModel.checkPresence(currentStatus, canvas);
    }

    @Override
    public void drawAvailable(Canvas canvas) {
        mPresencePaint.setColor(Color.rgb(0x4F, 0xBF, 0x62));
        drawPresence(canvas, false, true);
    }

    @Override
    public void drawAway(Canvas canvas) {
        mPresencePaint.setColor(Color.rgb(0xF7, 0xCA, 0x40));
        drawPresence(canvas, false, true);
    }

    @Override
    public void drawOffline(Canvas canvas) {
        mPresencePaint.setColor(Color.rgb(0x99, 0x99, 0x9c));
        drawPresence(canvas, true, true);
    }

    @Override
    public void drawInvisible(Canvas canvas) {
        mPresencePaint.setColor(Color.rgb(0x50, 0xC0, 0x62));
        drawPresence(canvas, true, true);
    }

    @Override
    public void drawBusy(Canvas canvas) {
        mPresencePaint.setColor(Color.rgb(0xE6, 0x44, 0x3F));
        drawPresence(canvas, false, true);
    }

    @Override
    public void drawDefault(Canvas canvas) {
        drawPresence(canvas, false, false);
    }

    private void drawPresence(Canvas canvas,boolean makeCircleHollow,  boolean drawPresence) {
        if (drawPresence) {
            // Clear background + create border
            mBackgroundPaint.setColor(Color.WHITE);
            mBackgroundPaint.setAntiAlias(true);
            canvas.drawCircle(mPresenceCenterX, mPresenceCenterY, mPresenceOuterRadius, mBackgroundPaint);

            // Draw Presence status
            mPresencePaint.setAntiAlias(true);
            canvas.drawCircle(mPresenceCenterX, mPresenceCenterY, mPresenceInnerRadius, mPresencePaint);

            // Draw hollow if needed
            if (makeCircleHollow) {
                canvas.drawCircle(mPresenceCenterX, mPresenceCenterY, (mPresenceInnerRadius / 2f), mBackgroundPaint);
            }
        }

    }
    @Override
    public String getInitials(Identity added) {
        return mAvatarInitials != null ? mAvatarInitials.getInitials(added) : Util.getInitials(added);
    }

    @Override
    public AvatarView getAvatar() {
        return this;
    }

}