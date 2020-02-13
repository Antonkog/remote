package com.kivi.remote.bus;

import java.util.List;

import androidx.annotation.Nullable;

/**
 * Created by andre on 06.06.2017.
 */
public class RemotePlayerEvent {
    private PlayerAction playerAction;
    private List<Float> args;

    public RemotePlayerEvent(PlayerAction playerAction, @Nullable List<Float> args) {
        this.playerAction = playerAction;
        this.args = args;
    }

    public RemotePlayerEvent() { }

    public PlayerAction getPlayerAction() {
        return playerAction;
    }

    public List<Float> getArgs() {
        return args;
    }

    public enum PlayerAction {
        PLAY_NEXT,
        PLAY_PREV,
        PLAY,
        PAUSE,
        SEEK_TO,
        CLOSE,
        REQUEST_STATE,
        REQUEST_CONTENT
    }

}
