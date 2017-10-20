package com.layer.ui.presence;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import com.layer.sdk.messaging.Identity;
import com.layer.sdk.messaging.PresenceStatus;
import com.layer.ui.R;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

public class PresenceView extends View {

    private final Paint mBackgroundPaint = new Paint();
    private final Paint mPresencePaint = new Paint();

    private Identity mIdentity;
    private int mAvailableColor = ContextCompat.getColor(getContext(), R.color.layer_ui_presence_available);
    private int mBusyColor = ContextCompat.getColor(getContext(), R.color.layer_ui_presence_busy);
    private int mAwayColor = ContextCompat.getColor(getContext(), R.color.layer_ui_presence_away);
    private int mInvisibleColor = ContextCompat.getColor(getContext(), R.color.layer_ui_presence_invisible);
    private int mOfflineColor = ContextCompat.getColor(getContext(), R.color.layer_ui_presence_offline);

    public PresenceView(Context context) {
        super(context);
    }

    public PresenceView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PresenceView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        parseStyle(context, attrs, defStyleAttr);
    }

    public void setParticipants(Set<Identity> participants) {
        processSetParticipants(participants);
    }

    public void setParticipants(Identity... participants) {
        processSetParticipants(Arrays.asList(participants));
    }

    private void processSetParticipants(Collection<Identity> participants) {
        if (participants.size() == 1) {
            mIdentity = participants.iterator().next();
            setVisibility(VISIBLE);
            invalidate();
        } else {
            setVisibility(INVISIBLE);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawPresence(canvas);
    }

    private void drawPresence(Canvas canvas) {

        PresenceStatus currentStatus = mIdentity != null ? mIdentity.getPresenceStatus() : null;
        if (currentStatus == null) {
            return;
        }

        switch (currentStatus) {
            case AVAILABLE:
                drawAvailable(canvas);
                break;
            case AWAY:
                drawAway(canvas);
                break;
            case OFFLINE:
                drawOffline(canvas);
                break;
            case INVISIBLE:
                drawInvisible(canvas);
                break;
            case BUSY:
                drawBusy(canvas);
                break;
        }
    }

    public void drawAvailable(Canvas canvas) {
        mPresencePaint.setColor(mAvailableColor);
        drawPresence(canvas, false);
    }

    public void drawAway(Canvas canvas) {
        mPresencePaint.setColor(mAwayColor);
        drawPresence(canvas, false);
    }

    public void drawOffline(Canvas canvas) {
        mPresencePaint.setColor(mOfflineColor);
        drawPresence(canvas, true);
    }

    public void drawInvisible(Canvas canvas) {
        mPresencePaint.setColor(mInvisibleColor);
        drawPresence(canvas, true);
    }

    public void drawBusy(Canvas canvas) {
        mPresencePaint.setColor(mBusyColor);
        drawPresence(canvas, false);
    }

    private void drawPresence(Canvas canvas,boolean makeCircleHollow) {

        int drawableWidth = getMeasuredWidth() - (getPaddingLeft() + getPaddingRight());
        int drawableHeight = getMeasuredHeight() - (getPaddingTop() + getPaddingBottom());
        float dimension = Math.min(drawableWidth, drawableHeight);
        float density = getContext().getResources().getDisplayMetrics().density;
        float fraction = 1f;
        float BORDER_SIZE_DP = 1F;

        float outerRadius = fraction * dimension / 2f;
        float innerRadius = outerRadius - (density * BORDER_SIZE_DP);
        float centerX = getPaddingLeft() + outerRadius;
        float centerY = getPaddingTop() + outerRadius;

        // Presence
        float presenceOuterRadius = outerRadius / 3f;
        float presenceInnerRadius = innerRadius / 3f;
        float presenceCenterX = centerX + outerRadius - presenceOuterRadius;
        float presenceCenterY = centerY + outerRadius - presenceOuterRadius;

        // Clear background + create border
        mBackgroundPaint.setColor(Color.WHITE);
        mBackgroundPaint.setAntiAlias(true);
        canvas.drawCircle(presenceCenterX, presenceCenterY, presenceOuterRadius, mBackgroundPaint);

        // Draw Presence status
        mPresencePaint.setAntiAlias(true);
        canvas.drawCircle(presenceCenterX, presenceCenterY, presenceInnerRadius, mPresencePaint);

        // Draw hollow if needed
        if (makeCircleHollow) {
            canvas.drawCircle(presenceCenterX, presenceCenterY, (presenceInnerRadius / 2f), mBackgroundPaint);
        }
    }

    private void parseStyle(Context context, AttributeSet attrs, int defStyle) {

        TypedArray ta = context.getTheme().obtainStyledAttributes(attrs, R.styleable.PresenceView, R.attr.PresenceView, defStyle);
        this.mAvailableColor = ta.getColor(R.styleable.PresenceView_presenceAvailableColor, getContext().getResources().getColor(R.color.layer_ui_presence_available));
        this.mBusyColor = ta.getColor(R.styleable.PresenceView_presenceBusyColor,getContext().getResources().getColor(R.color.layer_ui_presence_busy));
        this.mAwayColor = ta.getColor(R.styleable.PresenceView_presenceAwayColor, getContext().getResources().getColor(R.color.layer_ui_presence_away));
        this.mInvisibleColor = ta.getColor(R.styleable.PresenceView_presenceInvisibleColor, getContext().getResources().getColor(R.color.layer_ui_presence_invisible));
        this.mOfflineColor = ta.getColor(R.styleable.PresenceView_presenceOfflineColor, getContext().getResources().getColor(R.color.layer_ui_presence_offline));
        ta.recycle();
    }
}
