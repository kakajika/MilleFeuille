package com.labo.kaji.millefeuille;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * @author kakajika
 * @since 2015/04/18
 */
public class SlideStackLayoutManager extends RecyclerView.LayoutManager {

    private static final float MIN_HORIZONTAL_SCALE = 0.7f;
    private static final float SCROLL_FRICTION_SCALE = 0.02f;
    private static final float ITEM_Z_INTERVAL = 12.0f;

    private float mItemInterval = 12.0f;
    private float mMaxZ = 1.0f;
    private float mScrollZ = 0.0f;
    private int mScrollState;

    public SlideStackLayoutManager() {
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
        mMaxZ = (float) Math.sqrt(parentHeight);

        for (int idx = 0; idx < itemCount; ++idx) {
            float z = idx * mItemInterval - mScrollZ;
            if (z < -mItemInterval) {
                continue;
            }
            z = Math.max(0.0f, z);
            int top = (int) (z * z) + parentTop;
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
//                        Log.d(getClass().getSimpleName(), "remove child at "+firstIndex);
                    removeAndRecycleViewAt(0, recycler);
                    ++firstIndex;
                    continue;
                }
                z = Math.max(0.0f, z);
                int top = (int) (z * z) + parentTop;
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
                float z = idx * mItemInterval - mScrollZ;
                if (z < -mItemInterval) {
                    break;
                }
                z = Math.max(0.0f, z);
                int top = (int) (z * z) + parentTop;
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

        return dy;
    }

    private void setChildTransform(@NonNull View child, float z) {
        setChildTransform(child, z, false);
    }

    private void setChildTransform(@NonNull final View child, float z, boolean animated) {
    }

}
