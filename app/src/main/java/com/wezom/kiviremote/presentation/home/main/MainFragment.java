package com.wezom.kiviremote.presentation.home.main;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.res.ResourcesCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import com.wezom.kiviremote.App;
import com.wezom.kiviremote.R;
import com.wezom.kiviremote.bus.HideKeyboardEvent;
import com.wezom.kiviremote.bus.NavigateToRemoteEvent;
import com.wezom.kiviremote.bus.ShowKeyboardEvent;
import com.wezom.kiviremote.common.PreferencesManager;
import com.wezom.kiviremote.common.RxBus;
import com.wezom.kiviremote.common.Utils;
import com.wezom.kiviremote.databinding.MainFragmentBinding;
import com.wezom.kiviremote.presentation.base.BaseFragment;
import com.wezom.kiviremote.presentation.base.BaseViewModelFactory;
import com.wezom.kiviremote.presentation.home.HomeActivity;
import com.wezom.kiviremote.presentation.home.apps.AppsFragment;
import com.wezom.kiviremote.presentation.home.media.MediaFragment;
import com.wezom.kiviremote.presentation.home.remotecontrol.RemoteControlFragment;
import com.wezom.kiviremote.presentation.home.touchpad.TouchpadFragment;

import javax.inject.Inject;

import timber.log.Timber;

public class MainFragment extends BaseFragment implements BackHandler.OnBackClickListener {



    @Inject
    BaseViewModelFactory viewModelFactory;

    private MainFragmentBinding binding;

    private MainFragmentViewModel viewModel;

    private boolean isKeyboardShown;

    private BackHandler backButtonHandler;

    private int[] imageResId = {
            R.drawable.tab_remote_active, R.drawable.tab_touchpad, R.drawable.tab_apps, R.drawable.tab_media
    };

