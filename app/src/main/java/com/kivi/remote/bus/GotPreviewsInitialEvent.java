package com.kivi.remote.bus;


import com.kivi.remote.net.model.PreviewCommonStructure;

import java.util.List;

public class GotPreviewsInitialEvent {
    private List<PreviewCommonStructure> previewCommonStructures;

    public List<PreviewCommonStructure> getPreviewCommonStructures() {
        return previewCommonStructures;
    }

    public GotPreviewsInitialEvent setPreviewCommonStructures(List<PreviewCommonStructure> previewCommonStructures) {
        this.previewCommonStructures = previewCommonStructures;
        return this;
    }
}
