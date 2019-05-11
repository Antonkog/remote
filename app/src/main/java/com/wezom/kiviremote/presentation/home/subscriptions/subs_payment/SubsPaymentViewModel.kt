package com.wezom.kiviremote.presentation.home.subscriptions.subs_payment

import com.wezom.kiviremote.Screens
import com.wezom.kiviremote.presentation.base.BaseViewModel
import ru.terrakok.cicerone.Router

class SubsPaymentViewModel(private val router: Router) : BaseViewModel() {

    fun navigateToSubsTariffs() = router.navigateTo(Screens.SUBS_TARIFF_PLANS_FRAGMENT)

}