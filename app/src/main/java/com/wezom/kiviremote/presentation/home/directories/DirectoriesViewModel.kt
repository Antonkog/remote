package com.wezom.kiviremote.presentation.home.directories

import com.wezom.kiviremote.Screens.GALLERY_FRAGMENT
import com.wezom.kiviremote.presentation.base.BaseViewModel
import com.wezom.kiviremote.upnp.UPnPManager
import ru.terrakok.cicerone.Router


class DirectoriesViewModel(private val router: Router, val uPnPManager: UPnPManager) :
    BaseViewModel() {

    fun navigateToGallery() = router.navigateTo(GALLERY_FRAGMENT)
}