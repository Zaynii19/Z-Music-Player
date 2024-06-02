package com.example.zplayer

import android.content.Intent
import android.graphics.Color
import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.zplayer.databinding.ActivityPlaylistBinding
import com.example.zplayer.databinding.AddPlayDialogBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.Locale

class PlaylistActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityPlaylistBinding.inflate(layoutInflater)
    }

    private lateinit var playlistAdapter : PlaylistRcvAdapter
    private lateinit var list : MutableList<String>

    companion object{
        var musicPlaylist:MusicPlaylist = MusicPlaylist()
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

        list = mutableListOf()

        binding.back.setOnClickListener {
            //startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        binding.rcv.setHasFixedSize(true)
        binding.rcv.setItemViewCacheSize(13)
        binding.rcv.layoutManager = GridLayoutManager(this, 2)
        playlistAdapter = PlaylistRcvAdapter(this, musicPlaylist.ref)
        binding.rcv.adapter = playlistAdapter


        binding.addPlaylist.setOnClickListener {
            customAlertDialog()
        }

    }

    private fun customAlertDialog(){
        val customDialog = LayoutInflater.from(this).inflate(R.layout.add_play_dialog, null)
        val binder = AddPlayDialogBinding.bind(customDialog)

        val builder = MaterialAlertDialogBuilder(this)
        builder.setView(customDialog)
            .setTitle("Playlist Details")
            .setPositiveButton("Add"){ dialog, _ ->
                val playlistName = binder.playlistName.text
                val createdBy = binder.username.text
                if (playlistName != null && createdBy != null){
                    if (playlistName.isNotEmpty() && createdBy.isNotEmpty()){
                        addPlaylist(playlistName.toString(), createdBy.toString())
                    }
                }
                dialog.dismiss()
            }.show()
            .getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
    }

    private fun addPlaylist(name: String, createdBy: String) {
        var playlistExists = false
        for (i in musicPlaylist.ref){
            if (name == i.name){
                playlistExists = true
                break
            }
        }
        if (playlistExists){
            Toast.makeText(this, "Playlist Already Exists", Toast.LENGTH_SHORT).show()
        }else{
            val tempPlaylist = Playlist()
            tempPlaylist.name = name
            tempPlaylist.playList = mutableListOf()
            tempPlaylist.createdBy = createdBy
            val calendar = java.util.Calendar.getInstance().time
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
            tempPlaylist.createdOn = sdf.format(calendar)
            musicPlaylist.ref.add(tempPlaylist)
            playlistAdapter.refreshPlaylist()
        }
    }

    override fun onResume() {
        super.onResume()
        playlistAdapter.notifyDataSetChanged()
    }
}