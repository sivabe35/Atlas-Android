package com.layer.ui.typingindicators;

import android.content.Context;
import android.content.res.Resources;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.layer.ui.avatar.AvatarView;
import com.layer.ui.TypingIndicatorLayout;
import com.layer.ui.R;
import com.layer.sdk.listeners.LayerTypingIndicatorListener;
import com.layer.sdk.messaging.Identity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AvatarTypingIndicatorFactory implements TypingIndicatorLayout.TypingIndicatorFactory<LinearLayout> {
    private static final String TAG = AvatarTypingIndicatorFactory.class.getSimpleName();

    private static final int DOT_RES_ID = R.drawable.ui_typing_indicator_dot;
    private static final float DOT_ON_ALPHA = 0.31f;
    private static final long ANIMATION_PERIOD = 600;
    private static final long ANIMATION_OFFSET = ANIMATION_PERIOD / 3;

    public AvatarTypingIndicatorFactory() {}

    @Override
    public LinearLayout onCreateView(Context context) {
        Tag tag = new Tag();

        Resources r = context.getResources();
        int dotSize = r.getDimensionPixelSize(R.dimen.layer_ui_typing_indicator_dot_size);
        int dotSpace = r.getDimensionPixelSize(R.dimen.layer_ui_typing_indicator_dot_space);

        LinearLayout l = new LinearLayout(context);
        l.setGravity(Gravity.CENTER);
        l.setOrientation(LinearLayout.HORIZONTAL);
        l.setLayoutParams(new TypingIndicatorLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        ImageView v;
        LinearLayout.LayoutParams vp;

        v = new ImageView(context);
        vp = new LinearLayout.LayoutParams(dotSize, dotSize);
        vp.setMargins(0, 0, dotSpace, 0);
        v.setLayoutParams(vp);
        v.setBackgroundDrawable(r.getDrawable(DOT_RES_ID));
        tag.mDots.add(v);
        l.addView(v);

        v = new ImageView(context);
        vp = new LinearLayout.LayoutParams(dotSize, dotSize);
        vp.setMargins(0, 0, dotSpace, 0);
        v.setLayoutParams(vp);
        v.setBackgroundDrawable(r.getDrawable(DOT_RES_ID));
        tag.mDots.add(v);
        l.addView(v);

        v = new ImageView(context);
        vp = new LinearLayout.LayoutParams(dotSize, dotSize);
        v.setLayoutParams(vp);
        v.setBackgroundDrawable(r.getDrawable(DOT_RES_ID));
        tag.mDots.add(v);
        l.addView(v);

        l.setTag(tag);
        return l;
    }

    @Override
    public void onBindView(LinearLayout l, Map<Identity, LayerTypingIndicatorListener.TypingIndicator> typingUserIds) {
        @SuppressWarnings("unchecked")
        Tag tag = (Tag) l.getTag();

        int avatarSpace = l.getResources().getDimensionPixelSize(R.dimen.layer_ui_padding_narrow);
        int avatarDim = l.getResources().getDimensionPixelSize(R.dimen.layer_ui_message_avatar_item_single);

        // Iterate over existing typists and remove non-typists
        List<AvatarView> newlyFinished = new ArrayList<AvatarView>();
        Set<Identity> newlyActives = new HashSet<>(typingUserIds.keySet());
        for (AvatarView avatarView : tag.mActives) {
            Identity existingTypist = avatarView.getParticipants().iterator().next();
            if (!typingUserIds.containsKey(existingTypist) || (typingUserIds.get(existingTypist) == LayerTypingIndicatorListener.TypingIndicator.FINISHED)) {
                // Newly finished
                newlyFinished.add(avatarView);
            } else {
                // Existing started or paused
                avatarView.setAlpha(typingUserIds.get(existingTypist) == LayerTypingIndicatorListener.TypingIndicator.STARTED ? 1f : 0.5f);
                newlyActives.remove(existingTypist);
            }
        }

        for (AvatarView avatarView : newlyFinished) {
            tag.mActives.remove(avatarView);
            tag.mPassives.add(avatarView);
            l.removeView(avatarView);
        }

        // Add new typists
        for (Identity typist : newlyActives) {
            AvatarView avatarView = tag.mPassives.poll();
            if (avatarView == null) {
                // TODO: allow styling
                avatarView = new AvatarView(l.getContext()).init();
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(avatarDim, avatarDim);
                params.setMargins(0, 0, avatarSpace, 0);
                avatarView.setLayoutParams(params);
            }
            avatarView.setAlpha(typingUserIds.get(typist) == LayerTypingIndicatorListener.TypingIndicator.STARTED ? 1f : 0.5f);
            tag.mActives.add(avatarView);
            l.addView(avatarView, 0);
            avatarView.setParticipants(typist);
        }

        // Dot animations
        View dot1 = tag.mDots.get(0);
        View dot2 = tag.mDots.get(1);
        View dot3 = tag.mDots.get(2);

        Boolean animating = (Boolean) dot1.getTag();
        if (animating == null) animating = false;

        if (animating && typingUserIds.isEmpty()) {
            // Stop animating
            dot1.clearAnimation();
            dot2.clearAnimation();
            dot3.clearAnimation();
            dot1.setTag(true);
        } else if (!animating && !typingUserIds.isEmpty()) {
            // Start animating
            dot1.setAlpha(DOT_ON_ALPHA);
            dot2.setAlpha(DOT_ON_ALPHA);
            dot3.setAlpha(DOT_ON_ALPHA);
            startAnimation(dot1, ANIMATION_PERIOD, 0);
            startAnimation(dot2, ANIMATION_PERIOD, ANIMATION_OFFSET);
            startAnimation(dot3, ANIMATION_PERIOD, ANIMATION_OFFSET + ANIMATION_OFFSET);
            dot1.setTag(false);
        }
    }

    /**
     * Starts a repeating fade out / fade in with the given period and offset in milliseconds.
     *
     * @param view        View to start animating.
     * @param period      Length of time in milliseconds for the fade out / fade in period.
     * @param startOffset Length of time in milliseconds to delay the initial start.
     */
    private void startAnimation(final View view, long period, long startOffset) {
        final AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
        fadeOut.setDuration(period / 2);
        fadeOut.setStartOffset(startOffset);
        fadeOut.setInterpolator(COSINE_INTERPOLATOR);

        final AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(period / 2);
        fadeIn.setStartOffset(0);
        fadeIn.setInterpolator(COSINE_INTERPOLATOR);

        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                fadeIn.setStartOffset(0);
                fadeIn.reset();
                view.startAnimation(fadeIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                fadeOut.setStartOffset(0);
                fadeOut.reset();
                view.startAnimation(fadeOut);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        view.startAnimation(fadeOut);
    }

    /**
     * Ease in/out
     */
    private final Interpolator COSINE_INTERPOLATOR = new Interpolator() {
        @Override
        public float getInterpolation(float input) {
            return (float) (1.0 - Math.cos(input * Math.PI / 2.0));
        }
    };

    private static class Tag {
        public final ArrayList<View> mDots = new ArrayList<View>(3);
        public final LinkedList<AvatarView> mActives = new LinkedList<AvatarView>();
        public final LinkedList<AvatarView> mPassives = new LinkedList<AvatarView>();
    }
}
