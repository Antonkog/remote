package com.wezom.kiviremote.presentation.home.main;


public interface BackHandler {
    BackHandler addBackListener(OnBackClickListener listener);

    void removeBackListener(OnBackClickListener listener);

    interface OnBackClickListener {
        boolean onBackClick();
    }
}
