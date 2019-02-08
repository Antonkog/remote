package com.wezom.kiviremote.presentation.base

import android.os.Handler
import android.view.KeyEvent
import android.view.MotionEvent.*
import android.view.View
import com.wezom.kiviremote.common.Constants.HOME_KEY_DELAY

abstract class TvKeysFragment : BaseFragment() {

    private var homeClickTime: Long = 0
    private val handler = Handler()

    fun setTvButtons(viewModel : TvKeysViewModel, menu : View, back : View, home : View ) {

        val launchQuickApps = { viewModel.launchQuickApps() }

        menu.setOnClickListener { _ -> viewModel.sendKeyEvent(KeyEvent.KEYCODE_MENU) }
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
