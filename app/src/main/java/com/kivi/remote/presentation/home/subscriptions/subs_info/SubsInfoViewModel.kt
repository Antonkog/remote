package com.kivi.remote.presentation.home.subscriptions.subs_info

import com.kivi.remote.Screens
import com.kivi.remote.presentation.base.BaseViewModel
import ru.terrakok.cicerone.Router

class SubsInfoViewModel(private val router: Router) : BaseViewModel() {

    fun navigateToSubsTariffs() = router.navigateTo(Screens.SUBS_TARIFF_PLANS_FRAGMENT)

}