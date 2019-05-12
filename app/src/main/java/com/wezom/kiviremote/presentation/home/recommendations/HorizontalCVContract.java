package com.wezom.kiviremote.presentation.home.recommendations;

import com.wezom.kiviremote.net.model.RecommendItem;
import com.wezom.kiviremote.presentation.home.apps.AppModel;
import com.wezom.kiviremote.upnp.org.droidupnp.view.Port;

public class HorizontalCVContract {

    public interface HorizontalCVListener {

        void onPortChosen(Port port, int position);

        void onRecommendationChosen(RecommendItem item, int position);

        void appChosenNeedOpen(AppModel appModel, int positio);
    }
}