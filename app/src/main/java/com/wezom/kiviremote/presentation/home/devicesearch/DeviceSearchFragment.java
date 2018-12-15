package com.wezom.kiviremote.presentation.home.devicesearch;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wezom.kiviremote.R;
import com.wezom.kiviremote.bus.ChangeSnackbarStateEvent;
import com.wezom.kiviremote.bus.KillPingEvent;
import com.wezom.kiviremote.common.NetConnectionUtils;
import com.wezom.kiviremote.common.PreferencesManager;
import com.wezom.kiviremote.common.RxBus;
import com.wezom.kiviremote.databinding.HomeFragmentBinding;
import com.wezom.kiviremote.nsd.NsdServiceInfoWrapper;
import com.wezom.kiviremote.presentation.base.BaseFragment;
import com.wezom.kiviremote.presentation.base.BaseViewModelFactory;
import com.wezom.kiviremote.presentation.home.HomeActivity;
import com.wezom.kiviremote.presentation.home.tvsettings.AspectHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import timber.log.Timber;


/**
 * Created by andre on 22.05.2017.
 */

public class DeviceSearchFragment extends BaseFragment {

    @Inject
    BaseViewModelFactory viewModelFactory;

    DeviceSearchAdapter adapter;

    private List<NsdServiceInfoWrapper> currentDevices = new ArrayList<>();

    private DeviceSearchViewModel viewModel;
    private HomeFragmentBinding binding;

    private final Observer<Boolean> networkStateObserver = isAvailable -> {
        if (isAvailable != null && isAvailable)
            onNetworkAvailable();
        else
            onNetworkNotAvailable();
    };

    private final Observer<Set<NsdServiceInfoWrapper>> nsdDevicesObserver = devices -> {
        if (devices != null) {
            if (devices.isEmpty()) {
                onDeviceNotFound();
            }

            if (devices.size() == 1) {
                onSingleDevice(devices.iterator().next());
            }

            if (devices.size() > 1) {
                updateDeviceList(devices);
            }
        }
    };

    private NsdServiceInfoWrapper currentSingleDevice;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = HomeFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    private void initObservers() {
        viewModel.getNsdDevices().observe(this, nsdDevicesObserver);
        viewModel.getNetworkState().observe(this, networkStateObserver);
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

        initClickListeners();
        resetDisconnectStatus();

        resetMediaPlayback(((HomeActivity) getActivity()));
    }

    /**
     * Call this to kill ping interval and reset disconnect status after reconnecting to another device
     */
    private void resetDisconnectStatus() {
        AspectHolder.INSTANCE.setMessage(null);
        AspectHolder.INSTANCE.setAvailableSettings(null);
        RxBus.INSTANCE.publish(new ChangeSnackbarStateEvent(true));
        RxBus.INSTANCE.publish(new KillPingEvent());
    }

    private void initClickListeners() {
        binding.singleDeviceConnect.setOnClickListener(v -> connect(currentSingleDevice));
        binding.multipleDeviceConnect.setOnClickListener(v -> connect(adapter.getCurrentSelectedItem()));
        binding.homeHidden.setOnClickListener(v -> {
                    startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    Log.i(this.getClass().getName(), "on Click2");
                }
        );
        binding.wifiSettings.setOnClickListener(click -> {
            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
        });
    }

    private void resetMediaPlayback(HomeActivity activity) {
        if (activity != null) {
            activity.setHasContent(false);
            activity.stopPlayback();
        }
    }

    private void connect(NsdServiceInfoWrapper wrapper) {
        if (wrapper != null)
            viewModel.connect(wrapper);
    }

    @Override
    public void onResume() {
        super.onResume();
        Timber.d("OnResume");
        showWifiSSID();
        viewModel.updateRecentDevices();
        viewModel.discoverDevices();
        ((HomeActivity) getActivity()).hideSlidingPanel();
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
        setMultipleDevices();
    }

