package com.kivi.remote.bus;


import com.kivi.remote.net.model.Channel;

public class LaunchChannelEvent {
    Channel channel;

    public LaunchChannelEvent(Channel channel) {
        this.channel = channel;
    }

    public Channel getChannel() {
        return channel;
    }
}
