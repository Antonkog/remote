package com.kivi.remote.presentation.home.subscriptions.subs_price_list

import androidx.navigation.NavController
import com.kivi.remote.presentation.base.BaseViewModel

class SubsPriceListViewModel(private val navController: NavController) : BaseViewModel() {

    fun navigateToSubsInfo(data: PricePerTime) =  navController.navigate(SubsPriceListFragmentDirections.actionSubsPriceListFragmentToSubsInfoFragment(data))//navController.navigate(R.id.action_subsPriceListFragment_to_subsInfoFragment)

}