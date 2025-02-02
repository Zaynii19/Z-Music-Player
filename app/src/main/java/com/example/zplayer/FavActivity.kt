package com.example.zplayer

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.zplayer.databinding.ActivityFavBinding

class FavActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityFavBinding.inflate(layoutInflater)
    }
/*    companion object {
        var songListFA: MutableList<SongsLists> = mutableListOf()
        @SuppressLint("StaticFieldLeak")
        var favSongAdapter: FavRcvAdapter? = null
    }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setting Theme
        setTheme(HomeActivity.currentTheme[HomeActivity.themeIndex])

        enableEdgeToEdge()
        setContentView(binding.root)

        // Initialize the adapter if not already initialized
        if (FavoriteManager.favSongAdapter == null) {
            FavoriteManager.favSongAdapter = FavRcvAdapter(this, FavoriteManager.songListFA)
        }

        // Update song lists
        FavoriteManager.songListFA = checkPlaylist(FavoriteManager.songListFA)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.back.setOnClickListener {
            // Start activity Intent(this, HomeActivity::class.java)
            finish()
        }

        binding.rcv.setHasFixedSize(true)
        binding.rcv.setItemViewCacheSize(13)
        binding.rcv.layoutManager = GridLayoutManager(this, 4)

        // Initialize favSongAdapter here
        FavoriteManager.favSongAdapter = FavRcvAdapter(this, FavoriteManager.songListFA)
        binding.rcv.adapter = FavoriteManager.favSongAdapter

        if (FavoriteManager.songListFA.size < 2) {
            binding.shuffleBtn.visibility = View.INVISIBLE
        }

        binding.shuffleBtn.setOnClickListener {
            val intent = Intent(this, SongActivity::class.java)
            intent.putExtra("index", 0)
            intent.putExtra("class", "FavShuffle")
            startActivity(intent)
        }
    }
}
