package com.example.zplayer

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