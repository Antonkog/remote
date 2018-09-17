package com.wezom.kiviremote.interfaces;

/**
 * Created by andre on 12.06.2017.
 */

public interface OnTouchPadMessageListener <T, S> {
    void sendMotionEvent(T data);
    void buttonClick(S data);
    void sendKey(int keyCode);
}
