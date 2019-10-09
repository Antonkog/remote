package com.wezom.kiviremote.bus;


import com.wezom.kiviremote.net.model.PreviewContent;

import java.util.List;

public class GotPreviewsContentEvent {
    private List<PreviewContent> previewContents;

    public List<PreviewContent> getPreviewContents() {
        return previewContents;
    }

    public GotPreviewsContentEvent(List<PreviewContent> previewContents) {
        this.previewContents = previewContents;
    }
}
