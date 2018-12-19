package com.wezom.kiviremote.presentation.home.remotecontrol;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.wezom.kiviremote.R;
import com.wezom.kiviremote.common.PreferencesManager;
import com.wezom.kiviremote.databinding.RemoteControlFragmentBinding;
import com.wezom.kiviremote.interfaces.RockersButtonClickListener;
import com.wezom.kiviremote.net.model.AspectAvailable;
import com.wezom.kiviremote.net.model.AspectAvailableMock;
import com.wezom.kiviremote.presentation.base.BaseFragment;
import com.wezom.kiviremote.presentation.base.BaseViewModelFactory;
import com.wezom.kiviremote.presentation.home.tvsettings.AspectHolder;
import com.wezom.kiviremote.views.KiviDPadView;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_MOVE;
import static android.view.MotionEvent.ACTION_UP;
import static com.wezom.kiviremote.common.Constants.DPAD_EVENT_FREQUENCY;
import static com.wezom.kiviremote.common.Constants.HOME_KEY_DELAY;
import static com.wezom.kiviremote.common.Constants.INITIAL_DELAY;

/**
 * Created by andre on 29.05.2017.
 */

public class RemoteControlFragment extends BaseFragment implements RockersButtonClickListener<Integer> {
    public static final int POSITION = 0;

    @Inject
    BaseViewModelFactory viewModelFactory;

    RemoteControlViewModel viewModel;

    RemoteControlFragmentBinding binding;

    private boolean isMute = false;

    private Disposable continuousClicks;

    private long homeClickTime;

    private final Handler handler = new Handler();
    private final Runnable launchQuickApps = () -> viewModel.launchQuickApps();

    private final Observer<Boolean> muteObserver = status -> {
        if (status != null) {
            if (status)
                setMute(true);
            else
                setMute(false);
        }
    };

    private Observer<Boolean> showAspectObserver = show -> {
        if (show != null) setAspectButtons(show);
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
    }

    @Override
    public void onDestroyView() {
        viewModel.getMuteStatus().removeObserver(muteObserver);
        super.onDestroyView();
    }

    private void init() {
        binding.volume.setClickListener(this);
        binding.channels.setClickListener(this);
        binding.dpadBottom.setOnTouchListener(getGenericTouchListener(KiviDPadView.SectorLocation.BOTTOM, KeyEvent.KEYCODE_DPAD_DOWN));
        binding.dpadLeft.setOnTouchListener(getGenericTouchListener(KiviDPadView.SectorLocation.LEFT, KeyEvent.KEYCODE_DPAD_LEFT));
        binding.dpadRight.setOnTouchListener(getGenericTouchListener(KiviDPadView.SectorLocation.RIGHT, KeyEvent.KEYCODE_DPAD_RIGHT));
        binding.dpadTop.setOnTouchListener(getGenericTouchListener(KiviDPadView.SectorLocation.TOP, KeyEvent.KEYCODE_DPAD_UP));

        setMute(PreferencesManager.INSTANCE.getMuteStatus());

        if (AspectHolder.INSTANCE.getMessage() != null && AspectHolder.INSTANCE.getAvailableSettings() != null) {
            setAspectButtons(true);
        } else {
            //todo: put values in database when got them from tv,  why sometimes can't get aspect?
            Timber.e(" no Aspect from server");
            AspectHolder.INSTANCE.setAvailableSettings(new AspectAvailable(AspectAvailableMock.getAllAvailableValues(getContext())));
            AspectHolder.INSTANCE.setMessage(AspectAvailableMock.getTestMessage());
        }

        binding.mute.setOnClickListener(v -> {
            viewModel.sendButtonClick(KeyEvent.KEYCODE_VOLUME_MUTE);
            toggleMute();
        });

        binding.dpadOk.setOnClickListener(v -> viewModel.sendButtonClick(KeyEvent.KEYCODE_DPAD_CENTER));
        binding.switchOff.setOnClickListener(v -> viewModel.switchOff());

        binding.menu.setOnClickListener(v -> viewModel.sendButtonClick(KeyEvent.KEYCODE_MENU));
        binding.back.setOnClickListener(v -> viewModel.sendButtonClick(KeyEvent.KEYCODE_BACK));
        binding.buttonAspect.setOnClickListener(v -> viewModel.goToAspect());

        binding.input.setOnClickListener(view -> viewModel.goToInputSettings());

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
                viewModel.sendButtonClick(keyEvent);
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
        viewModel.sendButtonClick(buttonType);
    }

    public void setMute(boolean isMute) {
        if (isMute) {
            binding.mute.setImageDrawable(getResources().getDrawable(R.drawable.selector_mute_btn_active));
            this.isMute = true;
        } else {
            binding.mute.setImageDrawable(getResources().getDrawable(R.drawable.selector_mute_btn));
            this.isMute = false;
        }
    }

    private void setAspectButtons(boolean visible) {
        Timber.e(" setting aspect " + visible);
        if (binding.buttonAspect != null) {
            binding.buttonAspect.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        }

//        if(binding.input != null){
//            binding.input.setVisibility(visible ? View.VISIBLE : View.GONE);
//        }
    }

    private void toggleMute() {
        if (isMute) {
            isMute = false;
            binding.mute.setImageDrawable(getResources().getDrawable(R.drawable.selector_mute_btn));
        } else {
            isMute = true;
            binding.mute.setImageDrawable(getResources().getDrawable(R.drawable.selector_mute_btn_active));
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
