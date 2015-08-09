package com.labo.kaji.millefeuille.sample;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;

import com.labo.kaji.millefeuille.ShadeApplicator;

/**
 * @author kakajika
 * @since 2015/04/18.
 */
public class ShadeCardView extends CardView implements ShadeApplicator {

    private final Drawable mShadowDrawable = new ColorDrawable(Color.BLACK);

    public ShadeCardView(Context context) {
        super(context);
        init(context);
    }

    public ShadeCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ShadeCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setShadeLevel(0.0f);
        setMaxCardElevation(getResources().getDimensionPixelSize(R.dimen.cardview_elevation));
        setCardElevation(getResources().getDimensionPixelSize(R.dimen.cardview_elevation));
        setUseCompatPadding(true);
    }

    @Override
    public void setCardElevation(float radius) {
        super.setCardElevation(radius);
    }

    @Override
    public void setShadeLevel(float shadowLevel) {
//        mShadowDrawable.setAlpha((int) (shadowLevel * 255));
//        setCardElevation(getMaxCardElevation() * shadowLevel);
    }

    @Override
    public float getShadeLevel() {
        return getCardElevation() / getMaxCardElevation();
//        return mShadowDrawable.getAlpha() / 255f;
    }

}
