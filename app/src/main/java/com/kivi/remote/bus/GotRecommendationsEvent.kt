package com.kivi.remote.bus

import com.kivi.remote.net.model.Recommendation


class GotRecommendationsEvent(var recommendations: List<Recommendation>?)