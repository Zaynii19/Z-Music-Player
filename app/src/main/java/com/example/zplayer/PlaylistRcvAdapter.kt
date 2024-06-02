package com.example.zplayer

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.zplayer.databinding.PlaylistItemListBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.GsonBuilder

class PlaylistRcvAdapter(private val context: Context, private var playlistList: MutableList<Playlist>): RecyclerView.Adapter<PlaylistRcvAdapter.MyViewHolder>() {
    class MyViewHolder(binding : PlaylistItemListBinding) : RecyclerView.ViewHolder(binding.root) {
        val image = binding.songPic
        val name = binding.playListName
        val root = binding.root
        val delete = binding.delBtn
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(PlaylistItemListBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun getItemCount(): Int {
        return playlistList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.name.isSelected = true
        holder.name.text = playlistList[position].name

        holder.delete.setOnClickListener {
            val builder = MaterialAlertDialogBuilder(context)
            builder.setTitle(playlistList[position].name)
                .setMessage("Do you want to delete the playlist?")
                .setPositiveButton("Yes") { dialog, _ ->
                    PlaylistActivity.musicPlaylist.ref.removeAt(position)
                    refreshPlaylist()
                    savePlaylistsToPreferences()
                    dialog.dismiss()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }

            val exitDialog = builder.create()
            exitDialog.show()
            exitDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
            exitDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
        }

        if (PlaylistActivity.musicPlaylist.ref[position].playList.isNotEmpty()) {
            Glide.with(context)
                .load(PlaylistActivity.musicPlaylist.ref[position].playList[0].artUri)
                .apply(RequestOptions().placeholder(R.drawable.music_pic))
                .into(holder.image)
        }

        holder.root.setOnClickListener {
            val intent = Intent(context, PlaylistDetailsActivity::class.java)
            intent.putExtra("index", position)
            ContextCompat.startActivity(context, intent, null)
        }
    }

    fun refreshPlaylist() {
        playlistList = mutableListOf()
        playlistList.addAll(PlaylistActivity.musicPlaylist.ref)
        notifyDataSetChanged()
    }

    private fun savePlaylistsToPreferences() {
        val sharedPreferences = context.getSharedPreferences("FAVSONG", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val playlistJsonString = GsonBuilder().create().toJson(PlaylistActivity.musicPlaylist)
        editor.putString("Playlist", playlistJsonString)
        editor.apply()
    }
}

