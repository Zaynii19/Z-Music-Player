package com.example.zplayer

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.zplayer.databinding.ActivityPlaylistDetailsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.GsonBuilder

@Suppress("DEPRECATION")
class PlaylistDetailsActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityPlaylistDetailsBinding.inflate(layoutInflater)
    }

    lateinit var adapter: SongRcvAdapter

    companion object{
        var currentPlaylistPos: Int = -1
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Setting Theme
        setTheme(HomeActivity.currentTheme[HomeActivity.themeIndex])

        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        currentPlaylistPos = intent.extras?.getInt("index", -1) ?: -1

        if (currentPlaylistPos == -1) {
            Toast.makeText(this, "Playlist not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        //update song lists
        PlaylistActivity.musicPlaylist.ref[currentPlaylistPos].playList = checkPlaylist(PlaylistActivity.musicPlaylist.ref[currentPlaylistPos].playList)

        binding.back.setOnClickListener {
            finish()
        }

        binding.shuffleBtn.setOnClickListener {
            intent = Intent(this, SongActivity::class.java)
            intent.putExtra("index", 0)
            intent.putExtra("class", "PlaylistDetailsShuffle")
            startActivity(intent)
        }

        binding.rcv.setHasFixedSize(true)
        binding.rcv.setItemViewCacheSize(13)
        binding.rcv.layoutManager = LinearLayoutManager(this)
        adapter = SongRcvAdapter(this, PlaylistActivity.musicPlaylist.ref[currentPlaylistPos].playList, true)
        binding.rcv.adapter = adapter

        binding.addBtn.setOnClickListener {
            intent = Intent(this, SelectionActivity::class.java)
            startActivity(intent)
        }

        binding.removeBtn.setOnClickListener {
            val builder = MaterialAlertDialogBuilder(this)
            builder.setTitle("Remove Playlist")
                .setMessage("Do you want to Remove Playlist?")
                .setPositiveButton("Yes"){ dialog, _ ->
                    PlaylistActivity.musicPlaylist.ref[currentPlaylistPos].playList.clear()
                    adapter.refreshPlaylist()
                    dialog.dismiss()
                }
                .setNegativeButton("No"){dialog, _ ->
                    dialog.dismiss()
                }

            val exitDialog = builder.create()
            exitDialog.show()
            exitDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
            exitDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
        }

    }

    override fun onResume() {
        super.onResume()

        binding.playlistName.text = PlaylistActivity.musicPlaylist.ref[currentPlaylistPos].name
        binding.playlistDetails.text = buildString {
            append("Total ${adapter.itemCount} Songs")
            append("\n\nCreated on: \n${PlaylistActivity.musicPlaylist.ref[currentPlaylistPos].createdOn} " +
                    "\n\nCreated by: \n${PlaylistActivity.musicPlaylist.ref[currentPlaylistPos].createdBy} ")
        }

        if (adapter.itemCount > 0){
            Glide.with(this)
                .load(PlaylistActivity.musicPlaylist.ref[currentPlaylistPos].playList[0].artUri)
                .apply(RequestOptions().placeholder(R.drawable.music_pic))
                .into(binding.playlistImage)

            binding.shuffleBtn.visibility = View.VISIBLE
        }

        adapter.notifyDataSetChanged()

        val editor = getSharedPreferences("FAVSONG", MODE_PRIVATE).edit()
        val playlistJsonString = GsonBuilder().create().toJson(PlaylistActivity.musicPlaylist)
        editor.putString("Playlist", playlistJsonString)
        editor.apply()
    }

}