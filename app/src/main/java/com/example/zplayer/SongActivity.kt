package com.example.zplayer

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.database.Cursor
import android.graphics.Color
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.audiofx.AudioEffect
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
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

@Suppress("DEPRECATION")
class SongActivity : AppCompatActivity(), ServiceConnection, MediaPlayer.OnCompletionListener {
    companion object {
        lateinit var songListSA: MutableList<SongsLists>
        var songIndex: Int = 0
        var isPlaying: Boolean = false
        var musicService: MusicService? = null
        @SuppressLint("StaticFieldLeak")
        lateinit var binding: ActivitySongBinding
        var repeat: Boolean = false
        var min15: Boolean = false
        var min30: Boolean = false
        var min60: Boolean = false
        var nowPlayedId: String = ""
        var isFav: Boolean = false
        var fSongIndex: Int = -1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Setting Theme
        setTheme(HomeActivity.currentTheme[HomeActivity.themeIndex])

        binding = ActivitySongBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        songListSA = mutableListOf()

        binding.back.setOnClickListener {
            finish()
        }

        //run when song from external source play
        if (intent.data?.scheme.contentEquals("content")){
            val intentService = Intent(this, MusicService::class.java)
            bindService(intentService, this, BIND_AUTO_CREATE)
            startService(intentService)
            songListSA = mutableListOf()
            songListSA.add(getMusicDetails(intent.data!!))
            Glide.with(this)
                .load(getImageArt(songListSA[songIndex].path))
                .apply(RequestOptions().placeholder(R.drawable.music_player))
                .into(binding.songPic)

            binding.songName.text = songListSA[songIndex].title
        }else{
            initializeLayout()
        }

        binding.pauseSong.setOnClickListener {
            if (isPlaying) {
                pauseMusic()
            } else {
                playMusic()
            }
        }

        binding.rightSongBtn.setOnClickListener {
            preNextSong(true)
        }

        binding.leftSongBtn.setOnClickListener {
            preNextSong(false)
        }

        binding.seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    musicService!!.mediaPlayer!!.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })

        binding.repeatBtn.setOnClickListener {
            repeat = !repeat
            val color = if (repeat) R.color.purple_588 else R.color.AppRedButtons
            binding.repeatBtn.setColorFilter(ContextCompat.getColor(this, color))
        }

        binding.equalizerBtn.setOnClickListener {
            try {
                val eqIntent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
                eqIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, musicService!!.mediaPlayer!!.audioSessionId)
                eqIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, baseContext.packageName)
                eqIntent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
                startActivityForResult(eqIntent, 19)
            } catch (e: Exception) {
                Toast.makeText(this, "Equalizer Feature not Supported!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.timerBtn.setOnClickListener {
            val timer = min15 || min30 || min60
            if (!timer) showBottomSheetDialog()
            else {
                val builder = MaterialAlertDialogBuilder(this)
                builder.setTitle("Stop Timer")
                    .setMessage("Do you want to stop Timer?")
                    .setPositiveButton("Yes") { _, _ ->
                        min15 = false
                        min30 = false
                        min60 = false
                        binding.timerBtn.setColorFilter(ContextCompat.getColor(this, R.color.AppRedButtons))
                    }
                    .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }

                val exitDialog = builder.create()
                exitDialog.show()
                exitDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
                exitDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
            }
        }

        binding.shareBtn.setOnClickListener {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.type = "audio/*"
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(songListSA[songIndex].path))
            startActivity(Intent.createChooser(shareIntent, "Share Music File"))
        }

        binding.favBtn.setOnClickListener {
            if (FavActivity.favSongAdapter != null){
                val favSongAdapter = FavActivity.favSongAdapter!!
                if (isFav) {
                    binding.favBtn.setImageResource(R.drawable.empty_fav)
                    isFav = false
                    // Use the song object to find its index and remove it
                    val songToRemove = songListSA[songIndex]
                    val indexToRemove = FavActivity.songListFA.indexOf(songToRemove)
                    if (indexToRemove != -1) {
                        FavActivity.songListFA.removeAt(indexToRemove)
                        favSongAdapter.updateMusicList(FavActivity.songListFA)
                    }
                } else {
                    binding.favBtn.setImageResource(R.drawable.fav)
                    isFav = true
                    FavActivity.songListFA.add(songListSA[songIndex])
                    favSongAdapter.updateMusicList(FavActivity.songListFA) // Update adapter
                }
            }else{
                Toast.makeText(this, "Favorite songs not loaded yet", Toast.LENGTH_SHORT).show()
            }

        }
    }

    //for song played from external storage
    private fun getMusicDetails(contentUri: Uri): SongsLists {
        var cursor: Cursor? = null
        try {
            val projection = arrayOf(MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DURATION)
            cursor = this.contentResolver.query(contentUri, projection, null, null, null)
            val dataColumn = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val durationColumn = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            cursor!!.moveToFirst()
            val path = dataColumn?.let { cursor.getString(it) }
            val duration = durationColumn?.let { cursor.getLong(it) }
            val formattedDuration = duration?.let { formatSongDuration(it) }
            return  SongsLists("Unknown", path.toString(), "Unknown", "Unknown", path.toString(), formattedDuration.toString(), "Unknown")
        }finally {
            cursor?.close()
        }
    }

    private fun setLayout() {
        fSongIndex = favChecker(songListSA[songIndex].id)

        Glide.with(this)
            .load(songListSA[songIndex].artUri)
            .apply(RequestOptions().placeholder(R.drawable.music_player))
            .into(binding.songPic)

        binding.songName.text = songListSA[songIndex].title
        binding.songName.isSelected = true
        binding.songTotalLength.text = songListSA[songIndex].formattedDuration

        if (repeat) {
            binding.repeatBtn.setColorFilter(ContextCompat.getColor(this, R.color.purple_588))
        }

        if (min15 || min30 || min60) {
            binding.timerBtn.setColorFilter(ContextCompat.getColor(this, R.color.purple_588))
        }

        if (isFav) {
            binding.favBtn.setImageResource(R.drawable.fav)
        } else {
            binding.favBtn.setImageResource(R.drawable.empty_fav)
        }
    }

    private fun createMediaPlayer(startPlaying: Boolean = true) {
        try {
            if (musicService!!.mediaPlayer == null) {
                musicService!!.mediaPlayer = MediaPlayer()
            }
            musicService!!.mediaPlayer!!.reset()
            musicService!!.mediaPlayer!!.setDataSource(songListSA[songIndex].path)
            musicService!!.mediaPlayer!!.prepare()
            if (startPlaying) {
                musicService!!.mediaPlayer!!.start()
                isPlaying = true
                binding.pauseSong.setIconResource(R.drawable.pause)
                musicService!!.showNotification(R.drawable.noti_pause, 1f)
            }
            binding.songTotalLength.text = songListSA[songIndex].formattedDuration
            binding.songStartLength.text = formatSongDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
            binding.seekbar.progress = 0
            binding.seekbar.max = musicService!!.mediaPlayer!!.duration
            musicService!!.mediaPlayer!!.setOnCompletionListener(this)

            nowPlayedId = songListSA[songIndex].id

        } catch (e: Exception) {
            Log.e("SongActivity", "Error creating media player", e)
        }
    }

    private fun initializeLayout() {
        songIndex = intent.getIntExtra("index", 0)
        val sourceClass = intent.getStringExtra("class")
        when (sourceClass) {
            "PlaylistDetailsShuffle" -> {
                songListSA.addAll(PlaylistActivity.musicPlaylist.ref[PlaylistDetailsActivity.currentPlaylistPos].playList)
                songListSA.shuffle()
            }

            "PlaylistDetailsAdapter" -> {
                songListSA.addAll(PlaylistActivity.musicPlaylist.ref[PlaylistDetailsActivity.currentPlaylistPos].playList)
            }

            "FavShuffle" -> {
                songListSA.addAll(FavActivity.songListFA)
                songListSA.shuffle()
            }

            "FavAdapter" -> {
                songListSA.addAll(FavActivity.songListFA)
            }

            "NowPlaying" -> {
                if (HomeActivity.songListSearch.isNotEmpty()) {
                    songListSA.addAll(HomeActivity.songListSearch)
                } else {
                    songListSA.addAll(HomeActivity.songListMA)
                }
                setLayout()
                updateUI()
            }

            "SongRcvAdapterSearch" -> {
                songListSA.addAll(HomeActivity.songListSearch)
            }

            "SongRcvAdapter" -> {
                songListSA.addAll(HomeActivity.songListMA)
            }

            "MainActivity" -> {
                songListSA.addAll(HomeActivity.songListMA)
                songListSA.shuffle()
            }
        }

        val intent = Intent(this, MusicService::class.java)
        bindService(intent, this, BIND_AUTO_CREATE)
        startService(intent)
        setLayout()
        createMediaPlayer(startPlaying = true)
    }

    private fun updateUI() {
        if (isPlaying) {
            binding.pauseSong.setIconResource(R.drawable.pause)
        } else {
            binding.pauseSong.setIconResource(R.drawable.play)
        }

        binding.seekbar.progress = musicService?.mediaPlayer?.currentPosition ?: 0

        if (repeat) {
            binding.repeatBtn.setColorFilter(ContextCompat.getColor(this, R.color.purple_588))
        }

        if (min15 || min30 || min60) {
            binding.timerBtn.setColorFilter(ContextCompat.getColor(this, R.color.purple_588))
        }

        if (isFav) {
            binding.favBtn.setImageResource(R.drawable.fav)
        } else {
            binding.favBtn.setImageResource(R.drawable.empty_fav)
        }
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as MusicService.MyBinder
        musicService = binder.currentService()
        createMediaPlayer(startPlaying = true)
        musicService!!.seekbarSetup()
        musicService!!.audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        musicService!!.audioManager.requestAudioFocus(musicService, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        musicService = null
    }

    private fun pauseMusic() {
        isPlaying = false
        musicService!!.mediaPlayer!!.pause()
        binding.pauseSong.setIconResource(R.drawable.play)
        musicService!!.showNotification(R.drawable.noti_play, 0f)
    }

    private fun playMusic() {
        isPlaying = true
        musicService!!.mediaPlayer!!.start()
        binding.pauseSong.setIconResource(R.drawable.pause)
        musicService!!.showNotification(R.drawable.noti_pause, 1f)
    }

    private fun preNextSong(increment: Boolean) {
        if (increment) {
            setSongPosition(true)
        } else {
            setSongPosition(false)
        }
        createMediaPlayer(startPlaying = true)
        setLayout()
    }

    private fun showBottomSheetDialog() {
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(R.layout.timer_bottomsheet_dialog)
        dialog.show()

        dialog.findViewById<LinearLayout>(R.id.min_15)?.setOnClickListener {
            Toast.makeText(this, "Music App Closed in 15 Minutes", Toast.LENGTH_SHORT).show()
            binding.timerBtn.setColorFilter(ContextCompat.getColor(this, R.color.purple_588))
            min15 = true
            Thread {
                Thread.sleep(900000)
                if (min15) terminateApp()
            }.start()
            dialog.dismiss()
        }

        dialog.findViewById<LinearLayout>(R.id.min_30)?.setOnClickListener {
            Toast.makeText(this, "Music App Closed in 30 Minutes", Toast.LENGTH_SHORT).show()
            binding.timerBtn.setColorFilter(ContextCompat.getColor(this, R.color.purple_588))
            min30 = true
            Thread {
                Thread.sleep(1800000)
                if (min30) terminateApp()
            }.start()
            dialog.dismiss()
        }

        dialog.findViewById<LinearLayout>(R.id.min_60)?.setOnClickListener {
            Toast.makeText(this, "Music App Closed in 60 Minutes", Toast.LENGTH_SHORT).show()
            binding.timerBtn.setColorFilter(ContextCompat.getColor(this, R.color.purple_588))
            min60 = true
            Thread {
                Thread.sleep(3600000)
                if (min60) terminateApp()
            }.start()
            dialog.dismiss()
        }
    }


    override fun onCompletion(mp: MediaPlayer?) {
        preNextSong(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (songListSA[songIndex].id == "Unknown" && !isPlaying) exitProcess(1)
    }
}
