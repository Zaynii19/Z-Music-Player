package com.example.zplayer

import android.media.MediaMetadataRetriever

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

//Set notification image to current song image
fun getImageArt(path: String): ByteArray? {
    val retriever = MediaMetadataRetriever()
    retriever.setDataSource(path)
    return retriever.embeddedPicture
}

fun setSongPosition(next: Boolean){
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