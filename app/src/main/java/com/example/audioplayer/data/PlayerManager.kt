package com.example.audioplayer.data

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object PlayerManager {
    private var exoPlayer: ExoPlayer? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong

    private var currentList: List<Song> = emptyList()

    fun ensurePlayer(context: Context): ExoPlayer {
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(context.applicationContext).build().also { player ->
                player.addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        _isPlaying.value = isPlaying
                    }

                    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                        _currentSong.value = currentList.getOrNull(player.currentMediaItemIndex)
                        _currentIndex.value = player.currentMediaItemIndex
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        if (playbackState == Player.STATE_ENDED && currentList.isNotEmpty()) {
                            next()
                        }
                    }
                })
            }
        }
        return exoPlayer!!
    }

    fun setQueueAndPlay(context: Context, songs: List<Song>, index: Int) {
        val player = ensurePlayer(context)
        currentList = songs
        player.setMediaItems(songs.map { MediaItem.fromUri(it.data) }, index, 0)
        player.prepare()
        player.playWhenReady = true
        _currentSong.value = songs.getOrNull(index)
        _currentIndex.value = index
        startService(context)
    }

    fun play() { exoPlayer?.playWhenReady = true }
    fun pause() { exoPlayer?.pause() }
    fun togglePlayPause() { if (exoPlayer?.isPlaying == true) pause() else play() }
    fun seekTo(posMs: Long) { exoPlayer?.seekTo(posMs) }
    fun next() { exoPlayer?.let { it.seekToNextMediaItem(); if (!it.isPlaying) it.play() } }
    fun previous() { exoPlayer?.let { it.seekToPreviousMediaItem(); if (!it.isPlaying) it.play() } }

    fun getDuration(): Long = exoPlayer?.duration ?: 0L
    fun getPosition(): Long = exoPlayer?.currentPosition ?: 0L

    // Intentionally do not release on Activity/Composable dispose to keep playback alive.
    fun release() { exoPlayer?.release(); exoPlayer = null }

    private fun startService(context: Context) {
        try {
            val intent = Intent(context, com.example.audioplayer.service.AudioPlayerService::class.java)
            ContextCompat.startForegroundService(context, intent)
        } catch (_: Throwable) { }
    }
}


