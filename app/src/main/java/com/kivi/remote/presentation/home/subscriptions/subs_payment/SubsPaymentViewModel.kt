package com.kivi.remote.presentation.home.subscriptions.subs_payment

import com.kivi.remote.Screens
import com.kivi.remote.presentation.base.BaseViewModel
import ru.terrakok.cicerone.Router

class SubsPaymentViewModel(private val router: Router) : BaseViewModel() {

    fun navigateToSubsTariffs() = router.navigateTo(Screens.SUBS_TARIFF_PLANS_FRAGMENT)

}