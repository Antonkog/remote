package com.kivi.remote.presentation.home.recommendations;

import com.kivi.remote.net.model.Channel;
import com.kivi.remote.net.model.Input;
import com.kivi.remote.net.model.Recommendation;
import com.kivi.remote.net.model.ServerAppInfo;

public class HorizontalCVContract {

    public interface HorizontalCVListener {

        void onInputChosen(Input input, int position);

        void onRecommendationChosen(Recommendation item, int position);

        void onChannelChosen(Channel item, int position);

        void appChosenNeedOpen(ServerAppInfo appModel, int position);
    }
}