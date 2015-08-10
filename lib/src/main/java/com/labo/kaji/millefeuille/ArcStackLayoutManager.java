package com.labo.kaji.millefeuille;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.PointF;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.nineoldandroids.view.ViewHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * @author kakajika
 * @since 2015/04/18.
 */
public class ArcStackLayoutManager extends BaseStackLayoutManager {

    private static final float MIN_HORIZONTAL_SCALE = 0.7f;
    private static final float SCROLL_FRICTION_SCALE = 0.02f;

    private float mMaxZ = 1.0f;
    private float mArcCurvature = 0.5f;
    private float mItemInterval = 16.0f;

    private float mScrollZ = 0.0f;
    private int mScrollState;
    private final Map<View, Animator> mAnimatorMap = new HashMap<>();

    public ArcStackLayoutManager(Context context) {
        super(context);
        mItemInterval = context.getResources().getDimensionPixelSize(R.dimen.arc_item_z_interval_default);
    }

    public void setArcCurvature(float arcCurvature) {
        mArcCurvature = arcCurvature;
    }

    public void setItemInterval(float itemInterval) {
        mItemInterval = itemInterval;
    }

    @Override
    public int computeVerticalScrollExtent(RecyclerView.State state) {
        return (int) mMaxZ;
    }

    @Override
    public int computeVerticalScrollOffset(RecyclerView.State state) {
        return (int) mScrollZ;
    }

    @Override
    public int computeVerticalScrollRange(RecyclerView.State state) {
        return (int) (getItemCount() * mItemInterval - mMaxZ );
    }

    @Override
    public void onScrollStateChanged(int state) {
        mScrollState = state;
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        log("onLayoutChildren: ", state);
        if (state.isPreLayout()) {
            return;
        }

        detachAndScrapAttachedViews(recycler);

        final int itemCount = getItemCount();
        final int parentTop = getPaddingTop();
        final int parentBottom = getHeight() - getPaddingBottom();
        final int parentHeight = parentBottom - parentTop;
        final int parentLeft = getPaddingLeft();
        final int parentRight = getWidth() - getPaddingRight();
        mMaxZ = (float) Math.sqrt(parentHeight / mArcCurvature);

        for (int idx = 0; idx < itemCount; ++idx) {
            float z = idx * mItemInterval - mScrollZ;
            if (z < -mItemInterval) {
                continue;
            }
            z = Math.max(0.0f, z);
            int top = (int) (z * z * mArcCurvature) + parentTop;
            if (top > parentBottom) {
                break;
            }
            View child = recycler.getViewForPosition(idx);
            addView(child);
            measureChildWithMargins(child, 0, 0);
            int bottom = top + getDecoratedMeasuredHeight(child);
            layoutDecorated(child, parentLeft, top, parentRight, bottom);
            setChildTransform(child, z, state.willRunPredictiveAnimations());
        }
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (getChildCount() == 0) {
            return 0;
        }

        final int parentTop = getPaddingTop();
        final int parentBottom = getHeight() - getPaddingBottom();
        final int parentLeft = getPaddingLeft();
        final int parentRight = getWidth() - getPaddingRight();
        final int itemCount = getItemCount();

        float dz = dy * SCROLL_FRICTION_SCALE;
        if (mScrollState != RecyclerView.SCROLL_STATE_DRAGGING) {
            if (dz < 0) {
                if (mScrollZ <= 0) {
//                    dz = (int) (-dz);
                    dz = 0;
                    dy = 0;
                }
            } else if (dz > 0) {
                if (mScrollZ >= computeVerticalScrollRange(state)) {
//                    dz -= 5;
                    dz = 0;
                    dy = 0;
                }
            }
        }
        mScrollZ += dz;
//        baseZ = Math.min(0.0f, Math.max(baseZ, -computeVerticalScrollRange(state)));
        int firstIndex = getPosition(getChildAt(0));
        int lastIndex  = firstIndex + getChildCount() - 1;

        if (dz > 0) {
            for (int idx = firstIndex; idx < itemCount; ++idx) {
                float z = idx * mItemInterval - mScrollZ;
                if (z < -mItemInterval) {
                    removeAndRecycleViewAt(0, recycler);
                    ++firstIndex;
                    continue;
                }
                z = Math.max(0.0f, z);
                int top = (int) (z * z * mArcCurvature) + parentTop;
                if (top > parentBottom) {
                    break;
                }

                final View child;
                if (idx <= lastIndex) {
                    child = getChildAt(idx - firstIndex);
                } else {
                    child = recycler.getViewForPosition(idx);
                    addView(child);
                    ++lastIndex;
                }
                measureChildWithMargins(child, 0, 0);
                layoutDecorated(child, parentLeft, top, parentRight, top + getDecoratedMeasuredHeight(child));
                setChildTransform(child, z);
            }
        } else if (dz < 0) {
            for (int idx = lastIndex; idx >= 0; --idx) {
                float z = idx * mItemInterval - mScrollZ;
                if (z < -mItemInterval) {
                    break;
                }
                z = Math.max(0.0f, z);
                int top = (int) (z * z * mArcCurvature) + parentTop;
                if (top > parentBottom) {
                    removeAndRecycleViewAt(lastIndex - firstIndex, recycler);
                    --lastIndex;
                    continue;
                }

                final View child;
                if (idx >= firstIndex) {
                    child = getChildAt(idx - firstIndex);
                } else {
                    child = recycler.getViewForPosition(idx);
                    addView(child, 0);
                    --firstIndex;
                }
                measureChildWithMargins(child, 0, 0);
                layoutDecorated(child, parentLeft, top, parentRight, top + getDecoratedMeasuredHeight(child));
                setChildTransform(child, z);
            }
        }

        return dy;
    }

