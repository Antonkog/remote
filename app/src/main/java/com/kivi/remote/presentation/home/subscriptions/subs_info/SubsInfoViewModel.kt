package com.kivi.remote.presentation.home.subscriptions.subs_info

import androidx.navigation.NavController
import com.kivi.remote.R
import com.kivi.remote.presentation.base.BaseViewModel

class SubsInfoViewModel(private val navController: NavController) : BaseViewModel() {

    fun navigateToSubsTariffs() = navController.navigate(R.id.action_subsInfoFragment_to_subsTariffPlansFragment)

}