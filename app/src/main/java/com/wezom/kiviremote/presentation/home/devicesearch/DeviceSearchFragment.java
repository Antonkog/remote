package com.wezom.kiviremote.presentation.home.devicesearch;

import android.annotation.SuppressLint;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wezom.kiviremote.bus.ChangeSnackbarStateEvent;
import com.wezom.kiviremote.bus.KillPingEvent;
import com.wezom.kiviremote.bus.LocationEnabledEvent;
import com.wezom.kiviremote.common.Constants;
import com.wezom.kiviremote.common.GpsUtils;
import com.wezom.kiviremote.common.NetConnectionUtils;
import com.wezom.kiviremote.common.PreferencesManager;
import com.wezom.kiviremote.common.RxBus;
import com.wezom.kiviremote.common.recycler.RecyclerViewClickListener;
import com.wezom.kiviremote.databinding.HomeFragmentBinding;
import com.wezom.kiviremote.nsd.LastNsdHolder;
import com.wezom.kiviremote.nsd.NsdServiceInfoWrapper;
import com.wezom.kiviremote.presentation.base.BaseFragment;
import com.wezom.kiviremote.presentation.base.BaseViewModelFactory;
import com.wezom.kiviremote.presentation.home.HomeActivity;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * Created by andre on 22.05.2017.
 */

public class DeviceSearchFragment extends BaseFragment implements RecyclerViewClickListener {

    @Inject
    BaseViewModelFactory viewModelFactory;
    DeviceSearchAdapter adapter;

    private List<NsdServiceInfoWrapper> currentDevices = new ArrayList<>();
    private DeviceSearchViewModel viewModel;
    private HomeFragmentBinding binding;
    private Observer<Boolean> networkStateObserver;
    private Observer<Set<NsdServiceInfoWrapper>> nsdDevicesObserver;

    @SuppressLint("CheckResult")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        networkStateObserver = isAvailable -> { if (isAvailable != null && isAvailable) onNetworkAvailable(); else onNetworkNotAvailable(); };

        nsdDevicesObserver = devices -> {
            if (devices != null) {
                updateDeviceList(devices);
                tryGoMainScreen(devices);
            }
        };

