package com.wezom.kiviremote.bus;


import com.wezom.kiviremote.net.model.Recommendation;

public class LaunchRecommendationEvent {
Recommendation recommendation;

    public LaunchRecommendationEvent(Recommendation recommendation) {
        this.recommendation = recommendation;
    }

    public Recommendation getRecommendation() {
        return recommendation;
    }
}
