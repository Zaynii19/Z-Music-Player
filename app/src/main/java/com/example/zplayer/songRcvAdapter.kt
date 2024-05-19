package com.example.zplayer

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.zplayer.databinding.SongViewItemBinding

class songRcvAdapter(private val context:Context, private val musicList: MutableList<SongsLists>):RecyclerView.Adapter<songRcvAdapter.MyViewHolder>() {
    class MyViewHolder(binding:SongViewItemBinding):RecyclerView.ViewHolder(binding.root) {
        val title = binding.songTitle
        val album = binding.songAlbum
        val duration = binding.songDuration
        val img = binding.songImg
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

    }
}