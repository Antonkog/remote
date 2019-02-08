package com.wezom.kiviremote.common.extensions

import android.app.Activity
import android.os.Handler

class Run {
    companion object {
        fun after(delay: Long, process: () -> Unit) {
            Handler().postDelayed({
                process()
            }, delay)
        }

        fun afterOnMain(delay: Long, activity: Activity, process: () -> Unit) {
            Handler().postDelayed({
                activity.runOnUiThread({
                    Runnable {
                        process()
                    }
                })
            }, delay)
        }
    }
}