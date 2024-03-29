package com.wezom.kiviremote.views;


import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.wezom.kiviremote.App;
import com.wezom.kiviremote.R;

public class NoSwipeViewPager extends ViewPager {

    private boolean enabled = true;

    public NoSwipeViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public NoSwipeViewPager(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        this.setBackground(ResourcesCompat.getDrawable(context.getResources(),
                App.isDarkMode()? R.drawable.shape_gradient_black : R.drawable.shape_gradient_white, null ));
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return this.enabled && super.onInterceptTouchEvent(ev);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        performClick();
        return this.enabled && super.onTouchEvent(ev);
    }

    public void setPagingEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
        return v instanceof TouchpadView || super.canScroll(v, false, dx, x, y);
    }
}
