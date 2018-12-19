package com.wezom.kiviremote.presentation.home.touchpad;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.wezom.kiviremote.common.PreferencesManager;
import com.wezom.kiviremote.databinding.TouchPadFragmentBinding;
import com.wezom.kiviremote.interfaces.OnTouchPadMessageListener;
import com.wezom.kiviremote.presentation.base.BaseFragment;
import com.wezom.kiviremote.presentation.base.BaseViewModelFactory;

import javax.inject.Inject;

import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_MOVE;
import static android.view.MotionEvent.ACTION_UP;
import static com.wezom.kiviremote.common.Constants.HOME_KEY_DELAY;


/**
 * Created by andre on 09.06.2017.
 */
public class TouchpadFragment extends BaseFragment
        implements OnTouchPadMessageListener<TouchpadMotionModel, TouchpadButtonClickEvent> {

    public static final int POSITION = 1;

    @Inject
    BaseViewModelFactory viewModelFactory;

    private TouchpadViewModel viewModel;

    private TouchPadFragmentBinding binding;

    private GestureDetectorCompat gestureDetector;
    private final int SWIPE_MIN_DISTANCE = 120;
    private final int SWIPE_THRESHOLD_VELOCITY = 100;
    private long  homeClickTime;
    private final Handler handler = new Handler();
    private final Runnable launchQuickApps = () -> viewModel.launchQuickApps();

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
        binding.touchpad.setSpeedMultiplier(cursorSpeedMultiplier);
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

        binding.back.setOnClickListener(v -> viewModel.sendKeyEvent(KeyEvent.KEYCODE_BACK));
        binding.menu.setOnClickListener(v -> viewModel.goToSettings());
        binding.home.setOnTouchListener((view, event) -> {
            switch (event.getAction()) {
                case ACTION_DOWN:
                    view.setPressed(true);
                    viewModel.sendHomeDown();
                    homeClickTime = System.currentTimeMillis();
                    handler.postDelayed(launchQuickApps, 340);
                    break;

                case ACTION_MOVE:
                    view.setPressed(true);
                    break;

                case ACTION_UP:
                    view.performClick();
                    view.setPressed(false);
                    if (System.currentTimeMillis() - homeClickTime < HOME_KEY_DELAY) {
                        viewModel.sendHomeUp();
                        handler.removeCallbacks(launchQuickApps);
                    }
                    break;

                default:
                    view.setPressed(false);
                    break;
            }

            return true;
        });

        gestureDetector = new GestureDetectorCompat(getContext(), new MyGestureListener());
        binding.scroll.setOnTouchListener((view, event) -> {
            gestureDetector.onTouchEvent(event);
            return true;
        });
    }


    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent event) {
            return true;
        }

        @Override
        public void onLongPress(MotionEvent event) {
            if(event.getY() < binding.scroll.getY() + binding.scroll.getHeight()/2){
                viewModel.sendScrollEvent(true,0);
            } else {
                viewModel.sendScrollEvent(false, 0);
            }
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2,
                               float velocityX, float velocityY) {
            if (e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                viewModel.sendScrollEvent(true, Math.abs(e1.getY() - e2.getY()));
                return false; // Bottom to top
            } else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                viewModel.sendScrollEvent(false, Math.abs(e1.getY() - e2.getY()));
                return false; // Top to bottom
            }
            return true;
        }
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
