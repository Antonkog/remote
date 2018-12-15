package com.wezom.kiviremote.presentation.home;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialog;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.wezom.kiviremote.R;
import com.wezom.kiviremote.Screens;
import com.wezom.kiviremote.bus.ChangeSnackbarStateEvent;
import com.wezom.kiviremote.bus.NetworkStateEvent;
import com.wezom.kiviremote.common.RxBus;
import com.wezom.kiviremote.common.Utils;
import com.wezom.kiviremote.common.extensions.StringUtils;
import com.wezom.kiviremote.databinding.HomeActivityBinding;
import com.wezom.kiviremote.presentation.base.BaseActivity;
import com.wezom.kiviremote.presentation.base.BaseViewModelFactory;
import com.wezom.kiviremote.presentation.home.gallery.GalleryFragment;
import com.wezom.kiviremote.presentation.home.main.BackHandler;
import com.wezom.kiviremote.receivers.NetworkChangeReceiver;
import com.wezom.kiviremote.services.CleanupService;
import com.wezom.kiviremote.services.NotificationService;
import com.wezom.kiviremote.upnp.UPnPManager;
import com.wezom.kiviremote.upnp.org.droidupnp.model.upnp.IRendererState;
import com.wezom.kiviremote.views.UPnPControlsNotification;

import org.fourthline.cling.android.FixedAndroidLogHandler;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import jp.wasabeef.glide.transformations.BlurTransformation;
import timber.log.Timber;

import static com.wezom.kiviremote.common.Constants.NOTIFICATION_ID;

public class HomeActivity extends BaseActivity implements BackHandler {

    public final MutableLiveData<Boolean> isPanelCollapsed = new MutableLiveData<>();

    private final ArrayList<WeakReference<OnBackClickListener>> backClickListeners = new ArrayList<>();

    private boolean hasContent = false;

    @Inject
    BaseViewModelFactory viewModelFactory;

    private HomeActivityBinding binding;
    private HomeActivityViewModel viewModel;

    private Snackbar reconnectSnackbar;
    private AppCompatDialog dialog;

    private IRendererState.State currentState;
    private SlidingUpPanelLayout.PanelState lastKnownState;

    private Disposable flowDisposable;
    private Disposable playDelayDisposable;

    private boolean isInterrupted;

    private NetworkChangeReceiver networkChangeReceiver = new NetworkChangeReceiver();

    private NotificationManager notificationManager;

    private Observer<HomeActivityViewModel.ProgressModel> progressObserver = model -> {
        if (model != null)
            setVideoProgress(model.getRendererModel().getProgress(),
                    model.getRendererModel().getState(),
                    model.getRendererModel().getDurationRemaining(),
                    model.getRendererModel().getDurationElapse(),
                    model.getCurrentMediaType());
    };

    private Observer<Boolean> showSettingsObserver = show -> {
        if (show != null && show)
            showSettingsDialog();
    };

    private Observer<UPnPManager.SlidingContentModel> slidingContentObserver = content -> {
        if (content != null) {
            setContent(content.getTitle(), content.getUri(), content.getPosition(), content.getType());
        }
    };

    private Observer<UPnPManager.SlideshowProgress> slideshowProgressObserver = state -> {
        if (state != null) {
            if (binding.layoutRender != null) {
                binding.layoutRender.renderSlideshowProgress.setProgress(state.getProgress());
            }

            if (state.getTerminate()) {
                binding.layoutRender.renderPlay.setBackground(getImageDrawable(R.drawable.ic_image_play));
                binding.layoutRender.renderPanelPlay.setImageDrawable(getImageDrawable(R.drawable.ic_panel_play));
                refreshPlayListener(null);
                return;
            }

            if (state.getPauseState()) {
                binding.layoutRender.renderPlay.setBackground(getImageDrawable(R.drawable.ic_image_play));
                binding.layoutRender.renderPanelPlay.setImageDrawable(getImageDrawable(R.drawable.ic_panel_play));
                refreshPlayListener(v -> {
                    Intent serviceIntent = new Intent(this, NotificationService.class);
                    serviceIntent.setAction(NotificationService.ACTION_SLIDESHOW_START);
                    startService(serviceIntent);
                });
            } else {
                binding.layoutRender.renderPlay.setBackground(getImageDrawable(R.drawable.ic_image_pause));
                binding.layoutRender.renderPanelPlay.setImageDrawable(getImageDrawable(R.drawable.ic_panel_pause));
                refreshPlayListener(v -> {
                    Intent serviceIntent = new Intent(this, NotificationService.class);
                    serviceIntent.setAction(NotificationService.ACTION_SLIDESHOW_STOP);
                    startService(serviceIntent);
                });
            }
        }
    };

