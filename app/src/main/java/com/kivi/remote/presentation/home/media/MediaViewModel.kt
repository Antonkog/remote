package com.kivi.remote.presentation.home.media

import androidx.navigation.NavController
import com.kivi.remote.R
import com.kivi.remote.presentation.base.BaseViewModel
import com.kivi.remote.upnp.UPnPManager

class MediaViewModel(private val navController: NavController, val manager: UPnPManager) : BaseViewModel() {

    fun addObservers() = manager.addObservers()

    fun removeObservers() = manager.removeObservers()

    fun browseTo(id: String, title: String?) = manager.browseTo(id, title)

//    fun navigateToGallery() = router.navigateTo(Screens.GALLERY_FRAGMENT)

    fun navigateToDirectories() = navController.navigate(R.id.action_mediaFragment_to_directoriesFragment)

    fun initContent() = manager.controller.serviceListener.initContent()
}