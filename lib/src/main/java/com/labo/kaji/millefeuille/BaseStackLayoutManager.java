package com.labo.kaji.millefeuille;

import android.animation.Animator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author kakajika
 * @since 15/08/09.
 */
abstract class BaseStackLayoutManager extends RecyclerView.LayoutManager {

    protected final Map<View, Animator> mAnimatorMap = new HashMap<>();

    public BaseStackLayoutManager(Context context) {
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
    public void removeAndRecycleViewAt(int index, RecyclerView.Recycler recycler) {
        // Clear child's transformation.
        View child = getChildAt(index);
        child.setRotation(0);
        child.setRotationX(0);
        child.setRotationY(0);
        child.setScaleX(1);
        child.setScaleY(1);
        child.setTranslationX(0);
        child.setTranslationY(0);
        if (mAnimatorMap.containsKey(child)) {
            mAnimatorMap.remove(child).cancel();
        }
        super.removeAndRecycleViewAt(index, recycler);
    }

    @Override
    public void removeAndRecycleAllViews(RecyclerView.Recycler recycler) {
        super.removeAndRecycleAllViews(recycler);
    }

    @Override
    public void onItemsRemoved(RecyclerView recyclerView, int positionStart, int itemCount) {
        log("onItemsRemoved: position:", positionStart);
    }

    @Override
    public void onItemsMoved(RecyclerView recyclerView, int from, int to, int itemCount) {
        log("onItemsMoved: from:", from, " to:", to);
    }

    @Override
    public boolean supportsPredictiveItemAnimations() {
        return true;
    }

    protected void setChildTransform(@NonNull View child, float z) {
        setChildTransform(child, z, false);
    }

    protected abstract void setChildTransform(@NonNull final View child, float z, boolean animated);

    protected void clearChildAnimations() {
        Iterator<Map.Entry<View, Animator>> iterator = mAnimatorMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<View, Animator> entry = iterator.next();
            entry.getValue().cancel();
            iterator.remove();
        }
    }

    protected void log(@NonNull String message, Object... args) {
        StringBuilder string = new StringBuilder(message);
        for (Object arg : args) string.append(arg.toString());
        Log.i(getClass().getSimpleName(), string.toString());
    }

}
