package com.kivi.remote.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.kivi.remote.common.Action;
import com.kivi.remote.common.extensions.NumUtils;
import com.kivi.remote.interfaces.OnTouchPadMessageListener;
import com.kivi.remote.presentation.home.touchpad.TouchpadButtonClickEvent;
import com.kivi.remote.presentation.home.touchpad.TouchpadMotionModel;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.view.GestureDetectorCompat;
import timber.log.Timber;

/**
 * Created by andre on 09.06.2017.
 */

public class TouchpadView extends AppCompatImageView implements GestureDetector.OnGestureListener {
    private static final int TAP_TIMEOUT = 100;

    private boolean scroll = false;
    private boolean longTapStarted = false;
    private GestureDetectorCompat gestureDetectorCompat;
    private double speedMultiplier;

    private OnTouchPadMessageListener<TouchpadMotionModel, TouchpadButtonClickEvent> messageListener;
    private long eventStart;

    private boolean stop;

    private float ix, iy;
    private float xDiff, yDiff;

    private float x1, x2, y1, y2, dx, dy;

    private int centerClickArea = 65; //(getWidth() == 0) ? NumUtils.getToPx(30) : getWidth() / 4;

    public void setScrollMode(boolean scroll) {
        this.scroll = scroll;
    }

    public TouchpadView(Context context) {
        super(context);
        init(context);
    }

    public TouchpadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TouchpadView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void setMessageListener(OnTouchPadMessageListener<TouchpadMotionModel, TouchpadButtonClickEvent> messageListener) {
        this.messageListener = messageListener;
    }

    Path path = new Path();
    Paint paint = new Paint();

    @Override
    public void onDraw(Canvas canvas) {
        int w = getWidth();
        int h = getHeight();
//        paint.setColor(ResourcesCompat.getColor(getResources(), R.color.colorWhite, null));
        paint.setARGB(151, 151, 151, 151);
        paint.setStyle(Paint.Style.STROKE);
        paint.setPathEffect(new DashPathEffect(new float[]{20, 20}, 0));

        int marging = getWidth() / 4;
        int xlength = getWidth() - marging - marging;
//        int scale = (h / w) < 1 ? (w/h) : h/w;
        int vertMarging = (getHeight() - xlength) / 2;

        path.moveTo(w / 2, h / 2);
        path.lineTo(w / 2, vertMarging);

        path.moveTo(w / 2, h / 2);
        path.lineTo(w / 2, h - vertMarging);

        path.moveTo(w / 2, h / 2);
        path.lineTo(marging, h / 2);

        path.moveTo(w / 2, h / 2);
        path.lineTo(w - marging, h / 2);

        canvas.drawPath(path, paint);
    }

    private void init(Context context) {

        this.gestureDetectorCompat = new GestureDetectorCompat(context, this);
        this.setOnTouchListener((v, event) -> {
//            int pointerCount = event.getPointerCount();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    eventStart = System.currentTimeMillis();
                    stop = false;
                    x1 = NumUtils.getToDp(event.getX());
                    y1 = NumUtils.getToDp(event.getY());
                    break;
                case MotionEvent.ACTION_MOVE:
                    x2 = NumUtils.getToDp(event.getX());
                    y2 = NumUtils.getToDp(event.getY());

                    dx = x2 - x1;
                    dy = y2 - y1;

                    if (!scroll) {
                        x1 = x2;
                        y1 = y2;

                        Timber.d("Dx: " + dx);
                        Timber.d("Dy: " + dy);

                        double multiplyBy = 2 + speedMultiplier * 1.5;
                        messageListener.sendMotionEvent(new TouchpadMotionModel(dx * multiplyBy, dy * multiplyBy));
                    } else {
                        if (!stop && !longTapStarted) {
                            if (dy < -centerClickArea) {
                                messageListener.sendKey(KeyEvent.KEYCODE_DPAD_UP);
                                stop = true;
                            }

                            if (dy > centerClickArea) {
                                messageListener.sendKey(KeyEvent.KEYCODE_DPAD_DOWN);
                                stop = true;
                            }

                            if (dx > centerClickArea) {
                                messageListener.sendKey(KeyEvent.KEYCODE_DPAD_RIGHT);
                                stop = true;
                            }

                            if (dx < -centerClickArea) {
                                messageListener.sendKey(KeyEvent.KEYCODE_DPAD_LEFT);
                                stop = true;
                            }
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (longTapStarted) {
                        longTapStarted = false;
                        messageListener.longClick(new TouchpadButtonClickEvent(x2, y2, Action.LONG_TAP_UP));
                    } else if (scroll && centerClickArea > Math.sqrt(Math.pow(dx, 2) * Math.pow(dy, 2)))
                        performClick();
                    break;
            }

//            if (!scroll) {
                xDiff = event.getX() - ix;
                yDiff = event.getY() - iy;
                return gestureDetectorCompat.onTouchEvent(event);
//            } else {
//                return true;
//            }
        });
    }


    @Override
    public boolean performClick() {
        long eventEnd = System.currentTimeMillis();
        long eventDifference = eventEnd - eventStart;

        if (eventDifference < TAP_TIMEOUT)
            messageListener.sendKey(KeyEvent.KEYCODE_DPAD_CENTER);
        return super.performClick();
    }

    @Override
    public boolean onDown(MotionEvent event) {
        ix = event.getX();
        iy = event.getY();
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        if (!scroll && Math.abs(xDiff) <= 3 && Math.abs(yDiff) <= 3) {
            messageListener.buttonClick(new TouchpadButtonClickEvent(xDiff, yDiff, Action.LEFT_CLICK));
            return true;
        }
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        Timber.e("onLongPress");
        longTapStarted = true;
        messageListener.longClick(new TouchpadButtonClickEvent(x1, y1, Action.LONG_TAP_DOWN));
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return true;
    }

    public void setSpeedMultiplier(int value) {
        speedMultiplier = (value * 2.0) / 100.0;
    }
}
