package com.labo.kaji.millefeuille;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

/**
 * Created by kakajika on 2015/04/18.
 */
public class ArcLayoutManager extends RecyclerView.LayoutManager {

    public interface ShadowDispatcher {
        void setShadowLevel(float shadowLevel);
        float getShadowLevel();
    }

    private static final float MIN_HORIZONTAL_SCALE = 0.7f;
    private static final float SCROLL_FRICTION_SCALE = 0.02f;

    private float mMaxZ = 1.0f;
    private float mArcCurvature = 0.5f;
    private float mItemInterval = 16.0f;

    public ArcLayoutManager() {
        super();
    }

    public void setArcCurvature(float arcCurvature) {
        mArcCurvature = arcCurvature;
    }

    public void setItemInterval(float itemInterval) {
        mItemInterval = itemInterval;
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
        return (int)mItemInterval;
    }

    @Override
    public int computeVerticalScrollOffset(RecyclerView.State state) {
        return (int)(-(float)state.get(R.id.arc_z_position));
    }

    @Override
    public int computeVerticalScrollRange(RecyclerView.State state) {
        return (int)(getItemCount() * mItemInterval);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        log("onLayoutChildren: " + state.toString());
        if (!state.didStructureChange()) {
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

        final float baseZ = (state.get(R.id.arc_z_position) != null) ? (float)state.get(R.id.arc_z_position) : 0.0f;
        int firstIndex = (state.get(R.id.first_item_index) != null) ? (int)state.get(R.id.first_item_index) : 0;

        for (int idx = firstIndex; idx < itemCount; ++idx) {
            float z = baseZ + idx * mItemInterval;
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
            setChildTransform(child, z, !state.isPreLayout() && state.willRunPredictiveAnimations());
        }

        state.put(R.id.first_item_index, firstIndex);
        state.put(R.id.last_item_index, firstIndex + getChildCount() - 1);
        state.put(R.id.arc_z_position, baseZ);
    }

    @Override
    public int scrollVerticallyBy(int dz, RecyclerView.Recycler recycler, RecyclerView.State state) {
        super.scrollVerticallyBy(dz, recycler, state);

        final int parentTop = getPaddingTop();
        final int parentBottom = getHeight() - getPaddingBottom();
        final int parentLeft = getPaddingLeft();
        final int parentRight = getWidth() - getPaddingRight();
        final int itemCount = getItemCount();

        float baseZ = (float) state.get(R.id.arc_z_position) - dz * SCROLL_FRICTION_SCALE;
        baseZ = Math.min(0.0f, Math.max(baseZ, -computeVerticalScrollRange(state)));
        int firstIndex = state.get(R.id.first_item_index);
        int lastIndex = state.get(R.id.last_item_index);

        if (dz > 0) {
            for (int idx = firstIndex; idx < itemCount; ++idx) {
                float z = baseZ + idx * mItemInterval;
                if (z < -mItemInterval) {
//                        Log.d(getClass().getSimpleName(), "remove child at "+firstIndex);
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
//                        Log.d(getClass().getSimpleName(), "add child to "+idx);
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
                float z = baseZ + idx * mItemInterval;
                if (z < -mItemInterval) {
                    break;
                }
                z = Math.max(0.0f, z);
                int top = (int) (z * z * mArcCurvature) + parentTop;
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
                setChildTransform(child, z);
            }
        }

        state.put(R.id.arc_z_position, baseZ);
        state.put(R.id.first_item_index, firstIndex);
        state.put(R.id.last_item_index, lastIndex);
        return dz;
    }

    @Override
    public void scrollToPosition(int position) {
        requestLayout();
    }

    @Override
    public void onItemsRemoved(RecyclerView recyclerView, int positionStart, int itemCount) {
        log("onItemsRemoved: position:" + positionStart);
        for (int i=0; i<getChildCount(); ++i) {
            View child = getChildAt(i);
            int position = getPosition(child);
            if (positionStart <= position && position < positionStart+itemCount) {
//                ViewCompat.setAlpha(child, 0.5f);
            }
        }
    }

    @Override
    public void onItemsMoved(RecyclerView recyclerView, int from, int to, int itemCount) {
        log("onItemsMoved: from:"+from+" to:"+to);
    }

    @Override
    public boolean supportsPredictiveItemAnimations() {
        return true;
    }

    private void setChildTransform(@NonNull View child, float z) {
        setChildTransform(child, z, false);
    }

    private void setChildTransform(@NonNull final View child, float z, boolean animated) {
        final float zScale = z / mMaxZ;
        final float preScale = ViewCompat.getScaleX(child);
        final float scale = zScale * (1.0f - MIN_HORIZONTAL_SCALE) + MIN_HORIZONTAL_SCALE;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            child.setPivotX(child.getWidth() * 0.5f);
            child.setPivotY(0.0f);
            child.setTranslationX(0);
            child.setAlpha(1.0f);
            if (animated) {
                ValueAnimator anim = ValueAnimator.ofFloat(preScale, scale);
                anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        child.setScaleX((float) animation.getAnimatedValue());
                        child.setScaleY((float) animation.getAnimatedValue());
                    }
                });
                anim.setDuration(500);
                anim.start();
            } else {
                child.setScaleX(scale);
                child.setScaleY(scale);
            }
        } else {
            ViewHelper.setScaleX(child, 0.0f);
            ViewHelper.setScaleY(child, 0.0f);
            ViewHelper.setPivotY(child, 0.0f);
        }
        if (child instanceof CardView) {
//                ((CardView)child).setMaxCardElevation(mMaxZ);
            ((CardView) child).setCardElevation(z);
        }
        if (child instanceof ShadowDispatcher) {
            final float shadowLevel = Math.max(0.0f, 1.0f - zScale - MIN_HORIZONTAL_SCALE * 0.5f);
            if (animated) {
                final float preShadowLevel = ((ShadowDispatcher) child).getShadowLevel();
                ValueAnimator anim = ValueAnimator.ofFloat(preShadowLevel, shadowLevel);
                anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        ((ShadowDispatcher) child).setShadowLevel((float) animation.getAnimatedValue());
                    }
                });
                anim.setDuration(500);
                anim.start();
            } else {
                ((ShadowDispatcher) child).setShadowLevel(shadowLevel);
            }
        }
    }

    private void log(String message) {
        Log.i(getClass().getSimpleName(), message);
    }

}
