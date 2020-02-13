package com.kivi.remote.presentation.home.directories

import com.kivi.remote.Screens.GALLERY_FRAGMENT
import com.kivi.remote.presentation.base.BaseViewModel
import com.kivi.remote.upnp.UPnPManager
import ru.terrakok.cicerone.Router


class DirectoriesViewModel(private val router: Router, val uPnPManager: UPnPManager) :
    BaseViewModel() {

    fun navigateToGallery() = router.navigateTo(GALLERY_FRAGMENT)
}