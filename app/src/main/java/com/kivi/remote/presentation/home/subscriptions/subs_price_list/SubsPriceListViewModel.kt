package com.kivi.remote.presentation.home.subscriptions.subs_price_list

import com.kivi.remote.Screens
import com.kivi.remote.presentation.base.BaseViewModel
import ru.terrakok.cicerone.Router

class SubsPriceListViewModel(private val router: Router) : BaseViewModel() {

    fun navigateToSubsInfo(data: PricePerTime) = router.navigateTo(Screens.SUBS_INFO_FRAGMENT, data)

}