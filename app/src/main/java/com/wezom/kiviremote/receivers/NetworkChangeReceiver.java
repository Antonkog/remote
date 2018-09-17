package com.wezom.kiviremote.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.wezom.kiviremote.App;
import com.wezom.kiviremote.bus.NetworkStateEvent;
import com.wezom.kiviremote.bus.ReconnectEvent;
import com.wezom.kiviremote.common.RxBus;
import com.wezom.kiviremote.common.Utils;

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