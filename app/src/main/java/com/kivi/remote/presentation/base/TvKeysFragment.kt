package com.kivi.remote.presentation.base

import android.os.Handler
import android.view.KeyEvent
import android.view.MotionEvent.*
import android.view.View
import com.kivi.remote.common.Constants.HOME_KEY_DELAY

abstract class TvKeysFragment : BaseFragment() {

    private var homeClickTime: Long = 0
    private val handler = Handler()

    fun setTvButtons(viewModel : TvKeysViewModel, aspectMenu : View, back : View, home : View ) {

        val launchQuickApps = { viewModel.launchQuickApps() }

        aspectMenu.setOnClickListener { _ -> viewModel.showHideAspect() }
        back.setOnClickListener { _-> viewModel.sendKeyEvent(KeyEvent.KEYCODE_BACK) }
        home.setOnTouchListener { view, event ->
            when (event.action) {
                ACTION_DOWN -> {
                    view.isPressed = true
                    viewModel.sendHomeDown()
                    homeClickTime = System.currentTimeMillis()
                    handler.postDelayed(launchQuickApps, 340)
                }

                ACTION_MOVE -> view.isPressed = true

                ACTION_UP -> {
                    view.performClick()
                    view.isPressed = false
                    if (System.currentTimeMillis() - homeClickTime < HOME_KEY_DELAY) {
                        viewModel.sendHomeUp()
                        handler.removeCallbacks(launchQuickApps)
                    }
                }

                else -> view.isPressed = false
            }

            true
        }
    }
}
