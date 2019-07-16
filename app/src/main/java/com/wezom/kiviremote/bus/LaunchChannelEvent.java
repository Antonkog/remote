package com.wezom.kiviremote.bus;


import com.wezom.kiviremote.net.model.Channel;

public class LaunchChannelEvent {
    Channel channel;

    public LaunchChannelEvent(Channel channel) {
        this.channel = channel;
    }

    public Channel getChannel() {
        return channel;
    }
}
