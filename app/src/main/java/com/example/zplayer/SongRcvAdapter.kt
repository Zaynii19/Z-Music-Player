package com.example.zplayer

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.zplayer.databinding.SongViewItemBinding

class SongRcvAdapter(private val context: Context, private var musicList: MutableList<SongsLists>, private var playlistDetails: Boolean = false, private val selection: Boolean = false) :
    RecyclerView.Adapter<SongRcvAdapter.MyViewHolder>() {

    class MyViewHolder(binding: SongViewItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val title = binding.songTitle
        val album = binding.songAlbum
        val duration = binding.songDuration
        val img = binding.songImg
        val root = binding.root
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(SongViewItemBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun getItemCount(): Int {
        return musicList.size
    }



    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.title.text = musicList[position].title
        holder.album.text = musicList[position].album
        holder.duration.text = musicList[position].formattedDuration
        // Set song album image using Glide
        Glide.with(context)
            .load(musicList[position].artUri)
            .apply(RequestOptions().placeholder(R.drawable.music_player))  // Placeholder image
            .into(holder.img)

        when{
            playlistDetails -> {
                holder.root.setOnClickListener {
                    sendIntent("PlaylistDetailsAdapter", position)
                }
            }

            selection -> {
                holder.root.setOnClickListener {
                    if (addRemoveSong(musicList[position]))
                        holder.root.setBackgroundColor(ContextCompat.getColor(context, R.color.cool_pink ))
                    else
                        holder.root.setBackgroundColor(ContextCompat.getColor(context, R.color.white ))
                }
            }

            else -> {
                holder.root.setOnClickListener {
                    when {
                        HomeActivity.search -> sendIntent("SongRcvAdapterSearch", position)
                        //when same song is played again
                        musicList[position].id == SongActivity.nowPlayedId -> {
                            sendIntent("NowPlaying", SongActivity.songIndex)
                        }
                        else -> sendIntent("SongRcvAdapter", position)
                    }
                }
            }
        }
    }

    //update song list after search
    fun updateMusicList(searchList: MutableList<SongsLists>){
        musicList = mutableListOf()
        musicList.addAll(searchList)
        notifyDataSetChanged()
    }

    private fun sendIntent(reference: String, pos: Int){
        val intent = Intent(context, SongActivity::class.java)
        intent.putExtra("index", pos)
        intent.putExtra("class", reference)
        ContextCompat.startActivity(context, intent, null)
    }

    //add and remove the songs from playlist in selection activity
    private fun addRemoveSong(song: SongsLists): Boolean {
        PlaylistActivity.musicPlaylist.ref[PlaylistDetailsActivity.currentPlaylistPos].playList.forEachIndexed{index, music ->
            if (song.id == music.id){
                PlaylistActivity.musicPlaylist.ref[PlaylistDetailsActivity.currentPlaylistPos].playList.removeAt(index)
                return false
            }
        }

        PlaylistActivity.musicPlaylist.ref[PlaylistDetailsActivity.currentPlaylistPos].playList.add(song)
        return true
    }

    fun refreshPlaylist(){
        musicList = mutableListOf()
        musicList = PlaylistActivity.musicPlaylist.ref[PlaylistDetailsActivity.currentPlaylistPos].playList
        notifyDataSetChanged()
    }
    }
