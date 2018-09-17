package com.wezom.kiviremote.views;

import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.wezom.kiviremote.common.Action;
import com.wezom.kiviremote.common.extensions.NumUtils;
import com.wezom.kiviremote.interfaces.OnTouchPadMessageListener;
import com.wezom.kiviremote.presentation.home.touchpad.TouchpadButtonClickEvent;
import com.wezom.kiviremote.presentation.home.touchpad.TouchpadMotionModel;

import timber.log.Timber;

/**
 * Created by andre on 09.06.2017.
 */

public class TouchpadView extends android.support.v7.widget.AppCompatImageView implements GestureDetector.OnGestureListener {
    private static final int DOUBLE_FINGER_TAP_TIMEOUT = 75;

    private boolean scroll = false;
    private GestureDetectorCompat gestureDetectorCompat;
    private double speedMultiplier;

    private OnTouchPadMessageListener<TouchpadMotionModel, TouchpadButtonClickEvent> listener;
    private long eventStart;

    private boolean stop;
    private boolean multiFinger = false;

    private float ix, iy;
    private float xDiff, yDiff;

    private float x1, x2, y1, y2, dx, dy;

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

    public void setListener(OnTouchPadMessageListener<TouchpadMotionModel, TouchpadButtonClickEvent> listener) {
        this.listener = listener;
    }

    private void init(Context context) {
        this.gestureDetectorCompat = new GestureDetectorCompat(context, this);

        this.setOnTouchListener((v, event) -> {
            int pointerCount = event.getPointerCount();
            if (pointerCount == 1) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        eventStart = System.currentTimeMillis();
                        stop = false;
                        multiFinger = false;
                        x1 = NumUtils.getToDp(event.getX());
                        y1 = NumUtils.getToDp(event.getY());
                        break;

                    case MotionEvent.ACTION_MOVE:
                        if (!multiFinger) {
                            x2 = NumUtils.getToDp(event.getX());
                            y2 = NumUtils.getToDp(event.getY());
                            dx = x2 - x1;
                            dy = y2 - y1;

                            x1 = x2;
                            y1 = y2;

                            Timber.d("Dx: " + dx);
                            Timber.d("Dy: " + dy);

                            double multiplyBy = 2 + speedMultiplier * 1.5;
                            listener.sendMotionEvent(new TouchpadMotionModel(dx * multiplyBy, dy * multiplyBy));
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        performClick();
                        break;

                    case MotionEvent.ACTION_CANCEL:
                        Timber.d("Action was CANCEL");
                        break;

                    case MotionEvent.ACTION_OUTSIDE:
                        Timber.d("Movement occurred outside bounds of current screen element");
                        break;
                }
            }

            if (pointerCount == 2) {
                multiFinger = true;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        x1 = NumUtils.getToDp(event.getX());
                        y1 = NumUtils.getToDp(event.getY());
                        break;

                    case MotionEvent.ACTION_MOVE:
                        x2 = NumUtils.getToDp(event.getX());
                        y2 = NumUtils.getToDp(event.getY());

                        dx = x2 - x1;
                        dy = y2 - y1;

                        if (!stop) {
                            if (dy < -65) {
                                listener.sendKey(KeyEvent.KEYCODE_DPAD_UP);
                                stop = true;
                            }

                            if (dy > 65) {
                                listener.sendKey(KeyEvent.KEYCODE_DPAD_DOWN);
                                stop = true;
                            }

                            if (dx > 65) {
                                listener.sendKey(KeyEvent.KEYCODE_DPAD_RIGHT);
                                stop = true;
                            }

                            if (dx < -65) {
                                listener.sendKey(KeyEvent.KEYCODE_DPAD_LEFT);
                                stop = true;
                            }
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        performClick();
                        break;

                    case MotionEvent.ACTION_CANCEL:
                        Timber.d("Action was CANCEL");
                        break;

                    case MotionEvent.ACTION_OUTSIDE:
                        Timber.d("Movement occurred outside bounds of current screen element");
                        break;
                }
            }

            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (scroll) {
                    scroll = false;
                    return true;
                }
            }

            // prevent mouse from moving when scrolled with two fingers
            if (scroll)
                return true;

            xDiff = event.getX() - ix;
            yDiff = event.getY() - iy;

            return gestureDetectorCompat.onTouchEvent(event);
        });
    }

    @Override
    public boolean performClick() {
        long eventEnd = System.currentTimeMillis();
        long eventDifference = eventEnd - eventStart;

        if (multiFinger && eventDifference < DOUBLE_FINGER_TAP_TIMEOUT)
            listener.sendKey(KeyEvent.KEYCODE_DPAD_CENTER);
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
        if (Math.abs(xDiff) <= 3 && Math.abs(yDiff) <= 3) {
            listener.buttonClick(new TouchpadButtonClickEvent(xDiff, yDiff, Action.leftClick));
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

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return true;
    }

    public void setSpeedMultiplier(int value) {
        speedMultiplier = (value * 2.0) / 100.0;
    }
}
