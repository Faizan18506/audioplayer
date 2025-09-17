package com.example.audioplayer.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.example.audioplayer.ui.theme.SplashScreen

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SplashScreen(
                onStartClick = {
                   startActivity(Intent(this@SplashActivity, SongListActivity::class.java))
                    finish()
                }
            )
        }
    }
}