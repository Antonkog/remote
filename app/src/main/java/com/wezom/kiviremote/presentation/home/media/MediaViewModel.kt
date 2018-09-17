package com.wezom.kiviremote.presentation.home.media

import com.wezom.kiviremote.Screens
import com.wezom.kiviremote.presentation.base.BaseViewModel
import com.wezom.kiviremote.upnp.UPnPManager
import ru.terrakok.cicerone.Router

class MediaViewModel(private val router: Router, val manager: UPnPManager) : BaseViewModel() {

    fun addObservers() = manager.addObservers()

    fun removeObservers() = manager.removeObservers()

    fun browseTo(id: String, title: String?) = manager.browseTo(id, title)

    fun navigateToGallery() = router.navigateTo(Screens.GALLERY_FRAGMENT)

    fun navigateToDirectories() = router.navigateTo(Screens.DIRECTORIES_FRAGMENT)

    fun initContent() = manager.controller.serviceListener.initContent()
}