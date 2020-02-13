package com.kivi.remote.presentation.home.subscriptions.subs_tariff_plans

import com.kivi.remote.Screens
import com.kivi.remote.presentation.base.BaseViewModel
import ru.terrakok.cicerone.Router

class SubsTariffPlansViewModel(private val router: Router) : BaseViewModel() {

    fun navigateToSubsPayment() = router.navigateTo(Screens.SUBS_PAYMENT_FRAGMENT)

}