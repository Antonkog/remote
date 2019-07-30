package com.wezom.kiviremote.presentation.home;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.annotation.IntDef;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.wezom.kiviremote.App;
import com.wezom.kiviremote.R;
import com.wezom.kiviremote.Screens;
import com.wezom.kiviremote.bus.ChangeSnackbarStateEvent;
import com.wezom.kiviremote.bus.HideKeyboardEvent;
import com.wezom.kiviremote.bus.LocationEnabledEvent;
import com.wezom.kiviremote.bus.NetworkStateEvent;
import com.wezom.kiviremote.bus.SetVolumeEvent;
import com.wezom.kiviremote.bus.ShowKeyboardEvent;
import com.wezom.kiviremote.common.GpsUtils;
import com.wezom.kiviremote.common.RxBus;
import com.wezom.kiviremote.common.Utils;
import com.wezom.kiviremote.common.extensions.StringUtils;
import com.wezom.kiviremote.databinding.HomeActivityBinding;
import com.wezom.kiviremote.presentation.base.BaseActivity;
import com.wezom.kiviremote.presentation.base.BaseViewModelFactory;
import com.wezom.kiviremote.presentation.home.gallery.GalleryFragment;
import com.wezom.kiviremote.presentation.home.main.BackHandler;
import com.wezom.kiviremote.presentation.home.tvsettings.AspectHolder;
import com.wezom.kiviremote.receivers.NetworkChangeReceiver;
import com.wezom.kiviremote.services.CleanupService;
import com.wezom.kiviremote.services.NotificationService;
import com.wezom.kiviremote.upnp.UPnPManager;
import com.wezom.kiviremote.upnp.org.droidupnp.model.upnp.IRendererState;
import com.wezom.kiviremote.views.UPnPControlsNotification;

import org.fourthline.cling.android.FixedAndroidLogHandler;

import java.lang.annotation.Retention;
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
import static java.lang.annotation.RetentionPolicy.SOURCE;

public class HomeActivity extends BaseActivity implements BackHandler {

    public final MutableLiveData<Boolean> isPanelCollapsed = new MutableLiveData<>();

    private final ArrayList<WeakReference<OnBackClickListener>> backClickListeners = new ArrayList<>();

    private boolean hasContent = false;
    // define a variable to track hamburger-arrow state
    protected boolean isHomeAsUp = false;
    protected boolean isKeyboardShown = false;
    protected Toolbar toolbar;
    protected ActionBarDrawerToggle toggle;
    private DrawerLayout drawerLayout;
    private AudioManager audioManager;

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
                binding.layoutRender.renderPlay.setImageResource(R.drawable.ic_image_play);
                binding.layoutRender.renderPanelPlay.setImageResource(R.drawable.ic_panel_play);
                refreshPlayListener(null);
                return;
            }

            if (state.getPauseState()) {
                binding.layoutRender.renderPlay.setImageResource(R.drawable.ic_image_play);
                binding.layoutRender.renderPanelPlay.setImageResource(R.drawable.ic_panel_play);
                refreshPlayListener(v -> {
                    Intent serviceIntent = new Intent(this, NotificationService.class);
                    serviceIntent.setAction(NotificationService.ACTION_SLIDESHOW_START);
                    startService(serviceIntent);
                });
            } else {
                binding.layoutRender.renderPlay.setImageResource(R.drawable.ic_image_pause);
                binding.layoutRender.renderPanelPlay.setImageResource(R.drawable.ic_panel_pause);
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
        if (App.isDarkMode()) setTheme(R.style.Dark);
        else setTheme(R.style.Light);

        notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        startCleanupService();
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(HomeActivityViewModel.class);
        // Entry point
        viewModel.newRootScreen(Screens.DEVICE_SEARCH_FRAGMENT);

        binding = DataBindingUtil.setContentView(this, R.layout.home_activity);

       audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        initNetworkChangeReceiver();
        initUpnpRequirements();
        setupViews();
        setupObservers();
    }


    // I've implemented it in setContentView(), but you can implement it in onCreate()
