package com.example.zplayer

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.os.IBinder
import android.widget.SeekBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.zplayer.databinding.ActivitySongBinding
//ServiceConnection add service connection to activity and implement members
//MediaPlayer.OnCompletionListener restart next song when song is complete
class SongActivity : AppCompatActivity(), ServiceConnection, MediaPlayer.OnCompletionListener{
    companion object {
        lateinit var songListSA: MutableList<SongsLists>
        var songIndex:Int = 0
        //var mediaPlayer:MediaPlayer? = null   //use when no service created
        var isPlaying:Boolean = false
        var musicService : MusicService? = null
        @SuppressLint("StaticFieldLeak")
        lateinit var binding: ActivitySongBinding
        var repeat : Boolean = false
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Setting Theme
        setTheme(R.style.coolPink)

        binding = ActivitySongBinding.inflate(layoutInflater)

        //set binding
        setContentView(binding.root)

        //start service
        val intent = Intent(this, MusicService::class.java)
        bindService(intent, this, BIND_AUTO_CREATE)
        startService(intent)

        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        songListSA = mutableListOf()

        binding.back.setOnClickListener {
            //startActivity(Intent(this, HomeActivity::class.java))
            finish()
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

        binding.seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser){
                    musicService!!.mediaPlayer!!.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit


        })

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
        if (repeat){
            binding.repeatBtn.setColorFilter(ContextCompat.getColor(this, R.color.purple_588))
        }
    }

    //play song
    private fun createMediaPlayer(){
        try{
            if (musicService!!.mediaPlayer == null){
                musicService!!.mediaPlayer = MediaPlayer()
            }
            musicService!!.mediaPlayer!!.reset()
            musicService!!.mediaPlayer!!.setDataSource(songListSA[songIndex].path)
            musicService!!.mediaPlayer!!.prepare()
            musicService!!.mediaPlayer!!.start()
            isPlaying = true
            binding.pauseSong.setIconResource(R.drawable.pause)
            musicService!!.showNotification(R.drawable.noti_pause)

            //set seekbar, current song duration, total song duration
            binding.songTotalLength.text = songListSA[songIndex].formattedDuration
            binding.songStartLength.text = formatSongDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
            binding.seekbar.progress = 0
            binding.seekbar.max = musicService!!.mediaPlayer!!.duration
            musicService!!.mediaPlayer!!.setOnCompletionListener (this)

            //below buttons
            binding.repeatBtn.setOnClickListener {
                if (!repeat){
                    repeat = true
                    binding.repeatBtn.setColorFilter(ContextCompat.getColor(this, R.color.purple_588))
                }else{
                    repeat = false
                    binding.repeatBtn.setColorFilter(ContextCompat.getColor(this, R.color.AppRedButtons))
                }
            }

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
                //createMediaPlayer()  //when service is not created
            }

            "MainActivity" -> {
                songListSA.addAll(HomeActivity.songListMA)
                songListSA.shuffle()
                setLayout()
                //createMediaPlayer() //when service is not created
            }
        }
    }

    private fun playMusic(){
        binding.pauseSong.setIconResource(R.drawable.pause)
        musicService!!.showNotification(R.drawable.noti_pause)
        musicService!!.mediaPlayer!!.start()
        isPlaying = true
    }
    private fun pauseMusic(){
        binding.pauseSong.setIconResource(R.drawable.play)
        musicService!!.showNotification(R.drawable.noti_play)
        musicService!!.mediaPlayer!!.pause()
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


    //Service connection methods
    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as MusicService.MyBinder
        musicService = binder.currentService()
        createMediaPlayer()
        musicService!! .seekbarSetup()
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        musicService = null
    }

    //restart next song when song is complete
    override fun onCompletion(mp: MediaPlayer?) {
        setSongPosition(true)
        createMediaPlayer()
        try {
            setLayout()
        }catch (e:Exception){
            return
        }
    }
}