    private TabLayout.OnTabSelectedListener tabSelectedListener = new TabLayout.OnTabSelectedListener() {
        private TAB intToTab(int value) {
            return TAB.values()[value];
        }

        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            setSelectedTab(tab.getPosition());
            switch (intToTab(tab.getPosition())) {
                case REMOTE:
                    tab.setIcon(R.drawable.tab_remote_active);
                    if (getActivity() != null)
                        ((HomeActivity) getActivity()).hideSlidingPanel();
                    break;
                case TOUCHPAD:
                    tab.setIcon(R.drawable.tab_touchpad_active);
                    if (getActivity() != null)
                        ((HomeActivity) getActivity()).hideSlidingPanel();
                    break;
                case APPS:
                    tab.setIcon(R.drawable.tab_apps_active);
                    if (getActivity() != null)
                        ((HomeActivity) getActivity()).collapseSlidingPanel();
                    break;
                case MEDIA:
                    tab.setIcon(R.drawable.tab_media_active);
                    if (getActivity() != null)
                        ((HomeActivity) getActivity()).collapseSlidingPanel();
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {
            switch (intToTab(tab.getPosition())) {
                case REMOTE:
                    tab.setIcon(R.drawable.tab_remote);
                    break;
                case TOUCHPAD:
                    tab.setIcon(R.drawable.tab_touchpad);
                    break;
                case APPS:
                    tab.setIcon(R.drawable.tab_apps);
                    break;
                case MEDIA:
                    tab.setIcon(R.drawable.tab_media);
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {
            // Do nothing
        }
    };

    @Override
    public void injectDependencies() {
        getFragmentComponent().inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = MainFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MainFragmentViewModel.class);
        viewModel.startUPnPController();

        if (App.isDarkMode()) binding.topContainer.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.shape_gradient_black, null));
        else binding.topContainer.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.shape_gradient_white, null));

        initObservers();
        initTabLayout();
        initListeners();
    }

    private void initListeners() {
        binding.toolbar.mainText.setOnKeyListener((view1, i, keyEvent) -> {
            if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_DEL)
                viewModel.sendKeyEvent(KeyEvent.KEYCODE_DEL);
            return false;
        });

        binding.toolbar.mainText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.sendText(s.toString());
            }
        });

        binding.toolbar.mainText.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.sendKeyEvent(KeyEvent.KEYCODE_ENTER);
                return true;
            }

            if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_DEL) {
                viewModel.sendKeyEvent(KeyEvent.KEYCODE_DEL);
                return true;
            }
            return false;
        });

        binding.mainViewPager.setCurrentItem(getSelectedTab());
        binding.keyboard.setOnClickListener(v -> showInput());
        binding.devices.setOnClickListener(v -> viewModel.navigateToDevices());
        binding.toolbar.mainTextHide.setOnClickListener(v -> hideKeyboard());
    }


    private void initTabLayout() {
        MainFragmentPagerAdapter adapter = new MainFragmentPagerAdapter(getChildFragmentManager());

        binding.mainViewPager.setAdapter(adapter);
        binding.mainViewPager.setOffscreenPageLimit(3);

        binding.mainTabLayout.setupWithViewPager(binding.mainViewPager);
        binding.mainTabLayout.addOnTabSelectedListener(tabSelectedListener);

        for (int i = 0; i < MainFragmentPagerAdapter.PAGE_COUNT; i++) {
            binding.mainTabLayout.getTabAt(i).setIcon(imageResId[i]);
        }
    }

    private void initObservers() {
        disposables.add(RxBus.INSTANCE.listen(NavigateToRemoteEvent.class)
                .subscribe(event -> toRemote(), Timber::e));

        disposables.add(RxBus.INSTANCE.listen(ShowKeyboardEvent.class)
                .subscribe(event -> {
                    showKeyboard();
                    ((HomeActivity) getActivity()).hideSlidingPanel();
                }, Timber::e));

        disposables.add(RxBus.INSTANCE.listen(HideKeyboardEvent.class)
                .subscribe(event -> hideKeyboard(), Timber::e));
    }

    @Override
    public void onDestroy() {
        if (viewModel != null)
            viewModel.disconnect();
        super.onDestroy();
    }

    private void hideInput() {
        Utils.hideKeyboard(getActivity());
        binding.toolbar.mainText.clearFocus();
        binding.toolbar.mainContainer.setVisibility(View.GONE);
        binding.topContainer.setVisibility(View.VISIBLE);
        binding.mainViewPager.setVisibility(View.VISIBLE);
        isKeyboardShown = false;
    }

    private void showInput() {
        binding.toolbar.mainText.clearFocus();
        binding.toolbar.mainContainer.setVisibility(View.VISIBLE);
        binding.topContainer.setVisibility(View.GONE);
        binding.mainViewPager.setVisibility(View.GONE);
        binding.toolbar.mainText.requestFocus();
        binding.toolbar.mainText.setText("");
        Utils.showKeyboard(getActivity());
        isKeyboardShown = true;
    }

    @Override
    public void onPause() {
        Utils.hideKeyboard(getActivity());
        super.onPause();
    }

    public void showKeyboard() {
        if (!isKeyboardShown)
            showInput();
    }

    public void hideKeyboard() {
        if (isKeyboardShown)
            hideInput();
    }

    public void toRemote() {
        binding.mainViewPager.setCurrentItem(0);
    }

    @Override
    public boolean onBackClick() {
        if (isKeyboardShown) {
            hideInput();
            return true;
        }
        return false;
    }

    @Override
    public void onAttachFragment(Fragment childFragment) {
        super.onAttachFragment(childFragment);
        attachListener(((HomeActivity) getActivity()));
    }

    @Override
    public void onDetach() {
        detachListener();
        super.onDetach();
    }

    private void attachListener(HomeActivity activity) {
        if (activity != null)
            backButtonHandler = activity.addBackListener(this);
    }

    private void detachListener() {
        if (backButtonHandler != null)
            backButtonHandler.removeBackListener(this);
        backButtonHandler = null;
    }

    private int getSelectedTab() {
        return PreferencesManager.INSTANCE.getSelectedTab();
    }

    private void setSelectedTab(int position) {
        PreferencesManager.INSTANCE.setSelectedTab(position);
    }

    private enum TAB {
        REMOTE, TOUCHPAD, APPS, MEDIA
    }

    //    private static class MainFragmentPagerAdapter extends FragmentStatePagerAdapter {
    private static class MainFragmentPagerAdapter extends FragmentPagerAdapter {

        static final int PAGE_COUNT = 4;

        MainFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Override
        public BaseFragment getItem(int position) {
            switch (position) {
                case RemoteControlFragment.POSITION:
                    return new RemoteControlFragment();
                case TouchpadFragment.POSITION:
                    return new TouchpadFragment();
                case AppsFragment.POSITION:
                    return new AppsFragment();
                case MediaFragment.POSITION:
                    return new MediaFragment();
                default:
                    return null;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return null;
        }

    }
}
