package com.wezom.kiviremote.bus;

import com.wezom.kiviremote.net.model.PreviewCommonStructure;

/**
 * Created by andre on 06.06.2017.
 */
public class TVPlayerEvent {
    private PlayerAction playerAction;
    private int progress;
    private PreviewCommonStructure playerPreview;

    public TVPlayerEvent(PreviewCommonStructure playerPreview) {
        this.playerPreview = playerPreview;
        this.playerAction = PlayerAction.LAUNCH_PLAYER;
    }

    public TVPlayerEvent(PlayerAction playerAction, int progress) {
        this.playerAction = playerAction;
        this.progress = progress;
    }

    public PlayerAction getPlayerAction() {
        return playerAction;
    }

    public int getProgress() {
        return progress;
    }

    public PreviewCommonStructure getPlayerPreview() {
        return playerPreview;
    }

    public enum PlayerAction {
        LAUNCH_PLAYER,
        CHANGE_STATE,
        SEEK_TO
    }
}
