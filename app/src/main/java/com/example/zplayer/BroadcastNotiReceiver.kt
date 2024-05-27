package com.example.zplayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlin.system.exitProcess

class BroadcastNotiReceiver : BroadcastReceiver(){
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action){
            ApplicationClass.PREVIOUS -> prevNextSong(false, context!!)

            ApplicationClass.PLAY -> {
                if (SongActivity.isPlaying) pauseMusic()
                else playMusic()
            }

            ApplicationClass.NEXT -> prevNextSong(true, context!!)

            ApplicationClass.EXIT -> {
                terminateApp()
            }
        }
    }

    private fun playMusic(){
        SongActivity.isPlaying = true
        SongActivity.musicService!!.mediaPlayer!!.start()
        SongActivity.musicService!!.showNotification(R.drawable.noti_pause)
        SongActivity.binding.pauseSong.setIconResource(R.drawable.pause )
        NowPlayingFragment.binding.playPauseBtn.setImageResource(R.drawable.pause)
    }

    private fun pauseMusic(){
        SongActivity.isPlaying = false
        SongActivity.musicService!!.mediaPlayer!!.pause()
        SongActivity.musicService!!.showNotification(R.drawable.noti_play)
        SongActivity.binding.pauseSong.setIconResource(R.drawable.play )
        NowPlayingFragment.binding.playPauseBtn.setImageResource(R.drawable.play)
    }

    private fun prevNextSong(thisNext : Boolean, context: Context?){
        setSongPosition(next = thisNext)
        SongActivity.musicService!!.createMediaPlayer()
        //change the layout of Song Activity
        Glide.with(context!!)
            .load(SongActivity.songListSA[SongActivity.songIndex].artUri)
            .apply(RequestOptions().placeholder(R.drawable.music_player))  //if error in loading song album pic or have no pic
            .into(SongActivity.binding.songPic)

        SongActivity.binding.songName.text = SongActivity.songListSA[SongActivity.songIndex].title
        SongActivity.binding.songName.setSelected(true)
        SongActivity.binding.songTotalLength.text = SongActivity.songListSA[SongActivity.songIndex].formattedDuration
        playMusic()

        //change the layout of Now Playing Fragment
        Glide.with(context)
            .load(SongActivity.songListSA[SongActivity.songIndex].artUri)
            .apply(RequestOptions().placeholder(R.drawable.music_player))  //if error in loading song album pic or have no pic
            .into(NowPlayingFragment.binding.songPicF)

        NowPlayingFragment.binding.songNameF.text = SongActivity.songListSA[SongActivity.songIndex].title
        NowPlayingFragment.binding.songNameF.setSelected(true)

        //set filled heart if next song is fav
        SongActivity.fSongIndex = favChecker(SongActivity.songListSA[SongActivity.songIndex].id)
        if (SongActivity.isFav){
            SongActivity.binding.favBtn.setImageResource(R.drawable.fav)
        }else{
            SongActivity.binding.favBtn.setImageResource(R.drawable.empty_fav)
        }
    }
}