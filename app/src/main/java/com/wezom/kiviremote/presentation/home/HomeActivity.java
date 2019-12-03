package com.wezom.kiviremote.presentation.home;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.snackbar.Snackbar;
import com.wezom.kiviremote.App;
import com.wezom.kiviremote.R;
import com.wezom.kiviremote.Screens;
import com.wezom.kiviremote.bus.ChangeSnackbarStateEvent;
import com.wezom.kiviremote.bus.HideKeyboardEvent;
import com.wezom.kiviremote.bus.LocationEnabledEvent;
import com.wezom.kiviremote.bus.NetworkStateEvent;
import com.wezom.kiviremote.bus.SetVolumeEvent;
import com.wezom.kiviremote.bus.ShowKeyboardEvent;
import com.wezom.kiviremote.common.Constants;
import com.wezom.kiviremote.common.GpsUtils;
import com.wezom.kiviremote.common.RxBus;
import com.wezom.kiviremote.common.Utils;
import com.wezom.kiviremote.databinding.HomeActivityBinding;
import com.wezom.kiviremote.presentation.base.BaseActivity;
import com.wezom.kiviremote.presentation.base.BaseViewModelFactory;
import com.wezom.kiviremote.presentation.home.gallery.GalleryFragment;
import com.wezom.kiviremote.presentation.home.main.BackHandler;
import com.wezom.kiviremote.presentation.home.player.PlayerFragment;
import com.wezom.kiviremote.presentation.home.touchpad.TouchpadFragment;
import com.wezom.kiviremote.presentation.home.tvsettings.LastVolume;
import com.wezom.kiviremote.receivers.NetworkChangeReceiver;
import com.wezom.kiviremote.services.CleanupService;
import com.wezom.kiviremote.views.LockableBottomSheetBehavior;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

public class HomeActivity extends BaseActivity implements BackHandler {

    public final MutableLiveData<Boolean> isTouchPadCollapsed = new MutableLiveData<>();
    public final MutableLiveData<Boolean> isPlayerCollapsed = new MutableLiveData<>();

    private final ArrayList<WeakReference<OnBackClickListener>> backClickListeners = new ArrayList<>();

    // define a variable to track hamburger-arrow state
    protected boolean isHomeAsUp = false;
    protected boolean isKeyboardShown = false;
    protected Toolbar toolbar;
    protected ActionBarDrawerToggle toggle;
    private DrawerLayout drawerLayout;
    private LockableBottomSheetBehavior playerSheetBechavior = null;
    private LockableBottomSheetBehavior touchpadSheetBehavior = null;

    @Inject
    BaseViewModelFactory viewModelFactory;

    private HomeActivityBinding binding;
    private HomeActivityViewModel viewModel;

    private Snackbar reconnectSnackbar;
    private AppCompatDialog dialog;


    private NetworkChangeReceiver networkChangeReceiver = new NetworkChangeReceiver();

    private NotificationManager notificationManager;

    public Toolbar getToolbar() {
        return toolbar;
    }

