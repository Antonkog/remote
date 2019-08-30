package com.wezom.kiviremote.presentation.home.recommendations.deep

import com.wezom.kiviremote.net.model.Recommendation
import java.io.Serializable


data class RecommendationsHolder(val data: List<Recommendation>) : Serializable