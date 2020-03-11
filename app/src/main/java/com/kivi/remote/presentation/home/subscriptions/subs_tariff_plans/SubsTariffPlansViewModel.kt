package com.kivi.remote.presentation.home.subscriptions.subs_tariff_plans

import androidx.navigation.NavController
import com.kivi.remote.R
import com.kivi.remote.presentation.base.BaseViewModel

class SubsTariffPlansViewModel(private val navController: NavController) : BaseViewModel() {
    fun navigateToSubsPayment() = navController.navigate(R.id.action_subsTariffPlansFragment_to_subsPaymentFragment)
}