    public static PendingIntent getDismissIntent(Context context) {
        Intent intent = new Intent(context, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        startService(new Intent(this, CleanupService.class));
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(HomeActivityViewModel.class);
        // Entry point
        viewModel.newRootScreen(Screens.REMOTE_CONTROL_FRAGMENT);

        binding = DataBindingUtil.setContentView(this, R.layout.home_activity);

        initNetworkChangeReceiver();
        initUpnpRequirements();
        setupViews();
        setupObservers();
    }

    private void setupViews() {
        setupSlidingLayout();
        reconnectSnackbar = setupSnackbar();

        if (binding.toolbarLayout != null) {
            binding.toolbarLayout.toolbar.setPadding(0, Utils.getStatusBarHeight(getResources()), 0, 0);
        }

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);


        if (binding.layoutRender != null) {
            binding.layoutRender.renderProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    viewModel.progressTo(seekBar.getProgress(), seekBar.getMax());
                }
            });

            binding.layoutRender.renderPanelCloseClick.setOnClickListener(v -> stopPlayback());
        }
    }

    private void initUpnpRequirements() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // Fix the logging integration between java.util.logging and Android internal logging
        org.seamless.util.logging.LoggingUtil.resetRootHandler(new FixedAndroidLogHandler());
    }

    private void initNetworkChangeReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        intentFilter.addAction("android.net.conn.CONNECTIVITY_ACTION");
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(networkChangeReceiver, intentFilter);
    }

    private Snackbar setupSnackbar() {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                R.string.connection_is_lost, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.return_to_home_screen, v -> Utils.triggerRebirth(this));
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(getResources().getColor(R.color.colorWhite));

        int snackbarTextId = android.support.design.R.id.snackbar_text;

        TextView textView = snackbarView.findViewById(snackbarTextId);
        textView.setTextColor(getResources().getColor(R.color.colorSecondaryText));
        return snackbar;
    }

    private void setupObservers() {
        viewModel.getProgress().observe(this, progressObserver);
        viewModel.getShowSettingsDialog().observe(this, showSettingsObserver);
        viewModel.getSlidingPanelContent().observe(this, slidingContentObserver);
        viewModel.getCurrentContentObservable().observe(this, slidingContentObserver);
        viewModel.getSlideshowStateObservable().observe(this, slideshowProgressObserver);
        flowDisposable = viewModel.getFlowSubject().subscribe(isInterrupted -> {
            this.isInterrupted = isInterrupted;
            if (isInterrupted)
                hideSlidingPanel();
        }, Timber::e);

        viewModel.getTriggerRebirth().observe(this, value -> {
            if (value != null && value) {
                Utils.triggerRebirth(this);
            }
        });

        RxBus.INSTANCE.listen(NetworkStateEvent.class).subscribe(event -> {
            if (!event.isAvailable() && backStackIsNotEmpty()) {
                showReconnectSnackbar();
            }
        });

        RxBus.INSTANCE.listen(ChangeSnackbarStateEvent.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        event -> {
                            if (event.getHide()) {
                                hideReconnectSnackbar();
                            } else {
                                if (backStackIsNotEmpty()) {
                                    showReconnectSnackbar();
                                }
                            }
                        }, Timber::e);

    }

    private boolean backStackIsNotEmpty() {
        int count = getSupportFragmentManager().getBackStackEntryCount();
        return count > 0;
    }

    public void showReconnectSnackbar() {
        if (!reconnectSnackbar.isShown())
            reconnectSnackbar.show();
        binding.disconnectStatusIndicator.setVisibility(View.VISIBLE);
    }

    public void hideReconnectSnackbar() {
        if (reconnectSnackbar.isShown())
            reconnectSnackbar.dismiss();
        binding.disconnectStatusIndicator.setVisibility(View.GONE);
    }

    private void removeObservers() {
        viewModel.getProgress().removeObserver(progressObserver);
        viewModel.getShowSettingsDialog().removeObserver(showSettingsObserver);
        viewModel.getSlidingPanelContent().removeObserver(slidingContentObserver);
        viewModel.getCurrentContentObservable().removeObserver(slidingContentObserver);
        viewModel.getSlideshowStateObservable().removeObserver(slideshowProgressObserver);
        if (flowDisposable != null && !flowDisposable.isDisposed())
            flowDisposable.dispose();
    }

    public void stopPlayback() {
        if (viewModel != null) {
            viewModel.stopPlayback();
            notificationManager.cancel(NOTIFICATION_ID);
        }
    }

    private void setupSlidingLayout() {
        binding.slidingLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                if (binding.layoutRender != null) {
                    binding.layoutRender.renderShowHide.setRotation(slideOffset * -180);
                    binding.layoutRender.renderShowHide.setAlpha(slideOffset);
                    binding.layoutRender.renderMask.setAlpha(slideOffset);
                    binding.layoutRender.renderBackgroundPreview.setAlpha(slideOffset);
                    binding.layoutRender.renderTitle.setAlpha(slideOffset);
                    binding.layoutRender.renderPreview.setAlpha(slideOffset);

                    float previewOffset = 1 - slideOffset * 5;
                    previewOffset = previewOffset < 0 ? 0 : previewOffset;
                    binding.layoutRender.renderPreviewTitle.setAlpha(previewOffset);
                    binding.layoutRender.renderPreviewThumbnail.setAlpha(previewOffset);
                    binding.layoutRender.renderPlayingNow.setAlpha(previewOffset);
                    binding.layoutRender.renderPanelNext.setAlpha(previewOffset);
                    binding.layoutRender.renderPanelPrevious.setAlpha(previewOffset);
                    binding.layoutRender.renderPanelPlay.setAlpha(previewOffset);
                    binding.layoutRender.renderPanelClose.setAlpha(previewOffset);
                }
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                Timber.i("onPanelStateChanged " + newState);
                lastKnownState = newState;

                if (newState == SlidingUpPanelLayout.PanelState.EXPANDED) {
                    isPanelCollapsed.postValue(true);
                    if (binding.layoutRender != null) {
                        binding.layoutRender.renderPanelNext.setClickable(false);
                        binding.layoutRender.renderPanelPrevious.setClickable(false);
                        binding.layoutRender.renderPanelPlay.setClickable(false);
                        binding.layoutRender.renderPanelCloseClick.setClickable(false);
                    }
                }

                if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                    isPanelCollapsed.postValue(true);
                    return;
                }

                if (newState != SlidingUpPanelLayout.PanelState.EXPANDED
                        && binding.layoutRender != null) {
                    binding.layoutRender.renderPanelNext.setClickable(true);
                    binding.layoutRender.renderPanelPrevious.setClickable(true);
                    binding.layoutRender.renderPanelPlay.setClickable(true);
                    binding.layoutRender.renderPanelCloseClick.setClickable(true);
                }

                if (newState == SlidingUpPanelLayout.PanelState.HIDDEN) {
                    isPanelCollapsed.postValue(false);
                }
            }
        });

        binding.slidingLayout.setFadeOnClickListener(view -> binding.slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED));
        hideSlidingPanel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.setNavigator(baseNavigator);
    }

    @Override
    protected void onPause() {
        viewModel.removeNavigator();
//        viewModel.stopUPnPController();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        viewModel.tearConnectionDown();
        removeObservers();
//        viewModel.stopUPnPController();
        unregisterReceiver(networkChangeReceiver);
        super.onDestroy();
    }

    @Override
    public void injectDependency() {
        getActivityComponent().inject(this);
    }

    public void showBackButton() {
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setDisplayShowHomeEnabled(true);
        }
        if (binding.toolbarLayout != null) {
            binding.toolbarLayout.toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }
    }

    public void showSettingsDialog() {
        if (dialog == null) {
            dialog = new AlertDialog.Builder(this, R.style.DialogTheme)
                    .setMessage(R.string.wrong_input)
                    .setPositiveButton(R.string.ok, (dialog1, which) -> viewModel.openSettings())
                    .setNegativeButton(R.string.close, null)
                    .create();
        }

        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    @Override
    public BackHandler addBackListener(OnBackClickListener listener) {
        backClickListeners.add(new WeakReference<>(listener));
        return this;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null && intent.getAction() != null && binding.layoutRender != null) {
            switch (intent.getAction()) {
                case UPnPControlsNotification.ACTION_PLAY:
                    binding.layoutRender.renderPlay.performClick();
                    break;
                case UPnPControlsNotification.ACTION_NEXT:
                    binding.layoutRender.renderNext.performClick();
                    break;
                case UPnPControlsNotification.ACTION_PREVIOUS:
                    binding.layoutRender.renderPrevious.performClick();
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void removeBackListener(OnBackClickListener listener) {
        for (Iterator<WeakReference<OnBackClickListener>> iterator = backClickListeners.iterator();
             iterator.hasNext(); ) {
            WeakReference<OnBackClickListener> weakRef = iterator.next();
            if (weakRef.get() == listener) {
                iterator.remove();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (binding.slidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED)
            binding.slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        else if (!fragmentsBackKeyIntercept())
            super.onBackPressed();
    }

    private boolean fragmentsBackKeyIntercept() {
        boolean isIntercept = false;

        if (backClickListeners.isEmpty())
            return false;

        for (WeakReference<OnBackClickListener> weakRef : backClickListeners) {
            OnBackClickListener onBackClickListener = weakRef.get();
            if (onBackClickListener != null) {
                boolean isFragmentIntercept = onBackClickListener.onBackClick();
                if (!isIntercept) isIntercept = isFragmentIntercept;
            }
        }
        return isIntercept;
    }

    public boolean hasReadPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public void showOpenSettingsDialog() {
        new AlertDialog.Builder(this).setMessage(R.string.open_settings_dialog).setPositiveButton(R.string.open, (dialog1, which) -> {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
        }).setNegativeButton(R.string.cancel, null).show();
    }

    public void hideSlidingPanel() {
        if (binding.slidingLayout.getPanelState() != SlidingUpPanelLayout.PanelState.HIDDEN)
            binding.slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
    }

    public void expandSlidingPanel() {
        if (binding.slidingLayout.getPanelState() != SlidingUpPanelLayout.PanelState.EXPANDED)
            binding.slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
    }

    public void collapseSlidingPanel() {
        if (isInterrupted) {
            hideSlidingPanel();
            return;
        }

        if (hasContent && binding.slidingLayout.getPanelState() != SlidingUpPanelLayout.PanelState.COLLAPSED) {
            binding.slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        }
    }

    public void setVideoProgress(int progress, IRendererState.State state, String durationElapse, String remainingDuration, GalleryFragment.MediaType mediaType) {
        runOnUiThread(() -> {
            if (binding.layoutRender != null) {
                binding.layoutRender.renderProgress.setProgress(progress);
                if (remainingDuration != null)
                    binding.layoutRender.renderElapsed.setText(StringUtils.formatDuration(remainingDuration));
                if (durationElapse != null)
                    binding.layoutRender.renderRemaining.setText(StringUtils.formatDuration(durationElapse));

                if (mediaType == GalleryFragment.MediaType.VIDEO) {
                    switch (state) {
                        case PLAY:
                            binding.layoutRender.renderPlay.setBackground(getImageDrawable(R.drawable.ic_video_pause));
                            binding.layoutRender.renderPanelPlay.setImageDrawable(getImageDrawable(R.drawable.ic_panel_pause));
                            refreshPlayListener(v -> viewModel.pausePlayback());
                            break;

                        case PAUSE:
                            binding.layoutRender.renderPlay.setBackground(getImageDrawable(R.drawable.ic_video_play));
                            binding.layoutRender.renderPanelPlay.setImageDrawable(getImageDrawable(R.drawable.ic_panel_play));
                            refreshPlayListener(v -> viewModel.resumePlayback());
                            break;

                        case STOP:
                            binding.layoutRender.renderPlay.setBackground(getImageDrawable(R.drawable.ic_video_play));
                            binding.layoutRender.renderPanelPlay.setImageDrawable(getImageDrawable(R.drawable.ic_panel_play));
                            restartPlayDelay();
                            break;

                        default:
                            binding.layoutRender.renderPlay.setBackground(getImageDrawable(R.drawable.ic_video_play));
                            binding.layoutRender.renderPanelPlay.setImageDrawable(getImageDrawable(R.drawable.ic_panel_play));
                            break;
                    }
                    currentState = state;
                }
            }
        });
    }

    private void refreshClickListeners(GalleryFragment.MediaType type) {
        View.OnClickListener playClickListener;
        if (type == GalleryFragment.MediaType.IMAGE)
            playClickListener = v -> {
                Intent serviceIntent = new Intent(this, NotificationService.class);
                serviceIntent.setAction(NotificationService.ACTION_SLIDESHOW_START);
                startService(serviceIntent);
            };
        else
            playClickListener = view -> {
                if (currentState != null) switch (currentState) {
                    case PLAY:
                        viewModel.pausePlayback();
                        break;
                    case PAUSE:
                        viewModel.resumePlayback();
                        break;
                    case STOP:
                        viewModel.renderCurrentItem();
                        break;
                }
            };

        View.OnClickListener prevClickListener = v -> {
            if (binding.layoutRender != null) {
                binding.layoutRender.renderSlideshowProgress.setProgress(0);
            }

            Intent serviceIntent = new Intent(this, NotificationService.class);
            serviceIntent.setAction(NotificationService.ACTION_PREVIOUS);
            startService(serviceIntent);
        };

        View.OnClickListener nextClickListener = v -> {
            if (binding.layoutRender != null) {
                binding.layoutRender.renderSlideshowProgress.setProgress(0);
            }

            Intent serviceIntent = new Intent(this, NotificationService.class);
            serviceIntent.setAction(NotificationService.ACTION_NEXT);
            startService(serviceIntent);
        };

        if (binding.layoutRender != null) {

            binding.layoutRender.renderPlay.setOnClickListener(playClickListener);
            binding.layoutRender.renderNext.setOnClickListener(nextClickListener);
            binding.layoutRender.renderPrevious.setOnClickListener(prevClickListener);

            binding.layoutRender.renderPanelPlay.setOnClickListener(playClickListener);
            binding.layoutRender.renderPanelNext.setOnClickListener(nextClickListener);
            binding.layoutRender.renderPanelPrevious.setOnClickListener(prevClickListener);

            if (lastKnownState == SlidingUpPanelLayout.PanelState.EXPANDED) {
                binding.layoutRender.renderPanelPlay.setClickable(false);
                binding.layoutRender.renderPanelNext.setClickable(false);
                binding.layoutRender.renderPanelPrevious.setClickable(false);
            }
        }
    }

    private void refreshPlayListener(View.OnClickListener listener) {
        if (binding.layoutRender != null) {
            binding.layoutRender.renderPlay.setOnClickListener(listener);
            binding.layoutRender.renderPanelPlay.setOnClickListener(listener);

            if (lastKnownState == SlidingUpPanelLayout.PanelState.EXPANDED) {
                binding.layoutRender.renderPanelPlay.setClickable(false);
            }
        }
    }

    public void setContent(String title, String url, int position, GalleryFragment.MediaType type) {
        if (binding.layoutRender != null) {
            binding.layoutRender.slidingPane.setVisibility(View.VISIBLE);
            hasContent = true;

            binding.layoutRender.renderTitle.setText(title);
            binding.layoutRender.renderPreviewTitle.setText(title);

            binding.layoutRender.renderNext.setOnClickListener(null);

            switch (type) {
                case IMAGE:
                    viewModel.removeProgressObserver();
                    binding.layoutRender.renderPlay.setBackground(getImageDrawable(R.drawable.ic_image_play));
                    binding.layoutRender.renderPanelPlay.setImageDrawable(getImageDrawable(R.drawable.ic_panel_play));
                    binding.layoutRender.renderPrevious.setBackground(getImageDrawable(R.drawable.ic_image_prev));
                    binding.layoutRender.renderNext.setBackground(getImageDrawable(R.drawable.ic_image_next));
                    binding.layoutRender.renderProgress.setVisibility(View.INVISIBLE);
                    binding.layoutRender.renderMask.setVisibility(View.INVISIBLE);
                    binding.layoutRender.renderBackgroundPreview.setVisibility(View.INVISIBLE);
                    binding.layoutRender.renderElapsed.setVisibility(View.INVISIBLE);
                    binding.layoutRender.renderRemaining.setVisibility(View.INVISIBLE);
                    binding.layoutRender.renderTitle.setTextColor(getResources().getColor(R.color.sliding_panel_image_title));
                    binding.layoutRender.renderSlideshowProgressBackground.setVisibility(View.VISIBLE);
                    binding.layoutRender.renderSlideshowProgress.setVisibility(View.VISIBLE);
                    binding.layoutRender.renderCounter.setTextColor(getResources().getColor(R.color.image_counter));
                    binding.layoutRender.renderCounter.setText(getResources().getString(R.string.current_position,
                            String.valueOf(position + 1),
                            String.valueOf(viewModel.getImageContentSize())));

                    refreshClickListeners(type);
                    loadPreview(url, type);
                    break;
                case VIDEO:
                    viewModel.observeProgress();
                    Glide.with(this)
                            .load(url)
                            .apply(RequestOptions.bitmapTransform(new BlurTransformation(5, 2)))
                            .into(binding.layoutRender.renderBackgroundPreview);
                    binding.layoutRender.renderPrevious.setBackground(getImageDrawable(R.drawable.ic_video_prev));
                    binding.layoutRender.renderNext.setBackground(getImageDrawable(R.drawable.ic_video_next));
                    binding.layoutRender.renderPlay.setBackground(getImageDrawable(R.drawable.ic_video_play));
                    binding.layoutRender.renderMask.setVisibility(View.VISIBLE);
                    binding.layoutRender.renderBackgroundPreview.setVisibility(View.VISIBLE);
                    binding.layoutRender.renderProgress.setVisibility(View.VISIBLE);
                    binding.layoutRender.renderElapsed.setVisibility(View.VISIBLE);
                    binding.layoutRender.renderRemaining.setVisibility(View.VISIBLE);
                    binding.layoutRender.renderTitle.setTextColor(getResources().getColor(R.color.sliding_panel_video_title));
                    binding.layoutRender.renderSlideshowProgressBackground.setVisibility(View.INVISIBLE);
                    binding.layoutRender.renderSlideshowProgress.setVisibility(View.INVISIBLE);
                    binding.layoutRender.renderCounter.setTextColor(getResources().getColor(R.color.playing_now));
                    binding.layoutRender.renderCounter.setText(getResources().getString(R.string.current_position,
                            String.valueOf(position + 1),
                            String.valueOf(viewModel.getVideoContentSize())));

                    restartPlayDelay();
                    loadPreview(url, type);
                    break;
                default:
                    break;
            }
        }

        startNotificationService(title, url, position, type);
    }

    private void startNotificationService(String title, String url, int position, GalleryFragment.MediaType type) {
        Intent serviceIntent = new Intent(this, NotificationService.class);
        serviceIntent.putExtra(NotificationService.ITEM_TITLE, title);
        serviceIntent.putExtra(NotificationService.ITEM_URL, url);
        serviceIntent.putExtra(NotificationService.ITEM_POSITION, position);
        serviceIntent.putExtra(NotificationService.ITEM_TYPE, type.name());
        serviceIntent.setAction(NotificationService.ACTION_START_FOREGROUND);
        startService(serviceIntent);
    }

    private void restartPlayDelay() {
        if (playDelayDisposable != null && !playDelayDisposable.isDisposed())
            playDelayDisposable.dispose();

        playDelayDisposable = Single.timer(5, TimeUnit.SECONDS).subscribe(delay -> refreshClickListeners(GalleryFragment.MediaType.VIDEO));
    }

    private void loadPreview(String url, GalleryFragment.MediaType type) {
        switch (type) {
            case IMAGE:
                Glide.with(this).load(url).apply(new RequestOptions().error(R.drawable.bg_placeholder_image_shadow)).into(binding.layoutRender.renderPreview);
                Glide.with(this).load(url).apply(new RequestOptions().error(R.drawable.bg_placeholder_image)).into(binding.layoutRender.renderPreviewThumbnail);
                break;

            case VIDEO:
                Glide.with(this).load(url).apply(new RequestOptions().error(R.drawable.bg_placeholder_video_shadow)).into(binding.layoutRender.renderPreview);
                Glide.with(this).load(url).apply(new RequestOptions().error(R.drawable.bg_placeholder_video)).into(binding.layoutRender.renderPreviewThumbnail);
        }
    }

    private Drawable getImageDrawable(int id) {
        return getResources().getDrawable(id);
    }

    public boolean isHasContent() {
        return hasContent;
    }

    public void setHasContent(boolean hasContent) {
        this.hasContent = hasContent;
    }
}
