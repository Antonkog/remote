package com.kivi.remote.presentation.home.recommendations.deep

import com.kivi.remote.net.model.Recommendation
import java.io.Serializable


data class RecommendationsHolder(val data: List<Recommendation>) : Serializable