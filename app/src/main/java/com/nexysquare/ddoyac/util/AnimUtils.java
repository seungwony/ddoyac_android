package com.nexysquare.ddoyac.util;

import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.view.ViewCompat;
import androidx.core.view.ViewPropertyAnimatorCompat;
import androidx.core.view.ViewPropertyAnimatorListener;

public class AnimUtils {

    public static final int DEFAULT_DURATION = -1;
    public static final int NO_DELAY = 0;

    public static class AnimationCallback {

        public void onAnimationEnd() {}
        public void onAnimationCancel() {}
    }
    public static void crossFadeViews(View fadeIn, View fadeOut, int duration) {
        fadeIn(fadeIn, duration);
        fadeOut(fadeOut, duration);
    }
    public static void fadeOut(View fadeOut, int duration) {
        fadeOut(fadeOut, duration, null);
    }
    public static void fadeOut(final View fadeOut, int durationMs,
                               final AnimationCallback callback) {
        fadeOut.setAlpha(1);
        final ViewPropertyAnimatorCompat animator = ViewCompat.animate(fadeOut);
        animator.cancel();
        animator.alpha(0).withLayer().setListener(new ViewPropertyAnimatorListener() {

            @Override
            public void onAnimationStart(View view) {

            }

            @Override
            public void onAnimationEnd(View view) {
                fadeOut.setVisibility(View.GONE);
                if (callback != null) {
                    callback.onAnimationEnd();
                }
            }

            @Override
            public void onAnimationCancel(View view) {
                fadeOut.setVisibility(View.GONE);
                fadeOut.setAlpha(0);
                if (callback != null) {
                    callback.onAnimationCancel();
                }
            }
        });
        if (durationMs != DEFAULT_DURATION) {
            animator.setDuration(durationMs);
        }
        animator.start();
    }
    public static void fadeIn(View fadeIn, int durationMs) {
        fadeIn(fadeIn, durationMs, NO_DELAY, null);
    }
    public static void fadeIn(final View fadeIn, int durationMs, int delay,
                              final AnimationCallback callback) {
        fadeIn.setAlpha(0);
        final ViewPropertyAnimatorCompat animator = ViewCompat.animate(fadeIn);
        animator.cancel();
        animator.setStartDelay(delay);
        animator.alpha(1).withLayer().setListener(new ViewPropertyAnimatorListener() {
            @Override
            public void onAnimationStart(View view) {
                fadeIn.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationCancel(View view) {
                fadeIn.setAlpha(1);
                if (callback != null) {
                    callback.onAnimationCancel();
                }
            }

            @Override
            public void onAnimationEnd(View view) {
                if (callback != null) {
                    callback.onAnimationEnd();
                }
            }
        });
        if (durationMs != DEFAULT_DURATION) {
            animator.setDuration(durationMs);
        }
        animator.start();
    }

    public static void animateHeight(final View view, int from, int to, int duration) {
        boolean expanding = to > from;

        ValueAnimator anim = ValueAnimator.ofInt(from, to);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                layoutParams.height = val;
                view.setLayoutParams(layoutParams);
            }
        });
        anim.setDuration(duration);
        anim.start();

        view.animate().alpha(expanding ? 1 : 0).setDuration(duration / 2).start();
    }
}
