package com.wezom.kiviremote.presentation.home.subscriptions.subs_tariff_plans

import com.wezom.kiviremote.Screens
import com.wezom.kiviremote.presentation.base.BaseViewModel
import ru.terrakok.cicerone.Router

class SubsTariffPlansViewModel(private val router: Router) : BaseViewModel() {

    fun navigateToSubsPayment() = router.navigateTo(Screens.SUBS_PAYMENT_FRAGMENT)

}