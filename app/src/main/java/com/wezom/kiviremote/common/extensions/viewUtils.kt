package com.wezom.kiviremote.common.extensions

import android.view.View
import android.view.ViewGroup
import com.wezom.kiviremote.R


fun View.vanish() {
    this.visibility = View.GONE
}


fun View.changeBackgroundRecurcevly() {
    if (this is ViewGroup)
        for (counter in 0..childCount) {
            this.getChildAt(counter).changeBackgroundRecurcevly()
        }
    else {
        this.background = this.resources.getDrawable(R.drawable.shape_gradient_black)
    }

}