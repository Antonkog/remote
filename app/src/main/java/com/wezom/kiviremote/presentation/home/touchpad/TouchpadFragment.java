package com.wezom.kiviremote.presentation.home.touchpad;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.wezom.kiviremote.bus.GotAspectEvent;
import com.wezom.kiviremote.common.Action;
import com.wezom.kiviremote.common.Constants;
import com.wezom.kiviremote.common.PreferencesManager;
import com.wezom.kiviremote.common.extensions.NumUtils;
import com.wezom.kiviremote.databinding.TouchPadFragmentBinding;
import com.wezom.kiviremote.interfaces.OnTouchPadMessageListener;
import com.wezom.kiviremote.presentation.base.BaseViewModelFactory;
import com.wezom.kiviremote.presentation.base.TvKeysFragment;
import com.wezom.kiviremote.presentation.home.tvsettings.AspectHolder;

import javax.inject.Inject;

import timber.log.Timber;


/**
 * Created by andre on 09.06.2017.
 */
public class TouchpadFragment extends TvKeysFragment
        implements OnTouchPadMessageListener<TouchpadMotionModel, TouchpadButtonClickEvent> {

    public static final int POSITION = 1;

    @Inject
    BaseViewModelFactory viewModelFactory;

    private TouchpadViewModel viewModel;

    private TouchPadFragmentBinding binding;

    private int y1;
    private long scrollTime = System.currentTimeMillis();

    private Observer<GotAspectEvent> showAspectObserver = show -> setInputButton(show.hasManufacture());

    private void setInputButton(Boolean show) {
        binding.input.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = TouchPadFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = ViewModelProviders.of(getActivity(), viewModelFactory).get(TouchpadViewModel.class);
        init();
    }

    @Override
    public void injectDependencies() {
        getFragmentComponent().inject(this);
    }

    private void init() {
        binding.touchpad.setListener(this);
        int cursorSpeedMultiplier = PreferencesManager.INSTANCE.getCursorSpeed();

        setInputButton(AspectHolder.INSTANCE.hasManufacture());

        viewModel.getAspectSeen().observe(this, showAspectObserver);
        binding.touchpad.setSpeedMultiplier(cursorSpeedMultiplier);
        setScroll();
        binding.seekbar.setProgress(cursorSpeedMultiplier);
        binding.seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                PreferencesManager.INSTANCE.setCursorSpeed(progress);
                binding.touchpad.setSpeedMultiplier(progress);
            }
        });

        binding.input.setOnClickListener(view -> viewModel.goToInputSettings());
        setTvButtons(viewModel, binding.menu, binding.back, binding.home);
    }

    private void setScroll() {
        binding.scroll.setOnTouchListener((view, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Timber.d("TOUCH_SCROLL_DOWN y : " + event.getY());
                    y1 = NumUtils.getToDp((int) event.getY());
                    break;
                case MotionEvent.ACTION_MOVE:
                    Timber.d("TOUCH_SCROLL_MOVE y : " + event.getY());
                    if (System.currentTimeMillis() - scrollTime > Constants.SCROLL_EVENT_FREQUENCY) {
                        scrollTime = System.currentTimeMillis();
                        int y2 = NumUtils.getToDp((int) event.getY());
                        int dy = y2 - y1;
                        y1 = y2;
                        if (dy != 0) {
                            Timber.d("sendScrollEvent dy : " + dy);
                            viewModel.sendScrollEvent(Action.SCROLL, dy);
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    view.performClick();
                    break;

                case MotionEvent.ACTION_CANCEL:
                    Timber.d("Action was CANCEL");
                    break;
                case MotionEvent.ACTION_OUTSIDE:
                    Timber.d("Movement occurred outside bounds of current screen element");
                    break;
            }
            return true;
        });
    }

    @Override
    public void sendMotionEvent(TouchpadMotionModel data) {
        if (data.getX() != 0 && data.getY() != 0)
            viewModel.sendMotionMessage(data.getX(), data.getY());
    }

    @Override
    public void buttonClick(TouchpadButtonClickEvent data) {
        viewModel.sendClickMessage((int) data.getX(), (int) data.getY(), data.getAction());
    }

    @Override
    public void sendKey(int keyCode) {
        viewModel.sendKeyEvent(keyCode);
    }
}
