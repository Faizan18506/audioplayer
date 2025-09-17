package com.example.audioplayer.presentation

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.audioplayer.ui.theme.AudioplayerTheme
import com.example.audioplayer.ui.theme.SongListScreen

class SongListActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SongListScreen { songs, position ->
                val intent = Intent(this, PlayerActivity::class.java)
                intent.putParcelableArrayListExtra("songList", ArrayList(songs))
                intent.putExtra("position", position)
                startActivity(intent)
            }
        }
    }
}