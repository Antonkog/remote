package com.kivi.remote.bus;


import com.kivi.remote.net.model.PreviewContent;

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
