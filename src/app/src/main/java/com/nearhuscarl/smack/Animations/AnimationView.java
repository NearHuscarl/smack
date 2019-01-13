package com.nearhuscarl.smack.Animations;

import android.animation.TimeAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import com.nearhuscarl.smack.R;

import java.util.Random;

@SuppressLint("NewApi")
public class AnimationView extends View {

    private static class Attribute{
        private float x;
        private float y;
        private float scale;
        private float alpha;
        private float speed;
    }

    private static final int BASE_SPEED_DP_PER_S = 200;
    private static final int COUNT = 32;
    private static final int SEED = 1337;

    /** The minimum scale of a Favourite */
    private static final float SCALE_MIN_PART = 0.45f;
    /** How much of the scale that's based on randomness */
    private static final float SCALE_RANDOM_PART = 0.55f;
    /** How much of the alpha that's based on the scale of the Favourite */
    private static final float ALPHA_SCALE_PART = 0.5f;
    /** How much of the alpha that's based on randomness */
    private static final float ALPHA_RANDOM_PART = 0.5f;

    private final Attribute[] mAtribute = new Attribute[COUNT];
    private final Random mRnd = new Random(SEED);

    private TimeAnimator mTimeAnimator;
    private Drawable mDrawable;

    private float mBaseSpeed;
    private float mBaseSize;
    private long mCurrentPlayTime;

    /** @see View#View(Context) */
    public AnimationView(Context context) {
        super(context);
        init();
    }

    /** @see View#View(Context, AttributeSet) */
    public AnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /** @see View#View(Context, AttributeSet, int) */
    public AnimationView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mDrawable = ContextCompat.getDrawable(getContext(), R.drawable.heart_shape_silhouette);
        mBaseSize = Math.max(mDrawable.getIntrinsicWidth(), mDrawable.getIntrinsicHeight()) / 2f;
        mBaseSpeed = BASE_SPEED_DP_PER_S * getResources().getDisplayMetrics().density;
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldw, int oldh) {
        super.onSizeChanged(width, height, oldw, oldh);
        // The starting position is dependent on the size of the view,
        // which is why the model is initialized here, when the view is measured.
        for (int i = 0; i < mAtribute.length; i++) {
            final Attribute attribute = new Attribute();
            initializeStar(attribute, width, height);
            mAtribute[i] = attribute;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final int viewHeight = getHeight();
        for (final Attribute favourite : mAtribute) {
            // Ignore the favourite if it's outside of the view bounds
            final float starSize = favourite.scale * mBaseSize;
            if (favourite.y + starSize < 0 || favourite.y - starSize > viewHeight) {
                continue;
            }
            // Save the current canvas state
            final int save = canvas.save();
            // Move the canvas to the center of the favourite
            canvas.translate(favourite.x, favourite.y);
            // Rotate the canvas based on how far the favourite has moved
            final float progress = (favourite.y + starSize) / viewHeight;
            canvas.rotate(360 * progress);

            // Prepare the size and alpha of the drawable
            final int size = Math.round(starSize);
            mDrawable.setBounds(-size, -size, size, size);
            mDrawable.setAlpha(Math.round(255 * favourite.alpha));

            // Draw the favourite to the canvas
            mDrawable.draw(canvas);

            // Restore the canvas to it's previous position and rotation
            canvas.restoreToCount(save);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mTimeAnimator = new TimeAnimator();
        mTimeAnimator.setTimeListener(new TimeAnimator.TimeListener() {
            @Override
            public void onTimeUpdate(TimeAnimator animation, long totalTime, long deltaTime) {
                if (!isLaidOut()) {
                    // Ignore all calls before the view has been measured and laid out.
                    return;
                }
                updateState(deltaTime);
                invalidate();
            }
        });
        mTimeAnimator.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mTimeAnimator.cancel();
        mTimeAnimator.setTimeListener(null);
        mTimeAnimator.removeAllListeners();
        mTimeAnimator = null;
    }

    /**
     * Pause the animation if it's running
     */

    /**
     * Resume the animation if not already running
     */

    /**
     * Progress the animation by moving the stars based on the elapsed time
     * @param deltaMs time delta since the last frame, in millis
     */
    private void updateState(float deltaMs) {
        // Converting to seconds since PX/S constants are easier to understand
        final float deltaSeconds = deltaMs / 1000f;
        final int viewWidth = getWidth();
        final int viewHeight = getHeight();

        for (final Attribute attribute : mAtribute) {
            // Move the attribute based on the elapsed time and it's speed
            attribute.y -= attribute.speed * deltaSeconds;

            // If the attribute is completely outside of the view bounds after
            // updating it's position, recycle it.
            final float size = attribute.scale * mBaseSize;
            if (attribute.y + size < 0) {
                initializeStar(attribute, viewWidth, viewHeight);
            }
        }
    }

    /**
     * Initialize the given attribute by randomizing it's position, scale and alpha
     * @param attribute the attribute to initialize
     * @param viewWidth the view width
     * @param viewHeight the view height
     */
    private void initializeStar(Attribute attribute, int viewWidth, int viewHeight) {
        // Set the scale based on a min value and a random multiplier
        attribute.scale = SCALE_MIN_PART + SCALE_RANDOM_PART * mRnd.nextFloat();

        // Set X to a random value within the width of the view
        attribute.x = viewWidth * mRnd.nextFloat();

        // Set the Y position
        // Start at the bottom of the view
        attribute.y = viewHeight;
        // The Y value is in the center of the attribute, add the size
        // to make sure it starts outside of the view bound
        attribute.y += attribute.scale * mBaseSize;
        // Add a random offset to create a small delay before the
        // favourite appears again.
        attribute.y += viewHeight * mRnd.nextFloat() / 4f;

        // The alpha is determined by the scale of the attribute and a random multiplier.
        attribute.alpha = ALPHA_SCALE_PART * attribute.scale + ALPHA_RANDOM_PART * mRnd.nextFloat();
        // The bigger and brighter a favourite is, the faster it moves
        attribute.speed = mBaseSpeed * attribute.alpha * attribute.scale;
    }
}