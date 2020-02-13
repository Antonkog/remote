package com.kivi.remote.bus;


import com.kivi.remote.net.model.Recommendation;

public class LaunchRecommendationEvent {
Recommendation recommendation;

    public LaunchRecommendationEvent(Recommendation recommendation) {
        this.recommendation = recommendation;
    }

    public Recommendation getRecommendation() {
        return recommendation;
    }
}
