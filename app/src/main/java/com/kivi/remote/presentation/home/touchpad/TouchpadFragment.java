package com.kivi.remote.presentation.home.touchpad;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.kivi.remote.App;
import com.kivi.remote.R;
import com.kivi.remote.common.Action;
import com.kivi.remote.common.Constants;
import com.kivi.remote.common.extensions.NumUtils;
import com.kivi.remote.databinding.TouchPadFragmentBinding;
import com.kivi.remote.interfaces.OnTouchPadMessageListener;
import com.kivi.remote.presentation.base.BaseViewModelFactory;
import com.kivi.remote.presentation.base.TvKeysFragment;
import com.kivi.remote.presentation.home.HomeActivity;

import java.util.Locale;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import timber.log.Timber;


/**
 * Created by andre on 09.06.2017.
 */
public class TouchpadFragment extends TvKeysFragment
        implements OnTouchPadMessageListener<TouchpadMotionModel, TouchpadButtonClickEvent> {

    @Inject
    BaseViewModelFactory viewModelFactory;

    private TouchpadViewModel viewModel;

    private TouchPadFragmentBinding binding;

    private int y1;
    private long scrollTime = System.currentTimeMillis();
    private Intent mSpeechRecognizerIntent = null;

    private final int REQUEST_PERMISSION_CODE = 12123;

    private boolean isScrollMode = true;

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
        binding.powerOff.setOnClickListener(view -> viewModel.switchOff());
        binding.microphone.setOnClickListener(view -> {
            if (((HomeActivity) getActivity()).hasRecordAudioPermission()) {
                startListenIntent();
            } else {
                String[] s = {Manifest.permission.READ_EXTERNAL_STORAGE};
                requestPermissions(s, REQUEST_PERMISSION_CODE);
            }
        });


        viewModel.setSpeachRecognizer(SpeechRecognizer.createSpeechRecognizer(getContext()));

        binding.microphone.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_UP:
                        viewModel.speechListener.stopListening();
                        break;

                    case MotionEvent.ACTION_DOWN:
                        if (mSpeechRecognizerIntent != null)
                            viewModel.speechListener.startListening(mSpeechRecognizerIntent);
                        Toast.makeText(getContext(), "listening ", Toast.LENGTH_SHORT).show();
                        break;
                }
                return false;
            }
        });


        binding.touchpad.setSpeedMultiplier(50); // can modify but no place in ui

        binding.microphone.setImageResource(App.isDarkMode() ? R.drawable.ic_microphone_dm : R.drawable.ic_microphone);
        binding.powerOff.setImageResource(App.isDarkMode() ? R.drawable.ic_power_dm : R.drawable.ic_power);

        binding.touchBody.setBackgroundColor(binding.getRoot().getResources().getColor(App.isDarkMode()? R.color.touch_body :R.color.colorWhite));
        binding.topContainer.setBackgroundColor(binding.getRoot().getResources().getColor(App.isDarkMode()? R.color.touch_header_dm :R.color.white_87));

        binding.imgActionMode.setOnClickListener(v -> {
            this.isScrollMode = !isScrollMode;
            setScrollBtn();
        });
        setScrollBtn();
        setScroll();
        setTvButtons(viewModel, binding.aspectMenu, binding.back, binding.home);
    }

    private void setScrollBtn() {
        binding.touchpad.setScrollMode(isScrollMode);
        if (isScrollMode) {
            binding.imgActionMode.setImageResource(App.isDarkMode() ? R.drawable.ic_swipe_dm : R.drawable.ic_swipe);
        } else {
            binding.imgActionMode.setImageResource(App.isDarkMode() ? R.drawable.ic_cursor_dm : R.drawable.ic_cursor);
        }
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startListenIntent();
            } else {
                Toast.makeText(getContext(), " has no record audio permission", Toast.LENGTH_LONG).show();
            }
        }

    }

    private void startListenIntent() {
        mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                Locale.getDefault());


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
    public void longClick(TouchpadButtonClickEvent data) {
        viewModel.sendClickMessage((int) data.getX(), (int) data.getY(), data.getAction());
    }

    @Override
    public void sendKey(int keyCode) {
        viewModel.sendKeyEvent(keyCode);
    }
}
