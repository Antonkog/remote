package com.kivi.remote.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.kivi.remote.App;
import com.kivi.remote.bus.NetworkStateEvent;
import com.kivi.remote.bus.ReconnectEvent;
import com.kivi.remote.common.RxBus;
import com.kivi.remote.common.Utils;

import timber.log.Timber;


public class NetworkChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        ((App) context.getApplicationContext()).getApplicationComponent().inject(this);

        boolean isNetworkAvailable = Utils.isNetworkAvailable(context);
        Timber.d("NetworkChangeReceiver is network Available " + isNetworkAvailable);

        if (isNetworkAvailable)
            RxBus.INSTANCE.publish(new ReconnectEvent());

        RxBus.INSTANCE.publish(new NetworkStateEvent(isNetworkAvailable));
    }
}