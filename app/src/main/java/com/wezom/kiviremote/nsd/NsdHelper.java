package com.wezom.kiviremote.nsd;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;

import com.jakewharton.rxrelay2.BehaviorRelay;
import com.jakewharton.rxrelay2.Relay;
import com.wezom.kiviremote.di.qualifiers.ApplicationContext;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

/**
 * Created by andre on 25.05.2017.
 */
public class NsdHelper {
    public static final String SERVICE_MASK = "(KIVI_TV)";
    private static final String SERVICE_TYPE = "_http._tcp.";
    private CopyOnWriteArraySet<NsdServiceInfoWrapper> servicesSet = new CopyOnWriteArraySet<>();

    private final NsdManager mNsdManager;

    private NsdManager.ResolveListener mResolveListener;
    private NsdManager.DiscoveryListener mDiscoveryListener;
    private NsdServiceInfo chosenServiceInfo;

    private Relay<Set<NsdServiceInfoWrapper>> nsdServices;

    private Disposable deviceNotFoundTimerDisposable;

    @Inject
    public NsdHelper(@ApplicationContext Context context) {
        mNsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
        nsdServices = BehaviorRelay.create();
        nsdServices.doOnError(e -> Timber.e(e, e.getMessage()));
    }

    private void initializeDiscoveryListener() {
        this.mDiscoveryListener = new NsdManager.DiscoveryListener() {

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Timber.d("onStartDiscoveryFailed %s ", errorCode);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Timber.d("onStopDiscoveryFailed %s ", errorCode);
            }

            @Override
            public void onDiscoveryStarted(String serviceType) {
                Timber.d("Service discovery started");
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Timber.d("onDiscoveryStopped %s ", serviceType);
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                Timber.d("Found NSD device: " + service);
                if (!service.getServiceType().equals(SERVICE_TYPE)) {
                    Timber.d("Unknown Service Type: " + service.getServiceType());
                } else if (service.getServiceName().equals(SERVICE_MASK)) {
                    Timber.d("Same machine: " + SERVICE_MASK);
                } else if (service.getServiceName().contains(SERVICE_MASK)) {
                    killTimer();
                    servicesSet.add(new NsdServiceInfoWrapper(service));
                    nsdServices.accept(servicesSet);
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo serviceInfo) {
                Timber.d("NSD onServiceLost %s ", serviceInfo.getHost());
                boolean removedSuccessfully = servicesSet.remove(new NsdServiceInfoWrapper(serviceInfo));
                if (removedSuccessfully) {
                    nsdServices.accept(servicesSet);
                    if (chosenServiceInfo != null && chosenServiceInfo.equals(serviceInfo)) {
                        chosenServiceInfo = null;
                    }
                }
            }
        };
    }

    public Relay<Set<NsdServiceInfoWrapper>> getNsdRelay() {
        return nsdServices;
    }

    public void initializeResolveListener(NsdManager.ResolveListener listener) {
        mResolveListener = listener;
    }

    public void discoverServices() {
        servicesSet.clear();
        stopDiscovery();
        initializeDiscoveryListener();
        startTimer();
        mNsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
    }

    private Completable getTimer() {
        return Completable.timer(10000, TimeUnit.MILLISECONDS);
    }

    private void startTimer() {
        if (deviceNotFoundTimerDisposable != null && !deviceNotFoundTimerDisposable.isDisposed())
            deviceNotFoundTimerDisposable.dispose();
        deviceNotFoundTimerDisposable = getTimer().subscribe(() -> nsdServices.accept(servicesSet));
    }

    private void killTimer() {
        if (deviceNotFoundTimerDisposable != null && !deviceNotFoundTimerDisposable.isDisposed())
            deviceNotFoundTimerDisposable.dispose();
    }

    public void stopDiscovery() {
        if (mDiscoveryListener != null) {
            try {
                killTimer();
                mNsdManager.stopServiceDiscovery(mDiscoveryListener);
            } catch (Exception e) {
                Timber.e(e, "Failure while stopping discoveryListener: " + e.getMessage());
            }

            mDiscoveryListener = null;
        }
    }

    public void resolve(NsdServiceInfo serviceInfo, NsdManager.ResolveListener listener) {
        try {
            mResolveListener = listener;
            mNsdManager.resolveService(serviceInfo, mResolveListener);
        } catch (Exception e) {
            Timber.e(e, e.getMessage());
        }
    }
}
