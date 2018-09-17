package com.wezom.kiviremote.common.extensions

import android.view.View


fun View.vanish() {
    this.visibility = View.GONE
}