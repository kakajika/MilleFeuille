package com.labo.kaji.millefeuille;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.nineoldandroids.view.ViewHelper;

/**
 * Created by kakajika on 2015/01/22.
 */
public class CardStackView extends RecyclerView {
    
    public CardStackView(Context context) {
        super(context);
        init(context);
    }

    public CardStackView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CardStackView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

//    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//    public CardStackView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
//        super(context, attrs, defStyleAttr, defStyleRes);
//        init(context);
//    }

    private void init(Context context) {
        setAdapter(new CardAdapter());
        setLayoutManager(new CardLayoutManager(context));
    }

    class CardAdapter extends RecyclerView.Adapter {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(View.inflate(getContext(), R.layout.card, null)) {
            };
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
        }

        @Override
        public int getItemCount() {
            return 100;
        }

    }

    class CardLayoutManager extends LinearLayoutManager {

        private static final float MIN_HORIZONTAL_SCALE = 0.7f;
        private static final float SCROLL_FRICTION_SCALE = 0.02f;
        private static final float ITEM_Z_INTERVAL = 10.0f;

        private float mMaxZ = 1.0f;

        public CardLayoutManager(Context context) {
            super(context);
            setOrientation(LinearLayoutManager.VERTICAL);
        }

        public CardLayoutManager(Context context, int orientation, boolean reverseLayout) {
            super(context, orientation, reverseLayout);
        }

        @Override
        public void onLayoutChildren(Recycler recycler, State state) {
            super.onLayoutChildren(recycler, state);
//            detachAndScrapAttachedViews(recycler);

            final int itemCount = getItemCount();
            final int parentTop = getPaddingTop();
            final int parentBottom = getHeight()-getPaddingBottom();
            final int parentHeight = getHeight()-getPaddingTop()-getPaddingBottom();
            final int parentLeft = getPaddingLeft();
            final int parentRight = getWidth() - getPaddingRight();

            int top = 0, bottom = 0;
            for (int i=0, z=0; i<itemCount && top<parentBottom; ++i, z += ITEM_Z_INTERVAL) {
                View child = recycler.getViewForPosition(i);
                addView(child);
                measureChildWithMargins(child, 0, 0);
                top = z*z + parentTop;
                bottom = top + getDecoratedMeasuredHeight(child);
                layoutDecorated(child, parentLeft, top, parentRight, bottom);
                setChildTransform(child, z);
            }
            state.put(1, 0.0f);
            mMaxZ = (float)Math.sqrt(getHeight());
        }

        @Override
        @TargetApi(11)
        public int scrollVerticallyBy(int dz, Recycler recycler, State state) {
            final float baseZ = (Float)state.get(1) - dz*SCROLL_FRICTION_SCALE;
            final int baseIndex = findFirstVisibleItemPosition();
            final int lastIndex = findLastVisibleItemPosition();
            final int parentTop = getPaddingTop();
            final int parentBottom = getHeight()-getPaddingBottom();
            final int itemCount = getItemCount();

            final int childCount = getChildCount();
            if (dz > 0) {
                for (int i=baseIndex, j=0; i<itemCount; ++i) {
                    float z = baseZ + i*ITEM_Z_INTERVAL;
                    if (z < -ITEM_Z_INTERVAL) {
                        removeAndRecycleViewAt(0, recycler);
                        continue;
                    }
                    z = Math.max(0.0f, z);
                    int top = (int)(z*z) + parentTop;
                    if (top > parentBottom) {
                        break;
                    }

                    final View child;
                    if (i < baseIndex+childCount) {
                        child = getChildAt(j);
                        ++j;
                    } else {
                        child = recycler.getViewForPosition(i);
                        addView(child);
                    }
                    layoutDecorated(child, getDecoratedLeft(child), top, getDecoratedRight(child), top+getDecoratedMeasuredHeight(child));
                    setChildTransform(child, z);
                }
            } else if (dz < 0) {
                for (int i=lastIndex, j=childCount-1; i>=0; --i) {
                    float z = baseZ + i*ITEM_Z_INTERVAL;
                    if (z < -ITEM_Z_INTERVAL) {
                        break;
                    }
                    z = Math.max(0.0f, z);
                    int top = (int)(z*z) + parentTop;
                    if (top > parentBottom) {
                        removeAndRecycleViewAt(i-baseIndex, recycler);
                        --j;
                        continue;
                    }

                    final View child;
                    if (i >= baseIndex) {
                        child = getChildAt(j);
                        --j;
                    } else {
                        child = recycler.getViewForPosition(i);
                        addView(child, 0);
                    }
                    layoutDecorated(child, getDecoratedLeft(child), top, getDecoratedRight(child), top+getDecoratedMeasuredHeight(child));
                    setChildTransform(child, z);
                }
            }

            state.put(1, baseZ);
            return dz;
        }

        private void setChildTransform(View child, float z) {
            final float scale = z / mMaxZ * (1.0f-MIN_HORIZONTAL_SCALE) + MIN_HORIZONTAL_SCALE;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                child.setScaleX(scale);
                child.setScaleY(scale);
                child.setPivotY(0.0f);
            } else {
                ViewHelper.setScaleX(child, 0.0f);
                ViewHelper.setScaleY(child, 0.0f);
                ViewHelper.setPivotY(child, 0.0f);
            }
        }

    }
    
}
