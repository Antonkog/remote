package com.wezom.kiviremote.presentation.home.remotecontrol;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.wezom.kiviremote.App;
import com.wezom.kiviremote.R;
import com.wezom.kiviremote.bus.GotAspectEvent;
import com.wezom.kiviremote.common.PreferencesManager;
import com.wezom.kiviremote.databinding.RemoteControlFragmentBinding;
import com.wezom.kiviremote.interfaces.RockersButtonClickListener;
import com.wezom.kiviremote.presentation.base.BaseViewModelFactory;
import com.wezom.kiviremote.presentation.base.TvKeysFragment;
import com.wezom.kiviremote.presentation.home.tvsettings.AspectHolder;
import com.wezom.kiviremote.views.KiviDPadView;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

import static android.view.MotionEvent.ACTION_DOWN;
import static com.wezom.kiviremote.common.Constants.DPAD_EVENT_FREQUENCY;
import static com.wezom.kiviremote.common.Constants.INITIAL_DELAY;

/**
 * Created by andre on 29.05.2017.
 */

public class RemoteControlFragment extends TvKeysFragment implements RockersButtonClickListener<Integer> {
    public static final int POSITION = 0;

    @Inject
    BaseViewModelFactory viewModelFactory;

    RemoteControlViewModel viewModel;

    RemoteControlFragmentBinding binding;

    private boolean isMute = false;

    private Disposable continuousClicks;

    private final Observer<Boolean> muteObserver = status -> {
        if (status != null) {
            if (status)
                setMute(true);
            else
                setMute(false);
        }
    };

    private Observer<GotAspectEvent> showAspectObserver = show -> {
        Timber.i("set aspect from observable");
        setAspectButton(show.hasAspectSettings());
    };

    private Observer<GotAspectEvent> showInputsObserver = show -> {
        Timber.i("set imputs from observable, ports size: " + show.getPortsList().size());
        setInputButton(!show.getPortsList().isEmpty());
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = RemoteControlFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = ViewModelProviders.of(getActivity(), viewModelFactory).get(RemoteControlViewModel.class);
        init();
        viewModel.getMuteStatus().observe(this, muteObserver);
        viewModel.getAspectSeen().observe(this, showAspectObserver);
        viewModel.getImputSeen().observe(this, showInputsObserver);
    }

    private void init() {
        binding.buttonAspect.setBackground(ResourcesCompat.getDrawable(getResources(), (App.isDarkMode() ? R.drawable.ic_aspect_settings_black : R.drawable.ic_aspect_settings), null));
        binding.volume.setClickListener(this);
        binding.channels.setClickListener(this);
        binding.dpadBottom.setOnTouchListener(getGenericTouchListener(KiviDPadView.SectorLocation.BOTTOM, KeyEvent.KEYCODE_DPAD_DOWN));
        binding.dpadLeft.setOnTouchListener(getGenericTouchListener(KiviDPadView.SectorLocation.LEFT, KeyEvent.KEYCODE_DPAD_LEFT));
        binding.dpadRight.setOnTouchListener(getGenericTouchListener(KiviDPadView.SectorLocation.RIGHT, KeyEvent.KEYCODE_DPAD_RIGHT));
        binding.dpadTop.setOnTouchListener(getGenericTouchListener(KiviDPadView.SectorLocation.TOP, KeyEvent.KEYCODE_DPAD_UP));

        binding.mute.setListener(v -> {
            viewModel.sendKeyEvent(KeyEvent.KEYCODE_VOLUME_MUTE);
            toggleMute();
        });

        binding.dpadOk.setOnClickListener(v -> viewModel.sendKeyEvent(KeyEvent.KEYCODE_DPAD_CENTER));
        binding.switchOff.setListener(v -> viewModel.switchOff());
        binding.buttonAspect.setOnClickListener(v -> viewModel.goToAspect());
        binding.input.setOnClickListener(click -> viewModel.goToInputSettings());

        setTvButtons(viewModel, binding.menu, binding.back, binding.home);
        setMute(PreferencesManager.INSTANCE.getMuteStatus());
    }


    @Override
    public void onResume() {
        super.onResume();
        if (!AspectHolder.INSTANCE.hasAspectSettings())
            viewModel.requestAspect();
        else {
            setInputButton(!AspectHolder.INSTANCE.getPortsList().isEmpty());
            setAspectButton(AspectHolder.INSTANCE.hasAspectSettings());
        }
    }


    @Override
    public void onDestroyView() {
        viewModel.getMuteStatus().removeObserver(muteObserver);
        super.onDestroyView();
    }

    @Override
    public void injectDependencies() {
        getFragmentComponent().inject(this);
    }

    private View.OnTouchListener getGenericTouchListener(KiviDPadView.SectorLocation secOr, int keyEvent) {
        return (v, event) -> {
            if (event.getAction() == ACTION_DOWN) {
                if (binding.dpadView != null) {
                    binding.dpadView.onSectorSelected(secOr, true);
                    binding.dpadView.onArrowPressed(secOr);
                }
                viewModel.sendKeyEvent(keyEvent);
                RemoteControlFragment.this.disposeOfContinuousClick();
                continuousClicks = RemoteControlFragment.this.getContinuousClickObservable(keyEvent);

            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                v.performClick();
                if (binding.dpadView != null) {
                    binding.dpadView.onArrowReleased(secOr);
                    binding.dpadView.onSectorSelected(secOr, false);
                }
                RemoteControlFragment.this.disposeOfContinuousClick();
            }
            return true;
        };
    }

    @Override
    public void onButtonClick(Integer buttonType) {
        viewModel.sendKeyEvent(buttonType);
    }

    public void setMute(boolean isMute) {
        if (isMute) {
            binding.mute.setImageResource(R.drawable.ic_mute_active);
            this.isMute = true;
        } else {
            binding.mute.setImageResource(R.drawable.selector_mute_btn);
            this.isMute = false;
        }
    }

    private void setAspectButton(boolean visible) {
        if (binding.buttonAspect != null) {
            binding.buttonAspect.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        }
    }

    private void setInputButton(boolean visible) {
        if (binding.input != null) {
            binding.input.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }


    private void toggleMute() {
        if (isMute) {
            isMute = false;
            binding.mute.setImageResource(R.drawable.selector_mute_btn);
        } else {
            isMute = true;
            binding.mute.setImageResource(R.drawable.ic_mute_active);
        }
    }

    private void disposeOfContinuousClick() {
        if (continuousClicks != null && !continuousClicks.isDisposed()) {
            continuousClicks.dispose();
            continuousClicks = null;
        }
    }

    private Disposable getContinuousClickObservable(int keyEvent) {
        return Observable.interval(INITIAL_DELAY,
                DPAD_EVENT_FREQUENCY,
                TimeUnit.MILLISECONDS)
                .subscribe(
                        t -> onButtonClick(keyEvent),
                        e -> Timber.e(e, e.getMessage())
                );
    }
}
