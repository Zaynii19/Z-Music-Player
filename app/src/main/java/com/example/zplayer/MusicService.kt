package com.example.zplayer

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat

//AudioManager.OnAudioFocusChangeListener
//when calls incoming, stops music
class MusicService : Service(), AudioManager.OnAudioFocusChangeListener {
    private var myBinder = MyBinder()
    var mediaPlayer :MediaPlayer? = null
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var runnable: Runnable
    lateinit var audioManager: AudioManager

    override fun onBind(intent: Intent?): IBinder {
        mediaSession = MediaSessionCompat(baseContext, "My Music")
        return myBinder
    }

    inner class MyBinder : Binder(){
        fun currentService():MusicService{
            return this@MusicService
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    fun showNotification(playPauseBtn : Int, playbackSpeed: Float){

        //handle when user click on noti direct to song
        val intent = Intent(baseContext, HomeActivity::class.java)
        val contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val prevIntent = Intent(baseContext, BroadcastNotiReceiver::class.java).setAction(ApplicationClass.PREVIOUS)
        val prevPendingIntent = PendingIntent.getBroadcast(baseContext, 0, prevIntent, PendingIntent.FLAG_IMMUTABLE)

        val playIntent = Intent(baseContext, BroadcastNotiReceiver::class.java).setAction(ApplicationClass.PLAY)
        val playPendingIntent = PendingIntent.getBroadcast(baseContext, 0, playIntent, PendingIntent.FLAG_IMMUTABLE)

        val nextIntent = Intent(baseContext, BroadcastNotiReceiver::class.java).setAction(ApplicationClass.NEXT)
        val nextPendingIntent = PendingIntent.getBroadcast(baseContext, 0, nextIntent, PendingIntent.FLAG_IMMUTABLE)

        val exitIntent = Intent(baseContext, BroadcastNotiReceiver::class.java).setAction(ApplicationClass.EXIT)
        val exitPendingIntent = PendingIntent.getBroadcast(baseContext, 0, exitIntent, PendingIntent.FLAG_IMMUTABLE)

        val imageArt = getImageArt(SongActivity.songListSA[SongActivity.songIndex].path)
        val currentSongImage = if (imageArt != null){
            BitmapFactory.decodeByteArray(imageArt, 0, imageArt.size)
        }else{
            BitmapFactory.decodeResource(resources, R.drawable.music_player)
        }

        // create notification
        val notification = NotificationCompat.Builder(baseContext, ApplicationClass.CHANNEL_ID)
            .setContentIntent(contentIntent)
            .setContentTitle(SongActivity.songListSA[SongActivity.songIndex].title)
            .setContentText(SongActivity.songListSA[SongActivity.songIndex].artist)
            .setSmallIcon(R.drawable.music_icon)
            .setLargeIcon(currentSongImage)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(mediaSession.sessionToken))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .addAction(R.drawable.noti_previous, "Previous", prevPendingIntent)
            .addAction(playPauseBtn, "Play", playPendingIntent)
            .addAction(R.drawable.noti_next, "Next", nextPendingIntent)
            .addAction(R.drawable.noti_exit, "Exit", exitPendingIntent)
            .build()

        //for moving notification seekbar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            mediaSession.setMetadata(MediaMetadataCompat.Builder()
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, mediaPlayer!!.duration.toLong())
                .build())
            mediaSession.setPlaybackState(PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PLAYING, mediaPlayer!!.currentPosition.toLong(), playbackSpeed)
                .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
                .build())
        }

        startForeground(19, notification)

    }

    fun createMediaPlayer(){
        try{
            if (SongActivity.musicService!!.mediaPlayer == null){
                SongActivity.musicService!!.mediaPlayer = MediaPlayer()
            }
            SongActivity.musicService!!.mediaPlayer!!.reset()
            SongActivity.musicService!!.mediaPlayer!!.setDataSource(SongActivity.songListSA[SongActivity.songIndex].path)
            SongActivity.musicService!!.mediaPlayer!!.prepare()
            SongActivity.binding.pauseSong.setIconResource(R.drawable.pause)
            SongActivity.musicService!!.showNotification(R.drawable.noti_pause, 0f)
            SongActivity.binding.seekbar.progress = 0
            SongActivity.binding.seekbar.max = mediaPlayer!!.duration
            SongActivity.binding.songTotalLength.text = SongActivity.songListSA[SongActivity.songIndex].formattedDuration
            SongActivity.binding.songStartLength.text = formatSongDuration(mediaPlayer!!.currentPosition.toLong())

            SongActivity.nowPlayedId = SongActivity.songListSA[SongActivity.songIndex].id

        }catch (e:Exception){
            return
        }
    }

    //change the song start duration and seekbar with time
    fun seekbarSetup(){
        runnable = Runnable {
            SongActivity.binding.songStartLength.text = formatSongDuration(mediaPlayer!!.currentPosition.toLong())
            SongActivity.binding.seekbar.progress = mediaPlayer!!.currentPosition
            Handler(Looper.getMainLooper()).postDelayed(runnable, 200)
        }
        //Ensure the start of inner handler
        Handler(Looper.getMainLooper()).postDelayed(runnable, 0)
    }

    override fun onAudioFocusChange(focusChange: Int) {
        if (focusChange <= 0){
            //Pause music
            SongActivity.binding.pauseSong.setIconResource(R.drawable.play)
            NowPlayingFragment.binding.playPauseBtn.setImageResource(R.drawable.play)
            showNotification(R.drawable.noti_play, 0f)
            NowPlayingFragment.binding.playPauseBtn.setImageResource(R.drawable.play)
            SongActivity.musicService!!.mediaPlayer!!.pause()
            SongActivity.isPlaying = false


        }else{
            //play music
            SongActivity.binding.pauseSong.setIconResource(R.drawable.pause)
            NowPlayingFragment.binding.playPauseBtn.setImageResource(R.drawable.pause)
            showNotification(R.drawable.noti_pause, 1f)
            NowPlayingFragment.binding.playPauseBtn.setImageResource(R.drawable.pause)
            SongActivity.musicService!!.mediaPlayer!!.start()
            SongActivity.isPlaying = true
        }
    }

}