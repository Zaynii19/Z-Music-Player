@file:Suppress("DEPRECATION")

package com.example.zplayer

import android.annotation.SuppressLint
import android.media.MediaMetadataRetriever
import kotlin.system.exitProcess

//Data class for songs
data class SongsLists(
    val id: String,
    val title: String,
    val album: String,
    val artist: String,
    val path: String,
    val formattedDuration: String,
    val artUri:String
)

class Playlist{
    lateinit var name: String
    lateinit var playList:MutableList<SongsLists>
    lateinit var createdBy:String
    lateinit var createdOn:String
}

class MusicPlaylist{
    var ref: MutableList<Playlist> = mutableListOf()
}

//Set notification image to current song image
fun getImageArt(path: String): ByteArray? {
    val retriever = MediaMetadataRetriever()
    retriever.setDataSource(path)
    return retriever.embeddedPicture
}

fun setSongPosition(next: Boolean){
    if (!SongActivity.repeat){
        if (next){
            if (SongActivity.songListSA.size - 1 == SongActivity.songIndex){
                SongActivity.songIndex = 0
            }else{
                ++SongActivity.songIndex
            }
        }else{
            if (SongActivity.songIndex == 0){
                SongActivity.songIndex = SongActivity.songListSA.size - 1
            }else{
                --SongActivity.songIndex
            }
        }
    }
}

@SuppressLint("DefaultLocale")
fun formatSongDuration(duration: Long): String {
    val minutes = (duration / 1000 / 60).toInt()
    val seconds = (duration / 1000 % 60).toInt()
    return String.format("%02d:%02d", minutes, seconds)
}

fun terminateApp(){
    if (SongActivity.musicService != null) {
        SongActivity.musicService!!.stopForeground(true)
        SongActivity.musicService!!.mediaPlayer!!.release()
        SongActivity.musicService = null
    }
    exitProcess(1)
}

fun favChecker(id: String): Int {
    FavActivity.songListFA.forEachIndexed { index, song ->
        SongActivity.isFav = false
        //checks on click fav button the current song is already present in fav songs
        if (id == song.id){
            SongActivity.isFav = true
            return index
        }
    }

    return -1
}
