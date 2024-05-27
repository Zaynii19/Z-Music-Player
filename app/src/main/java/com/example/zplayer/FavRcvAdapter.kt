package com.example.zplayer

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.zplayer.databinding.FavViewItemBinding

class FavRcvAdapter(private val context: Context, private var favSongList: MutableList<SongsLists>) : RecyclerView.Adapter<FavRcvAdapter.MyViewHolder>() {
    class MyViewHolder(binding : FavViewItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val image = binding.songIMG
        val name = binding.songName
        val root = binding.root
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(FavViewItemBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun getItemCount(): Int {
        return favSongList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.name.text = favSongList[position].title
        Glide.with(context)
            .load(favSongList[position].artUri)
            .apply(RequestOptions().placeholder(R.drawable.music_player))
            .into(holder.image)

        holder.root.setOnClickListener {
            val intent = Intent(context, SongActivity::class.java)
            intent.putExtra("index", position)
            intent.putExtra("class", "FavAdapter")
            ContextCompat.startActivity(context, intent, null)
        }
    }

    //update song list after search
    fun updateMusicList(favList: MutableList<SongsLists>){
        favSongList = mutableListOf()
        favSongList.addAll(favList)
        notifyDataSetChanged()
    }

}