//    @Override
//    public void setContentView(@LayoutRes int layoutResID) {
//        super.setContentView(layoutResID);

    private void configureToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // overwrite Navigation OnClickListener that is set by ActionBarDrawerToggle
        toolbar.setNavigationOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else if (isHomeAsUp) {
                onBackPressed();
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        binding.mainText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrow_down_selector, 0);


        binding.search.setOnClickListener(v -> showKeyboard());

        binding.toolbarETxt.mainTextHide.setOnClickListener(v -> hideKeyboard());

        binding.fab.setOnClickListener(view -> viewModel.goTo(Screens.TOUCH_PAD_FRAGMENT));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            binding.fab.setElevation(getResources().getDimension(R.dimen.elevation_small));
        }
    }

    public void changeToolbarVisibility(int visible) {
        this.runOnUiThread(() -> toolbar.setVisibility(visible));
    }

    public void changeFabVisibility(int visible) {
        this.runOnUiThread(() -> binding.fab.setVisibility(visible));
    }


    // call this method for animation between hamburged and arrow
    public void setHomeAsUp(boolean isHomeAsUp) {
        if (this.isHomeAsUp != isHomeAsUp) {
            this.isHomeAsUp = isHomeAsUp;

            ValueAnimator anim = isHomeAsUp ? ValueAnimator.ofFloat(0, 1) : ValueAnimator.ofFloat(1, 0);
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float slideOffset = (Float) valueAnimator.getAnimatedValue();
                    toggle.onDrawerSlide(drawerLayout, slideOffset);
                    if (isHomeAsUp)
                        binding.mainText.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                    else
                        binding.mainText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrow_down_selector, 0);

                }
            });
            anim.setInterpolator(new DecelerateInterpolator());
            // You can change this duration to more closely match that of the default animation.
            anim.setDuration(400);
            anim.start();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GpsUtils.INSTANCE.getRESULT_CODE()) {
            if (resultCode == Activity.RESULT_OK) {
                RxBus.INSTANCE.publish(new LocationEnabledEvent(true));
            } else {
                RxBus.INSTANCE.publish(new LocationEnabledEvent(false));
            }
        }
    }

    private void startCleanupService() {
        try {
            startService(new Intent(this, CleanupService.class));
        } catch (IllegalStateException e) {
            Timber.e("cant start CleanupService in background " + e.getMessage());
        }
    }

    private void setupViews() {
        setupSlidingLayout();
        reconnectSnackbar = setupSnackbar();
//        ActionBarDrawerToggle.Delegate delegate = getDrawerToggleDelegate();

//        binding.toolbar.setPadding(0, Utils.getStatusBarHeight(getResources()), 0, 0);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        configureNavigationDrawer();
        configureToolbar();

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

    // to call when router need arrow back
    private void configureNavigationDrawer() {
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navView = findViewById(R.id.nav_view);
//        navView.setPadding(0, Utils.getStatusBarHeight(getResources()), 0, 0);
        navView.setNavigationItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.nav_devices:
                    viewModel.goTo(Screens.DEVICE_SEARCH_FRAGMENT);
                    break;

                case R.id.nav_subscriptions:
                    viewModel.goTo(Screens.DEVICE_SEARCH_FRAGMENT);
                    break;

                case R.id.nav_settings:
                    if (AspectHolder.INSTANCE.hasAspectSettings())
                        viewModel.goTo(Screens.TV_SETTINGS_FRAGMENT);
                    break;

                case R.id.nav_support:
                    String url = "https://kivi.ua/support-center";
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    try {
                        startActivity(i);
                    } catch (ActivityNotFoundException e) {
                        Timber.e("can't go to support " + e);
                        Toast.makeText(this, " ActivityNotFoundException", Toast.LENGTH_LONG).show();
                    }
                    break;
                case R.id.dark_mode:
                    viewModel.restartColorScheme(this);
                    break;

                case R.id.nav_exit:
                    this.finish();
                    break;

            }
            return false;
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_UP:
                int volume = Math.round(audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM)*100/audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM));
                RxBus.INSTANCE.publish(new SetVolumeEvent(volume));
                return true;
            default:
                return super.onKeyDown(keyCode, event);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            // Android home
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
            // manage other entries if you have it ...
        }
        return true;
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
        flowDisposable = viewModel.getFlowSubject().subscribe(interrupted -> {
            this.isInterrupted = interrupted;
            if (isInterrupted)
                hideSlidingPanel();
        }, Timber::e);

        viewModel.getTriggerRebirth().observe(this, value -> {
            if (value != null && value) {
                Utils.triggerRebirth(this);
            }
        });


        RxBus.INSTANCE.listen(ShowKeyboardEvent.class)
                .subscribe(event -> {
                    showKeyboard();
                    hideSlidingPanel();
                }, Timber::e);

        RxBus.INSTANCE.listen(HideKeyboardEvent.class)
                .subscribe(event -> hideKeyboard(), Timber::e);


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
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
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
        if (!fragmentsBackKeyIntercept())
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


    public boolean hasRecordAudioPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
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
//        if (binding.slidingLayout.getPanelState() != SlidingUpPanelLayout.PanelState.HIDDEN)
//            binding.slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
    }

    public void expandSlidingPanel() {
//        if (binding.slidingLayout.getPanelState() != SlidingUpPanelLayout.PanelState.EXPANDED)
//            binding.slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
    }

    public void collapseSlidingPanel() {
        if (isInterrupted) {
            hideSlidingPanel();
            return;
        }

//        if (hasContent && binding.slidingLayout.getPanelState() != SlidingUpPanelLayout.PanelState.COLLAPSED) {
//            binding.slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
//        }
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
                            binding.layoutRender.renderPlay.setImageResource(R.drawable.ic_image_pause);
                            binding.layoutRender.renderPanelPlay.setImageResource(R.drawable.ic_panel_pause);
                            refreshPlayListener(v -> viewModel.pausePlayback());
                            break;

                        case PAUSE:
                            binding.layoutRender.renderPlay.setImageResource(R.drawable.ic_image_play);
                            binding.layoutRender.renderPanelPlay.setImageResource(R.drawable.ic_panel_play);
                            refreshPlayListener(v -> viewModel.resumePlayback());
                            break;

                        case STOP:
                            binding.layoutRender.renderPlay.setImageResource(R.drawable.ic_image_play);
                            binding.layoutRender.renderPanelPlay.setImageResource(R.drawable.ic_panel_play);
                            restartPlayDelay();
                            break;

                        default:
                            binding.layoutRender.renderPlay.setImageResource(R.drawable.ic_image_play);
                            binding.layoutRender.renderPanelPlay.setImageResource(R.drawable.ic_panel_play);
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
                    binding.layoutRender.renderPlay.setImageResource(R.drawable.ic_image_play);
                    binding.layoutRender.renderPanelPlay.setImageResource(R.drawable.ic_panel_play);
                    binding.layoutRender.renderPrevious.setImageResource(R.drawable.ic_image_prev);
                    binding.layoutRender.renderNext.setImageResource(R.drawable.ic_image_next);
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
                    binding.layoutRender.renderPrevious.setImageResource(R.drawable.ic_image_prev);
                    binding.layoutRender.renderNext.setImageResource(R.drawable.ic_image_next);
                    binding.layoutRender.renderPlay.setImageResource(R.drawable.ic_image_play);
                    binding.layoutRender.renderMask.setVisibility(View.VISIBLE);
                    binding.layoutRender.renderBackgroundPreview.setVisibility(View.VISIBLE);
                    binding.layoutRender.renderProgress.setVisibility(View.VISIBLE);
                    binding.layoutRender.renderElapsed.setVisibility(View.VISIBLE);
                    binding.layoutRender.renderRemaining.setVisibility(View.VISIBLE);
                    binding.layoutRender.renderTitle.setTextColor(getResources().getColor(App.isDarkMode() ? R.color.sliding_panel_video_title : R.color.sliding_panel_video_black));
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
                Glide.with(this).load(url).apply(new RequestOptions().error(R.drawable.placeholder_image)).into(binding.layoutRender.renderPreview);
                Glide.with(this).load(url).apply(new RequestOptions().error(R.drawable.placeholder_image)).into(binding.layoutRender.renderPreviewThumbnail);
                break;

            case VIDEO:
                Glide.with(this).load(url).apply(new RequestOptions().error(R.drawable.placeholder_video)).into(binding.layoutRender.renderPreview);
                Glide.with(this).load(url).apply(new RequestOptions().error(R.drawable.placeholder_video)).into(binding.layoutRender.renderPreviewThumbnail);
                break;
            default:
                Glide.with(this).load(url).apply(new RequestOptions().error(R.drawable.placeholder_image)).into(binding.layoutRender.renderPreview);
                Glide.with(this).load(url).apply(new RequestOptions().error(R.drawable.placeholder_image)).into(binding.layoutRender.renderPreviewThumbnail);
                break;
        }
    }


    public void showKeyboard() {
        if (!isKeyboardShown)
            showInput(true);
    }

    public void hideKeyboard() {
        if (isKeyboardShown)
            showInput(false);
    }

    private void showInput(boolean show) {
        if (show) {
            binding.toolbarETxt.mainText.clearFocus(); //etxt
            binding.toolbarETxt.mainContainer.setVisibility(View.VISIBLE);// maint tb
            binding.toolbarLayout.setVisibility(View.GONE);//other toolbar
            binding.toolbarETxt.mainText.requestFocus();//etxt
            binding.toolbarETxt.mainText.setText("");//etxt
            Utils.showKeyboard(this);
            isKeyboardShown = true;
        } else {
            Utils.hideKeyboard(this);
            binding.toolbarETxt.mainText.clearFocus(); //etxt
            binding.toolbarETxt.mainContainer.setVisibility(View.GONE); // maint tb
            binding.toolbarLayout.setVisibility(View.VISIBLE); //other toolbar
            isKeyboardShown = false;
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
