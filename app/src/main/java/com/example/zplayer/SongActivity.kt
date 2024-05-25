package com.example.zplayer

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.media.MediaPlayer
import android.media.audiofx.AudioEffect
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.zplayer.databinding.ActivitySongBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlin.system.exitProcess

//ServiceConnection add service connection to activity and implement members
//MediaPlayer.OnCompletionListener restart next song when song is complete
@Suppress("DEPRECATION")
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
        var min15: Boolean = false
        var min30: Boolean = false
        var min60: Boolean = false
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

        if (min15 || min30 || min60){
            binding.timerBtn.setColorFilter(ContextCompat.getColor(this, R.color.purple_588))
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

            //repeat button functionality
            binding.repeatBtn.setOnClickListener {
                if (!repeat){
                    repeat = true
                    binding.repeatBtn.setColorFilter(ContextCompat.getColor(this, R.color.purple_588))
                }else{
                    repeat = false
                    binding.repeatBtn.setColorFilter(ContextCompat.getColor(this, R.color.AppRedButtons))
                }
            }

            //equalizer button functionality
            binding.equalizerBtn.setOnClickListener {
                try {
                    val eqIntent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
                    eqIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, musicService!!.mediaPlayer!!.audioSessionId)
                    eqIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, baseContext.packageName)
                    eqIntent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
                    startActivityForResult(eqIntent, 19)
                }catch (e: Exception){
                    Toast.makeText(this, "Equalizer Feature not Sported!", Toast.LENGTH_SHORT).show()
                }
            }

            //timer button functionality
            binding.timerBtn.setOnClickListener {
                val timer = min15 || min30 || min60
                if (!timer)
                showBottomSheetDialog()
                else{
                    val builder = MaterialAlertDialogBuilder(this)
                    builder.setTitle("Stop Timer")
                        .setMessage("Do you want to stop Timer?")
                        //first _ show dialog and 2nd _ show result
                        .setPositiveButton("Yes"){ _, _ ->
                            min15 = false
                            min30 = false
                            min60 = false

                            binding.timerBtn.setColorFilter(ContextCompat.getColor(this, R.color.AppRedButtons))
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

            //share button functionality
            binding.shareBtn.setOnClickListener {
                val shareIntent = Intent()
                shareIntent.action = Intent.ACTION_SEND
                shareIntent.type = "audio/*"
                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(songListSA[songIndex].path))
                startActivity(Intent.createChooser(shareIntent, "Share Music File"))
            }



        }catch (e:Exception){
            return
        }
    }

    private fun initializeLayout(){
        songIndex = intent.getIntExtra("index", 0)
        when(intent.getStringExtra("class")){
            "SongRcvAdapterSearch" -> {
                songListSA.addAll(HomeActivity.songListSearch)
                setLayout()
            }
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

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 19 || resultCode == RESULT_OK){
            return
        }
    }

    private fun showBottomSheetDialog(){
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(R.layout.timer_bottomsheet_dialog)
        dialog.show()

        dialog.findViewById<LinearLayout>(R.id.min_15)?.setOnClickListener {
            Toast.makeText(this, "Music App Closed in 15 Minutes", Toast.LENGTH_SHORT).show()
            binding.timerBtn.setColorFilter(ContextCompat.getColor(this, R.color.purple_588))

            min15 = true
            Thread{
                Thread.sleep(9000000)
                if (min15) terminateApp()
            }.start()

            dialog.dismiss()
        }

        dialog.findViewById<LinearLayout>(R.id.min_30)?.setOnClickListener {
            Toast.makeText(this, "Music App Closed in 30 Minutes", Toast.LENGTH_SHORT).show()
            binding.timerBtn.setColorFilter(ContextCompat.getColor(this, R.color.purple_588))

            min30 = true
            Thread{
                Thread.sleep(1800000)
                if (min30) terminateApp()
            }.start()

            dialog.dismiss()
        }

        dialog.findViewById<LinearLayout>(R.id.min_60)?.setOnClickListener {
            Toast.makeText(this, "Music App Closed in 60 Minutes", Toast.LENGTH_SHORT).show()
            binding.timerBtn.setColorFilter(ContextCompat.getColor(this, R.color.purple_588))

            min60 = true
            Thread{
                Thread.sleep(3600000)
                if (min60) terminateApp()
            }.start()

            dialog.dismiss()
        }
    }
}