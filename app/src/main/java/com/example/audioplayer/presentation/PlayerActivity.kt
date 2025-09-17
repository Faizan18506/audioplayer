package com.example.audioplayer.presentation

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.audioplayer.R
import com.example.audioplayer.data.Song
import com.example.audioplayer.ui.theme.PlayerScreen
// Adjust the path if needed

class PlayerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
      val mySongList = intent.getParcelableArrayListExtra<Song>("songList") ?: arrayListOf()
        val initialIndex=intent.getIntExtra("position",0)
        setContent {
            PlayerScreen(
                songList =mySongList,
                initialIndex = initialIndex,
                onBack = { finish() }
            )
        }
    }
}