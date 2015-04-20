package com.labo.kaji.millefeuille;

import android.os.Build;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.labo.kaji.millefeuille.R;
import com.nineoldandroids.view.ViewHelper;

/**
 * Created by kakajika on 2015/04/18.
 */
public class SlideStackLayoutManager extends RecyclerView.LayoutManager {

    private static final float MIN_HORIZONTAL_SCALE = 0.7f;
    private static final float SCROLL_FRICTION_SCALE = 0.02f;
    private static final float ITEM_Z_INTERVAL = 12.0f;

    private float mMaxZ = 1.0f;

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
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        super.onLayoutChildren(recycler, state);
        detachAndScrapAttachedViews(recycler);

        final int itemCount = getItemCount();
        final int parentTop = getPaddingTop();
        final int parentBottom = getHeight() - getPaddingBottom();
        final int parentHeight = parentBottom - parentTop;
        final int parentLeft = getPaddingLeft();
        final int parentRight = getWidth() - getPaddingRight();
        mMaxZ = (float) Math.sqrt(parentHeight);

        for (int idx = 0; idx < itemCount; ++idx) {
            float z = idx * ITEM_Z_INTERVAL;
            int top = (int) (z * z) + parentTop;
            if (top > parentBottom) {
                break;
            }
            View child = recycler.getViewForPosition(idx);
            addView(child);
            measureChildWithMargins(child, 0, 0);
            int bottom = top + getDecoratedMeasuredHeight(child);
            layoutDecorated(child, parentLeft, top, parentRight, bottom);
            setChildTransform(child, z);
        }

        state.put(R.id.first_item_index, 0);
        state.put(R.id.last_item_index, getChildCount() - 1);
        state.put(R.id.arc_z_position, 0.0f);
    }

    @Override
    public int scrollVerticallyBy(int dz, RecyclerView.Recycler recycler, RecyclerView.State state) {
        final int parentTop = getPaddingTop();
        final int parentBottom = getHeight() - getPaddingBottom();
        final int parentLeft = getPaddingLeft();
        final int parentRight = getWidth() - getPaddingRight();
        final int itemCount = getItemCount();

        final float baseZ = (float) state.get(R.id.arc_z_position) - dz * SCROLL_FRICTION_SCALE;
        int firstIndex = state.get(R.id.first_item_index);
        int lastIndex = state.get(R.id.last_item_index);

        if (dz > 0) {
            for (int idx = firstIndex; idx < itemCount; ++idx) {
                float z = baseZ + idx * ITEM_Z_INTERVAL;
                if (z < -ITEM_Z_INTERVAL) {
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
                float z = baseZ + idx * ITEM_Z_INTERVAL;
                if (z < -ITEM_Z_INTERVAL) {
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

        state.put(R.id.arc_z_position, baseZ);
        state.put(R.id.first_item_index, firstIndex);
        state.put(R.id.last_item_index, lastIndex);
        return dz;
    }

    private void setChildTransform(View child, float z) {
        final float scale = z / mMaxZ * (1.0f - MIN_HORIZONTAL_SCALE) + MIN_HORIZONTAL_SCALE;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//            child.setScaleX(scale);
//            child.setScaleY(scale);
//            child.setPivotY(0.0f);
//        } else {
//            ViewHelper.setScaleX(child, 0.0f);
//            ViewHelper.setScaleY(child, 0.0f);
//            ViewHelper.setPivotY(child, 0.0f);
//        }
        if (child instanceof CardView) {
//                ((CardView)child).setMaxCardElevation(mMaxZ);
            ((CardView) child).setCardElevation(z);
        }
    }

}