        RxBus.INSTANCE.listen(LocationEnabledEvent.class).subscribe(event -> {
            if (event.getEnabled()) {
                showWifiName();
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = HomeFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(DeviceSearchViewModel.class);
        viewModel.updateRecentDevices();
        viewModel.initResolveListener();
        viewModel.discoverDevices();

        initObservers();
        PreferencesManager.INSTANCE.setSelectedTab(0);
        initAdapter();

        resetDisconnectStatus();
        resetMediaPlayback(((HomeActivity) getActivity()));
    }

    @Override
    public void onResume() {
        super.onResume();
        Timber.d("OnResume");
        viewModel.updateRecentDevices();
        viewModel.discoverDevices();
        showWifiName();

        if (getActivity() == null) { return; }
        ((HomeActivity) getActivity()).hideSlidingPanel();
    }

    private void initObservers() {
        viewModel.getNsdDevices().observe(this, nsdDevicesObserver);
        viewModel.getNetworkState().observe(this, networkStateObserver);
    }

    private void initAdapter() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(binding.devicesContainer.getContext(), layoutManager.getOrientation());
        adapter = new DeviceSearchAdapter((v, position) -> connect(currentDevices.get(position)));

        binding.devicesContainer.setLayoutManager(layoutManager);
        binding.devicesContainer.addItemDecoration(dividerItemDecoration);
        binding.devicesContainer.setAdapter(adapter);
    }

    private void tryGoMainScreen(Set<NsdServiceInfoWrapper> devices) {
        if (getActivity() == null) { return; }
        if (getActivity().getIntent().getBooleanExtra(Constants.BUNDLE_REALUNCH_KEY, false) && LastNsdHolder.INSTANCE.getNsdServiceWrapper() != null && devices.contains(LastNsdHolder.INSTANCE.getNsdServiceWrapper())) {
            Handler h = new Handler();
            h.postDelayed(() -> connect(LastNsdHolder.INSTANCE.getNsdServiceWrapper()), Constants.DELAY_COLOR_RESTART);
            Timber.e("App is restarted");
        } else {
            Timber.e("App is not restarted");
        }
    }

    private void connect(NsdServiceInfoWrapper wrapper) {
        if (wrapper != null) {
            viewModel.connect(wrapper);
            LastNsdHolder.INSTANCE.setNsdServiceWrapper(wrapper);
        }
    }

    /**
     * Call this to kill ping interval and reset disconnect status after reconnecting to another device
     */
    private void resetDisconnectStatus() {
        RxBus.INSTANCE.publish(new ChangeSnackbarStateEvent(true));
        RxBus.INSTANCE.publish(new KillPingEvent());
    }

    private void resetMediaPlayback(HomeActivity activity) {
        if (activity != null) {
            activity.setHasContent(false);
            activity.stopPlayback();
        }
    }

    @Override
    public void injectDependencies() {
        getFragmentComponent().inject(this);
    }

    public void updateDeviceList(Set<NsdServiceInfoWrapper> set) {
        Timber.d("LOG_ updateDeviceList %s", set.size());
        currentDevices.clear();
        currentDevices.addAll(set);

        adapter.setData(currentDevices);
        binding.devicesContainer.setVisibility(View.VISIBLE);
        hideProgress();
    }

    public void hideProgress() {
        binding.searchProgressContainer.setVisibility(View.GONE);
    }

    public void onNetworkNotAvailable() {
        Timber.d("onNetworkNotAvailable");
        updateWifiInfoViews();
        adapter.setData(new ArrayList<>());
        hideProgress();
    }

    public void onNetworkAvailable() {
        Timber.d("onNetworkAvailable");
        updateWifiInfoViews();
    }

    private void updateWifiInfoViews() {
        if (getContext() == null) { return; }
        if (NetConnectionUtils.isConnectedWithWifi(getContext())) {
            showWifiInfo();
        } else {
            hideWifiInfo();
        }
    }

    private void showWifiInfo() {
        if (getActivity() == null) { return; }
        GpsUtils.INSTANCE.enableGPS(getActivity(), null,null);
        binding.wifiIcon.setVisibility(View.VISIBLE);
        binding.wifiName.setVisibility(View.VISIBLE);
    }

    private void hideWifiInfo() {
        binding.wifiIcon.setVisibility(View.INVISIBLE);
        binding.wifiName.setVisibility(View.INVISIBLE);
    }
    
    private void showWifiName() {
        if (getContext() == null) { return; }
        if (NetConnectionUtils.isConnectedWithWifi(getContext())) {
            binding.wifiName.setText(NetConnectionUtils.getCurrentSsid(getContext()).replace("\"", ""));
        }
    }

    @Override
    public void recyclerViewListClicked(@NotNull View v, int position) {
        connect(currentDevices.get(position));
    }
}

//    private void setNoDeviceViewState() {
//        //binding.noDeviceContainer.setVisibility(View.VISIBLE);
//        binding.devicesContainer.setVisibility(View.INVISIBLE);
//        //binding.singleDeviceContainer.setVisibility(View.INVISIBLE);
//        //binding.singleDeviceConnect.setVisibility(View.GONE);
//        //binding.wifiSettings.setVisibility(View.VISIBLE);
//        binding.searchProgressContainer.setVisibility(View.GONE);
//        hideProgress();
//    }
//
//    private void setSingleDeviceViewState(String deviceName) {
//        //binding.singleDeviceTitle.setText(deviceName);
//        //binding.singleDeviceContainer.setVisibility(View.VISIBLE);
//        //binding.noDeviceContainer.setVisibility(View.GONE);
//        binding.devicesContainer.setVisibility(View.GONE);
//        //binding.singleDeviceConnect.setVisibility(View.VISIBLE);
//        //binding.wifiSettings.setVisibility(View.GONE);
//        setMultipleDevicesConnectButtonState(false);
//        setSingleDeviceConnectButtonState(true);
//        hideProgress();
//        setButtonText(R.string.discovery_connect);
//    }

//    private void setButtonText(int textId) {
//        //binding.singleDeviceConnect.setText(textId);
//    }

//    private void setMultipleDevicesConnectButtonState(boolean enable) {
//        //binding.multipleDeviceConnect.setClickable(enable);
//    }
//
//    private void setSingleDeviceConnectButtonState(boolean enable) {
//        //binding.singleDeviceConnect.setClickable(enable);
//    }