package com.wezom.kiviremote.bus;


public class NewVolumeEvent {
    private int volume;

    public NewVolumeEvent(int volume) {
        this.volume = volume;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }
}
