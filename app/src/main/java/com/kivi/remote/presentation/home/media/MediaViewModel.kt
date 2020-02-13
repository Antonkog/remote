package com.kivi.remote.presentation.home.media

import com.kivi.remote.Screens
import com.kivi.remote.presentation.base.BaseViewModel
import com.kivi.remote.upnp.UPnPManager
import ru.terrakok.cicerone.Router

class MediaViewModel(private val router: Router, val manager: UPnPManager) : BaseViewModel() {

    fun addObservers() = manager.addObservers()

    fun removeObservers() = manager.removeObservers()

    fun browseTo(id: String, title: String?) = manager.browseTo(id, title)

    fun navigateToGallery() = router.navigateTo(Screens.GALLERY_FRAGMENT)

    fun navigateToDirectories() = router.navigateTo(Screens.DIRECTORIES_FRAGMENT)

    fun initContent() = manager.controller.serviceListener.initContent()
}