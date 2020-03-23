package com.kivi.remote.presentation.home.tutorial

import android.content.SharedPreferences
import androidx.navigation.NavController
import com.kivi.remote.common.Constants
import com.kivi.remote.common.extensions.boolean
import com.kivi.remote.presentation.base.BaseViewModel

//
// Created by Antonio on 3/23/20.
// email: akogan777@gmail.com
//

class TutorialViewModel  (
private val navController: NavController,
private val preferences: SharedPreferences
) : BaseViewModel() {

    private var tutorialDone by preferences.boolean(false, key = Constants.TUTORIAL_DONE)

    init {

    }

    fun navigate (id : Int) = navController.navigate(id)

    fun tutorialIsDone (){
        tutorialDone = true
    }
}