package com.wezom.kiviremote.presentation.home.recommendations;

import com.wezom.kiviremote.net.model.Channel;
import com.wezom.kiviremote.net.model.Input;
import com.wezom.kiviremote.net.model.Recommendation;
import com.wezom.kiviremote.net.model.ServerAppInfo;

public class HorizontalCVContract {

    public interface HorizontalCVListener {

        void onInputChosen(Input input, int position);

        void onRecommendationChosen(Recommendation item, int position);

        void onChannelChosen(Channel item, int position);

        void appChosenNeedOpen(ServerAppInfo appModel, int position);
    }
}