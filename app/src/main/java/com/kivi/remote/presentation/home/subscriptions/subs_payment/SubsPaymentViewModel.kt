package com.kivi.remote.presentation.home.subscriptions.subs_payment

import androidx.navigation.NavController
import com.kivi.remote.R
import com.kivi.remote.presentation.base.BaseViewModel

class SubsPaymentViewModel(private val navController: NavController) : BaseViewModel() {

    fun navigateToSubsTariffs() = navController.navigate(R.id.action_subsPaymentFragment_to_subsTariffPlansFragment)

}