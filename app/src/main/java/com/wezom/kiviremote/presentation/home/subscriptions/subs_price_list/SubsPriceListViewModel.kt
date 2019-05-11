package com.wezom.kiviremote.presentation.home.subscriptions.subs_price_list

import com.wezom.kiviremote.Screens
import com.wezom.kiviremote.presentation.base.BaseViewModel
import ru.terrakok.cicerone.Router

class SubsPriceListViewModel(private val router: Router) : BaseViewModel() {

    fun navigateToSubsInfo(data: PricePerTime) = router.navigateTo(Screens.SUBS_INFO_FRAGMENT, data)

}