package com.wezom.kiviremote.bus;


import com.wezom.kiviremote.net.model.PreviewCommonStructure;
import com.wezom.kiviremote.net.model.ServerAppInfo;

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
