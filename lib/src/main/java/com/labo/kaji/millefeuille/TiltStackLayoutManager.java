package com.labo.kaji.millefeuille;

import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.nineoldandroids.view.ViewHelper;

/**
 * @author kakajika
 * @since 2015/08/08
 */
public class TiltStackLayoutManager extends RecyclerView.LayoutManager {

    private static final float SCROLL_FRICTION_SCALE = 0.02f;

    private float mItemInterval = 200.0f;
    private float mMaxY = 1.0f;
    private float mScrollY = 0.0f;
    private int mScrollState;

    public TiltStackLayoutManager(Context context) {
        super();
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public boolean canScrollVertically() {
        return true;
    }

    @Override
    public int computeVerticalScrollExtent(RecyclerView.State state) {
        return (int) mMaxY;
    }

    @Override
    public int computeVerticalScrollOffset(RecyclerView.State state) {
        return (int) mScrollY;
    }

    @Override
    public int computeVerticalScrollRange(RecyclerView.State state) {
        return (int) (getItemCount() * mItemInterval - mMaxY );
    }

    @Override
    public void onScrollStateChanged(int state) {
        mScrollState = state;
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        super.onLayoutChildren(recycler, state);
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
        mMaxY = (float) parentHeight;

        for (int idx = 0; idx < itemCount; ++idx) {
            float y = idx * mItemInterval - mScrollY;
            if (y < -mItemInterval) {
                continue;
            }
            y = Math.max(0.0f, y);
            int top = (int) y + parentTop;
            if (top > parentBottom) {
                break;
            }
            View child = recycler.getViewForPosition(idx);
            addView(child);
            measureChildWithMargins(child, 0, 0);
            int bottom = top + getDecoratedMeasuredHeight(child);
            layoutDecorated(child, parentLeft, top, parentRight, bottom);
            setChildTransform(child, y, state.willRunPredictiveAnimations());
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

//        float dz = dy * SCROLL_FRICTION_SCALE;
//        if (mScrollState != RecyclerView.SCROLL_STATE_DRAGGING) {
//            if (dz < 0) {
//                if (mScrollZ <= 0) {
////                    dz = (int) (-dz);
//                    dz = 0;
//                    dy = 0;
//                }
//            } else if (dz > 0) {
//                if (mScrollZ >= computeVerticalScrollRange(state)) {
////                    dz -= 5;
//                    dz = 0;
//                    dy = 0;
//                }
//            }
//        }
        mScrollY += dy;
//        baseZ = Math.min(0.0f, Math.max(baseZ, -computeVerticalScrollRange(state)));
        int firstIndex = getPosition(getChildAt(0));
        int lastIndex  = firstIndex + getChildCount() - 1;

        if (dy > 0) {
            for (int idx = firstIndex; idx < itemCount; ++idx) {
                float y = idx * mItemInterval - mScrollY;
                if (y < -mItemInterval) {
//                        Log.d(getClass().getSimpleName(), "remove child at "+firstIndex);
                    removeAndRecycleViewAt(0, recycler);
                    ++firstIndex;
                    continue;
                }
                y = Math.max(0.0f, y);
                int top = (int) y + parentTop;
                if (top > parentBottom) {
                    break;
                }

                final View child;
                if (idx <= lastIndex) {
                    child = getChildAt(idx - firstIndex);
                } else {
//                        Log.d(getClass().getSimpleName(), "add child to "+idx);
                    child = recycler.getViewForPosition(idx);
                    addView(child);
                    ++lastIndex;
                }
                measureChildWithMargins(child, 0, 0);
                layoutDecorated(child, parentLeft, top, parentRight, top + getDecoratedMeasuredHeight(child));
                setChildTransform(child, y);
            }
        } else if (dy < 0) {
            for (int idx = lastIndex; idx >= 0; --idx) {
                float y = idx * mItemInterval - mScrollY;
                if (y < -mItemInterval) {
                    break;
                }
                y = Math.max(0.0f, y);
                int top = (int) y + parentTop;
                if (top > parentBottom) {
//                        Log.d(getClass().getSimpleName(), "remove child at "+lastIndex);
                    removeAndRecycleViewAt(lastIndex - firstIndex, recycler);
                    --lastIndex;
                    continue;
                }

                final View child;
                if (idx >= firstIndex) {
                    child = getChildAt(idx - firstIndex);
                } else {
//                        Log.d(getClass().getSimpleName(), "add child to "+idx);
                    child = recycler.getViewForPosition(idx);
                    addView(child, 0);
                    --firstIndex;
                }
                measureChildWithMargins(child, 0, 0);
                layoutDecorated(child, parentLeft, top, parentRight, top + getDecoratedMeasuredHeight(child));
                setChildTransform(child, y);
            }
        }

        return dy;
    }

    private void setChildTransform(@NonNull View child, float y) {
        setChildTransform(child, y, false);
    }

    private void setChildTransform(@NonNull final View child, float y, boolean animated) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            child.setPivotX(child.getWidth() * 0.5f);
            child.setPivotY(0.0f);
            child.setRotationX(-30.0f);
//            if (animated) {
//                ValueAnimator anim = ValueAnimator.ofFloat(preScale, scale);
//                anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//                    @Override
//                    public void onAnimationUpdate(ValueAnimator animation) {
//                        child.setScaleX((float) animation.getAnimatedValue());
//                        child.setScaleY((float) animation.getAnimatedValue());
//                    }
//                });
//                anim.setDuration(500);
//                anim.start();
//            } else {
//            }
            child.setRotationX(-30.0f);
        } else {
            ViewHelper.setRotationX(child, -30.0f);
            ViewHelper.setPivotY(child, 0.0f);
        }
    }

}
