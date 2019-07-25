package com.wezom.kiviremote.presentation.home.touchpad;

import android.Manifest;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.wezom.kiviremote.common.Action;
import com.wezom.kiviremote.common.Constants;
import com.wezom.kiviremote.common.PreferencesManager;
import com.wezom.kiviremote.common.extensions.NumUtils;
import com.wezom.kiviremote.databinding.TouchPadFragmentBinding;
import com.wezom.kiviremote.interfaces.OnTouchPadMessageListener;
import com.wezom.kiviremote.presentation.base.BaseViewModelFactory;
import com.wezom.kiviremote.presentation.base.TvKeysFragment;
import com.wezom.kiviremote.presentation.home.HomeActivity;

import java.util.Locale;

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
    private Intent mSpeechRecognizerIntent = null;

    private final int REQUEST_PERMISSION_CODE = 12123;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = TouchPadFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        ((HomeActivity) getActivity()).changeToolbarVisibility(View.VISIBLE);
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        ((HomeActivity) getActivity()).changeToolbarVisibility(View.GONE);
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
                String s[] = {Manifest.permission.READ_EXTERNAL_STORAGE};
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

        int cursorSpeedMultiplier = PreferencesManager.INSTANCE.getCursorSpeed();

        binding.touchpad.setSpeedMultiplier(cursorSpeedMultiplier);

        setScroll();
        setTvButtons(viewModel, binding.aspectMenu, binding.back, binding.home);
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
    public void sendKey(int keyCode) {
        viewModel.sendKeyEvent(keyCode);
    }
}