    private Observer<Boolean> showSettingsObserver = show -> {
        if (show != null && show)
            showSettingsDialog();
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

        initNetworkChangeReceiver();
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

        binding.search.setOnClickListener(v -> showKeyboard());

        binding.mainTextHide.setOnClickListener(v -> hideKeyboard());

        binding.fab.setOnClickListener(view -> moveTouchPad(BottomSheetBehavior.STATE_EXPANDED));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            binding.fab.setElevation(getResources().getDimension(R.dimen.elevation_small));
        }
    }

    public void setToolbarTxt(String text) {
//        binding.toolbarText.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        binding.toolbarText.setText(text);
    }

    public void uncheckMenu() {
        for (int i = 0; i < binding.navView.getMenu().size(); i++) {
            binding.navView.getMenu().getItem(i).setChecked(false);
        }
    }

    public void moveTouchPad(int state) {
        touchpadSheetBehavior.setState(state);
    }

    public void hideSlidingPanel() {
        if (playerSheetBechavior != null && playerSheetBechavior.getState() != BottomSheetBehavior.STATE_COLLAPSED)
            playerSheetBechavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    public void expandSlidingPanel() {
        if (playerSheetBechavior != null && playerSheetBechavior.getState() != BottomSheetBehavior.STATE_EXPANDED)
            playerSheetBechavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    public void changeToolbarVisibility(int visibility) {
        this.runOnUiThread(() -> toolbar.setVisibility(visibility));
    }

    public void changeFabVisibility(int visibility) {
        this.runOnUiThread(() -> binding.fab.setVisibility(visibility));
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

        setupMediaSlider();
        setupTouchpadSlider();
        reconnectSnackbar = setupSnackbar();
//        ActionBarDrawerToggle.Delegate delegate = getDrawerToggleDelegate();
//        binding.toolbar.setPadding(0, Utils.getStatusBarHeight(getResources()), 0, 0);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        configureNavigationDrawer();
        configureToolbar();

        binding.switchDm.setChecked(App.isDarkMode());

        binding.switchDm.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.restartColorScheme(this);
        });

        binding.autoConnect.setChecked(viewModel.getAutoConnect());
        binding.autoConnect.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.setAutoConnect(isChecked);
        });

    }

    // to call when router need arrow back
    private void configureNavigationDrawer() {
        drawerLayout = findViewById(R.id.drawer_layout);

        binding.navView.setNavigationItemSelectedListener(menuItem -> {

            drawerLayout.closeDrawer(GravityCompat.START);
            menuItem.setChecked(true);

            switch (menuItem.getItemId()) {
                case R.id.nav_devices:
                    viewModel.goTo(Screens.RECENT_DEVICES_FRAGMENT);
                    break;

//                case R.id.nav_settings:
//                    viewModel.clearData(); ///test!!!
//                    Toast.makeText(this, " cleaned db ", Toast.LENGTH_LONG).show();
////                    viewModel.goToDeviceInfo(new TvDeviceInfo(new RecentDevice(viewModel.getCurrentContentName(), null), null, 0));
//                    break;

                case R.id.nav_support:
                    String url = "https://kivi.ua/support-center";
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    try {
                        startActivity(i);
                    } catch (ActivityNotFoundException e) {
                        Timber.e("can't go to support " + e);
                    }
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
        final int volume;
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                volume = LastVolume.INSTANCE.getVolumeInt() >= Constants.VOLUME_EVENT_POINT ? LastVolume.INSTANCE.getVolumeInt() - Constants.VOLUME_EVENT_POINT : 0;
                break;
            case KeyEvent.KEYCODE_VOLUME_UP:
                volume = LastVolume.INSTANCE.getVolumeInt() >= 100 ? 100 : LastVolume.INSTANCE.getVolumeInt() + Constants.VOLUME_EVENT_POINT;
                break;
            default:
                return super.onKeyDown(keyCode, event);
        }
        if (volume != LastVolume.INSTANCE.getVolumeInt()) {
            RxBus.INSTANCE.publish(new SetVolumeEvent(volume));
            return true;
        }
        return super.onKeyDown(keyCode, event);
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

        int snackbarTextId = com.google.android.material.R.id.snackbar_text;

        TextView textView = snackbarView.findViewById(snackbarTextId);
        textView.setTextColor(getResources().getColor(R.color.colorSecondaryText));
        return snackbar;
    }

    private void setupObservers() {
        viewModel.getShowSettingsDialog().observe(this, showSettingsObserver);

        viewModel.getTriggerRebirth().observe(this, value -> {
            if (value != null && value) {
                Utils.triggerRebirth(this);
            }
        });


        RxBus.INSTANCE.listen(ShowKeyboardEvent.class)
                .subscribe(event -> {
                    showKeyboard();
                    hideSlidingPanel();
                    moveTouchPad(BottomSheetBehavior.STATE_COLLAPSED);
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
//        changeFabVisibility(View.GONE);
        binding.disconnectStatusIndicator.setVisibility(View.VISIBLE);
    }

    public void hideReconnectSnackbar() {
        if (reconnectSnackbar.isShown())
            reconnectSnackbar.dismiss();
//        changeFabVisibility(View.VISIBLE);
        binding.disconnectStatusIndicator.setVisibility(View.GONE);
    }

    private void removeObservers() {
        viewModel.getShowSettingsDialog().removeObserver(showSettingsObserver);
    }



    private void setupMediaSlider() {

// настройка поведения нижнего экрана for player
        FrameLayout frameLayout = new FrameLayout(this);
        frameLayout.setId(View.generateViewId());

        Fragment playerFragment = new PlayerFragment();
        getSupportFragmentManager().beginTransaction().add(frameLayout.getId(), playerFragment, Screens.PLAYER_FRAGMENT).commit();
        binding.mediaSlider.addView(frameLayout);

        playerSheetBechavior = LockableBottomSheetBehavior.Companion.from(findViewById(R.id.media_slider));

        playerSheetBechavior.setHideable(true);

        playerSheetBechavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                    case BottomSheetBehavior.STATE_HIDDEN:
                        isPlayerCollapsed.postValue(true);
                        changeFabVisibility(View.VISIBLE);
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        isPlayerCollapsed.postValue(false);
                        changeFabVisibility(View.GONE);
                        break;
                    default:
                        isPlayerCollapsed.postValue(false);
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
        playerSheetBechavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }


    private void setupTouchpadSlider() {

        FrameLayout frameLayout = new FrameLayout(this);
        frameLayout.setId(View.generateViewId());
        Fragment touchpadFragment = new TouchpadFragment();
        getSupportFragmentManager().beginTransaction().add(frameLayout.getId(), touchpadFragment, Screens.TOUCH_PAD_FRAGMENT).commit();
        binding.touchpadSlider.addView(frameLayout);

        touchpadSheetBehavior = LockableBottomSheetBehavior.Companion.from(findViewById(R.id.touchpad_slider));
        touchpadSheetBehavior.setHideable(true);
        touchpadSheetBehavior.setSwipeEnabled(false); ///!!!!!!!
        touchpadSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                    case BottomSheetBehavior.STATE_HIDDEN:
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        changeFabVisibility(View.GONE);
                        break;
                    default:
                        changeFabVisibility(View.GONE);
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

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
        if (playerSheetBechavior != null && playerSheetBechavior.getState() != BottomSheetBehavior.STATE_COLLAPSED)
            playerSheetBechavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        if (touchpadSheetBehavior != null && touchpadSheetBehavior.getState() != BottomSheetBehavior.STATE_COLLAPSED)
            touchpadSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
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
            binding.editText.clearFocus(); //etxt
            binding.toolbarLayout.setVisibility(View.GONE);//other toolbar
            binding.toolbarETxt.setVisibility(View.VISIBLE);// maint tb
            binding.editText.requestFocus();//etxt
            binding.editText.setText("");//etxt
            Utils.showKeyboard(this);
            isKeyboardShown = true;
        } else {
            Utils.hideKeyboard(this);
            binding.editText.clearFocus(); //etxt
            binding.toolbarETxt.setVisibility(View.GONE); // maint tb
            binding.toolbarLayout.setVisibility(View.VISIBLE); //other toolbar
            isKeyboardShown = false;
        }
    }

    public void setUpnpContent(@Nullable String title, @Nullable String image, int position, @NotNull GalleryFragment.MediaType type) {
        //todo: if we will  use UPNP we will need to set content on new sliding panel. (What to do with recommendations media???
    }
}

