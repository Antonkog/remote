package com.wezom.kiviremote.presentation.home.tvsettings

import com.wezom.kiviremote.Screens
import com.wezom.kiviremote.presentation.base.BaseViewModel
import ru.terrakok.cicerone.Router


class TvSettingsViewModel(private val router: Router) : BaseViewModel() {

    fun navigateToHome() = router.backTo(Screens.DEVICE_SEARCH_FRAGMENT)

    fun navigateBack() = router.exit()
}