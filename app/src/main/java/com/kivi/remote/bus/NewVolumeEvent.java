package com.kivi.remote.bus;


import com.kivi.remote.net.model.LastVolume;

public class NewVolumeEvent {
    private int volume;

    public NewVolumeEvent(int volume) {
        this.volume = volume;
        LastVolume.INSTANCE.setVolumeInt(volume);
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }
}
