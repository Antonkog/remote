package com.wezom.kiviremote.bus

import com.wezom.kiviremote.net.model.Channel
import com.wezom.kiviremote.net.model.Recommendation


class GotRecommendationsEvent(var recommendations: List<Recommendation>?)