    @Override
    public void scrollToPosition(int position) {
        mScrollZ = (position+1) * mItemInterval - mMaxZ;
        requestLayout();
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
        LinearSmoothScroller scroller = new LinearSmoothScroller(recyclerView.getContext() ) {
            @Override
            public PointF computeScrollVectorForPosition(int targetPosition) {
                return ArcStackLayoutManager.this.computeScrollVectorForPosition(targetPosition);
            }
        };
        scroller.setTargetPosition(position);
        startSmoothScroll(scroller);
    }

    private PointF computeScrollVectorForPosition(int targetPosition) {
        if (getChildCount() == 0) {
            return null;
        }
        float targetZ = (targetPosition+1) * mItemInterval - mMaxZ;
        if (mScrollZ < targetZ) {
            mScrollZ += 1;
            return new PointF(0, mArcCurvature);
        } else if (mScrollZ > targetZ) {
            mScrollZ -= 1;
            return new PointF(0, -mArcCurvature);
        }
        return null;
    }

    @Override
    protected void setChildTransform(@NonNull final View child, float z, boolean animated) {
        final float zScale = z / mMaxZ;
        final float preScale = ViewCompat.getScaleX(child);
        final float scale = zScale * (1.0f - MIN_HORIZONTAL_SCALE) + MIN_HORIZONTAL_SCALE;
        if (preScale == scale) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            child.setPivotX(child.getWidth() * 0.5f);
            child.setPivotY(0.0f);
            if (mAnimatorMap.containsKey(child)) {
                mAnimatorMap.remove(child).cancel();
            }
            if (animated) {
                ValueAnimator anim = ValueAnimator.ofFloat(preScale, scale);
                anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                        child.setScaleX((float) animation.getAnimatedValue());
                        child.setScaleY((float) animation.getAnimatedValue());
                    }
                });
                anim.setDuration(500);
                anim.start();
                mAnimatorMap.put(child, anim);
            } else {
                child.setScaleX(scale);
                child.setScaleY(scale);
            }
        } else {
            ViewHelper.setScaleX(child, 0.0f);
            ViewHelper.setScaleY(child, 0.0f);
            ViewHelper.setPivotY(child, 0.0f);
        }
        if (child instanceof ShadeApplicator) {
            final float shadowLevel = Math.max(0.0f, 1.0f - zScale - MIN_HORIZONTAL_SCALE * 0.5f);
            if (animated) {
                final float preShadowLevel = ((ShadeApplicator) child).getShadeLevel();
                ValueAnimator anim = ValueAnimator.ofFloat(preShadowLevel, shadowLevel);
                anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        ((ShadeApplicator) child).setShadeLevel((float) animation.getAnimatedValue());
                    }
                });
                anim.setDuration(500);
                anim.start();
            } else {
                ((ShadeApplicator) child).setShadeLevel(shadowLevel);
            }
        }
    }

}
