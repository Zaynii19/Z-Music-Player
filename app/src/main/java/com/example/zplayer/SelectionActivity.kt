package com.example.zplayer

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zplayer.databinding.ActivitySelectionBinding

class SelectionActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivitySelectionBinding.inflate(layoutInflater)
    }
    private lateinit var adapter: SongRcvAdapter
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

        binding.back.setOnClickListener {
            finish()
        }

        binding.rcv.setHasFixedSize(true)
        binding.rcv.setItemViewCacheSize(13)
        binding.rcv.layoutManager = LinearLayoutManager(this)
        adapter = SongRcvAdapter(this, HomeActivity.songListMA, selection = true)
        binding.rcv.adapter = adapter

        //for search view
        binding.search.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            //call when user hit enter after search
            override fun onQueryTextSubmit(query: String?): Boolean = true
            //call when user type query
            override fun onQueryTextChange(newText: String?): Boolean {
                HomeActivity.songListSearch = mutableListOf()
                if (newText != null){
                    val userInput = newText.lowercase()
                    for (song in HomeActivity.songListMA){
                        if (song.title.lowercase().contains(userInput)){
                            HomeActivity.songListSearch.add(song)
                        }
                        adapter.updateMusicList(HomeActivity.songListSearch)
                    }
                }
                return true
            }
        })


    }
}