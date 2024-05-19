package com.example.zplayer

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.zplayer.databinding.ActivitySongBinding

class SongActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivitySongBinding.inflate(layoutInflater)
    }

    companion object {
        lateinit var songListSA: MutableList<SongsLists>
        var songIndex:Int = 0
        var mediaPlayer:MediaPlayer? = null
        var isPlaying:Boolean = false
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Setting Theme
        setTheme(R.style.coolPink)

        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        songListSA = mutableListOf()

        binding.back.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }

        initializeLayout()

        //play and pause song
        binding.pauseSong.setOnClickListener {
            if (isPlaying){
                pauseMusic()
            }else{
                playMusic()
            }
        }

        binding.rightSongBtn.setOnClickListener {
            preNextSong(true)
        }

        binding.leftSongBtn.setOnClickListener {
            preNextSong(false)
        }

    }

    //Update UI for each song
    private fun setLayout(){
        //set song album image using glide
        Glide.with(this)
            .load(songListSA[songIndex].artUri)
            .apply(RequestOptions().placeholder(R.drawable.music_player))  //if error in loading song album pic or have no pic
            .into(binding.songPic)

        binding.songName.text = songListSA[songIndex].title
        binding.songTotalLength.text = songListSA[songIndex].formattedDuration
    }

    //play song
    private fun createMediaPlayer(){
        try{
            if (mediaPlayer == null){
                mediaPlayer = MediaPlayer()
            }
            mediaPlayer!!.reset()
            mediaPlayer!!.setDataSource(songListSA[songIndex].path)
            mediaPlayer!!.prepare()
            mediaPlayer!!.start()
            isPlaying = true
            binding.pauseSong.setIconResource(R.drawable.pause)
        }catch (e:Exception){
            return
        }
    }

    private fun initializeLayout(){
        songIndex = intent.getIntExtra("index", 0)
        when(intent.getStringExtra("class")){
            "SongRcvAdapter" -> {
                songListSA.addAll(HomeActivity.songListMA)
                setLayout()
                createMediaPlayer()
            }

            "MainActivity" -> {
                songListSA.addAll(HomeActivity.songListMA)
                songListSA.shuffle()
                setLayout()
                createMediaPlayer()
            }
        }
    }

    private fun playMusic(){
        binding.pauseSong.setIconResource(R.drawable.pause)
        mediaPlayer!!.start()
        isPlaying = true
    }
    private fun pauseMusic(){
        binding.pauseSong.setIconResource(R.drawable.play)
        mediaPlayer!!.pause()
        isPlaying = false
    }

    //play next and previous song
    private fun preNextSong(next : Boolean){
        if (next){
            setSongPosition(true)
            setLayout()
            createMediaPlayer()
        }else{
            setSongPosition(false)
            setLayout()
            createMediaPlayer()
        }
    }

    private fun setSongPosition(next: Boolean){
        if (next){
            if (songListSA.size - 1 == songIndex){
                songIndex = 0
            }else{
                ++songIndex
            }
        }else{
            if (songIndex == 0){
                songIndex = songListSA.size - 1
            }else{
                --songIndex
            }
        }
    }
}