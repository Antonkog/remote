package com.kivi.remote.presentation.home.directories

import androidx.navigation.NavController
import com.kivi.remote.R
import com.kivi.remote.presentation.base.BaseViewModel
import com.kivi.remote.upnp.UPnPManager


class DirectoriesViewModel(private val navController: NavController, val uPnPManager: UPnPManager) :
    BaseViewModel() {

    fun navigateToGallery() =  navController.navigate(R.id.action_directoriesFragment_to_galleryFragment)

}