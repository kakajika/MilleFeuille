package com.labo.kaji.millefeuille;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.View;

import com.nineoldandroids.view.ViewHelper;

/**
 * Created by kakajika on 2015/04/18.
 */
public class ShadowCardView extends CardView implements ArcLayoutManager.ShadowDispatcher {

    private View mShadowOverlay;

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
        mShadowOverlay = new View(context);
        mShadowOverlay.setBackgroundColor(Color.BLACK);
        addView(mShadowOverlay);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mShadowOverlay.bringToFront();
    }

    @Override
    public void setShadowLevel(float shadowLevel) {
//        mShadowOverlay.bringToFront();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mShadowOverlay.setAlpha(shadowLevel);
        } else {
            ViewHelper.setAlpha(mShadowOverlay, shadowLevel);
        }
    }

    @Override
    public float getShadowLevel() {
        return ViewCompat.getAlpha(mShadowOverlay);
    }
}
