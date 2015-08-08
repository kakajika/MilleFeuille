package com.labo.kaji.millefeuille;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;

/**
 * @author kakajika
 * @since 2015/04/18.
 */
public class ShadowCardView extends CardView implements ArcLayoutManager.ShadowDispatcher {

    private final Drawable mShadowDrawable = new ColorDrawable(Color.BLACK);

    public ShadowCardView(Context context) {
        super(context);
        init(context);
    }

    public ShadowCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ShadowCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setShadowLevel(0.0f);
//        setForeground(mShadowDrawable);
        mShadowDrawable.setBounds(getPaddingLeft(), getPaddingTop(), getWidth()-getPaddingRight(), getHeight()-getPaddingBottom());
    }

    @Override
    public void setCardElevation(float radius) {
        super.setCardElevation(radius);
    }

    @Override
    public void setShadowLevel(float shadowLevel) {
        mShadowDrawable.setAlpha((int) (shadowLevel * 255));
    }

    @Override
    public float getShadowLevel() {
        return mShadowDrawable.getAlpha() / 255f;
    }

}
