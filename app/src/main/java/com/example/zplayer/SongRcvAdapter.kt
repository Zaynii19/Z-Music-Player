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

class SongRcvAdapter(private val context: Context, private var musicList: MutableList<SongsLists>) :
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

        holder.root.setOnClickListener {
            when {
                HomeActivity.search -> sendIntent("SongRcvAdapterSearch", position)
                else -> sendIntent("SongRcvAdapter", position)
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
}
