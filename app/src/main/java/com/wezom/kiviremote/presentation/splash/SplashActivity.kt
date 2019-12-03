package com.wezom.kiviremote.presentation.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.wezom.kiviremote.presentation.home.HomeActivity


class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}