    public void onSingleDevice(NsdServiceInfoWrapper wrapper) {
        setSingleDeviceViewState(wrapper.getServiceName());
        currentSingleDevice = wrapper;
        hideProgress();
    }

    public void onDeviceNotFound() {
        setNoDeviceViewState();
    }

    public void hideProgress() {
        binding.searchProgressContainer.setVisibility(View.GONE);
    }

    public void onNetworkNotAvailable() {
        Timber.d("onNetworkNotAvailable");
        showWifiSSID();
        adapter.setData(new ArrayList<>());
        hideProgress();
    }

    public void onNetworkAvailable() {
        Timber.d("onNetworkAvailable");
        showWifiSSID();
    }

    private void showWifiSSID() {
        String ssid = NetConnectionUtils.getCurrentSsid(getActivity());
        if (NetConnectionUtils.isConnectedWithWifi(getContext())) {
            binding.wifiIsNotAvailableContainer.setVisibility(View.INVISIBLE);
            binding.wifiIcon.setVisibility(View.VISIBLE);
            binding.wifiNameContainer.setVisibility(View.VISIBLE);
            binding.connectedToLabel.setVisibility(View.VISIBLE);
            binding.wifiName.setVisibility(View.VISIBLE);
            binding.wifiSettings.setVisibility(View.GONE);
            binding.wifiName.setText(ssid.replace("\"", ""));
        } else {
            binding.wifiIcon.setVisibility(View.INVISIBLE);
            binding.connectedToLabel.setVisibility(View.GONE);
            binding.wifiIsNotAvailableContainer.setVisibility(View.VISIBLE);
            binding.wifiName.setVisibility(View.INVISIBLE);
            binding.wifiSettings.setVisibility(View.VISIBLE);
        }
    }

    private void initAdapter() {
        binding.devicesContainer.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        adapter = new DeviceSearchAdapter();
        binding.devicesContainer.setAdapter(adapter);
    }

    private void setMultipleDevices() {
        binding.singleDeviceContainer.setVisibility(View.GONE);
        binding.noDeviceContainer.setVisibility(View.GONE);
        binding.devicesContainer.setVisibility(View.VISIBLE);
        binding.singleDeviceConnect.setVisibility(View.INVISIBLE);
        binding.multipleDeviceConnect.setVisibility(View.VISIBLE);
        binding.wifiSettings.setVisibility(View.GONE);
        setMultipleDevicesConnectButtonState(true);
        setSingleDeviceConnectButtonState(false);
        hideProgress();
    }

    private void setNoDeviceViewState() {
        binding.noDeviceContainer.setVisibility(View.VISIBLE);
        binding.devicesContainer.setVisibility(View.INVISIBLE);
        binding.singleDeviceContainer.setVisibility(View.INVISIBLE);
        binding.singleDeviceConnect.setVisibility(View.GONE);
        binding.wifiSettings.setVisibility(View.VISIBLE);
        binding.searchProgressContainer.setVisibility(View.GONE);
        hideProgress();
    }

    private void setSingleDeviceViewState(String deviceName) {
        binding.singleDeviceTitle.setText(deviceName);
        binding.singleDeviceContainer.setVisibility(View.VISIBLE);
        binding.noDeviceContainer.setVisibility(View.GONE);
        binding.devicesContainer.setVisibility(View.GONE);
        binding.singleDeviceConnect.setVisibility(View.VISIBLE);
        binding.wifiSettings.setVisibility(View.GONE);
        setMultipleDevicesConnectButtonState(false);
        setSingleDeviceConnectButtonState(true);
        hideProgress();
        setButtonText(R.string.discovery_connect);
    }

    private void setButtonText(int textId) {
        binding.singleDeviceConnect.setText(textId);
    }

    private void setMultipleDevicesConnectButtonState(boolean enable) {
        binding.multipleDeviceConnect.setClickable(enable);
    }

    private void setSingleDeviceConnectButtonState(boolean enable) {
        binding.singleDeviceConnect.setClickable(enable);
    }
}
