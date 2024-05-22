package com.example.zplayer

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.CalendarContract.Colors
import android.provider.MediaStore
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zplayer.databinding.ActivityHomeBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlin.system.exitProcess

class HomeActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityHomeBinding.inflate(layoutInflater)
    }

    private lateinit var toggle: ActionBarDrawerToggle

    private val REQUEST_CODE_READ_MEDIA_AUDIO = 19
    private val REQUEST_CODE_READ_EXTERNAL_STORAGE = 20

    private lateinit var songAdapter: SongRcvAdapter

    // Static single object of class can be accessed by any class
    companion object {
        lateinit var songListMA: MutableList<SongsLists>
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTheme(R.style.coolPinkNav)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        songListMA = mutableListOf()

        // Define toggle
        toggle = ActionBarDrawerToggle(this, binding.main, binding.toolbar, R.string.open, R.string.close)
        binding.main.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        binding.playlistBtn.setOnClickListener {
            startActivity(Intent(this, PlaylistActivity::class.java))
        }

        binding.favBtn.setOnClickListener {
            startActivity(Intent(this, FavActivity::class.java))
        }

        binding.shuffleBtn.setOnClickListener {
            intent = Intent(this, SongActivity::class.java)
            intent.putExtra("index", 0)
            intent.putExtra("class", "MainActivity")
            startActivity(intent)
        }

        // Navigation items functionality
        binding.navBar.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.settings -> Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show()
                R.id.about -> Toast.makeText(this, "About", Toast.LENGTH_SHORT).show()
                R.id.exit -> {
                    val builder = MaterialAlertDialogBuilder(this)
                    builder.setTitle("EXit")
                        .setMessage("Do you want to exit App?")
                        //first _ show dialog and 2nd _ show result
                        .setPositiveButton("Yes"){ _, _ ->
                            terminateApp()
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
            true
        }

        // Request permissions and load songs if permissions are already granted
        requestRuntimePermission()
    }

    private fun requestRuntimePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!hasReadMediaAudioPermission()) {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_MEDIA_AUDIO), REQUEST_CODE_READ_MEDIA_AUDIO)
            } else {
                initializeSongList()
            }
        } else {
            if (!hasReadExternalStoragePermission()) {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CODE_READ_EXTERNAL_STORAGE)
            } else {
                initializeSongList()
            }
        }
    }

    // After reading storage store songs to list
    private fun initializeSongList() {
        if (hasReadMediaAudioPermission() || hasReadExternalStoragePermission()) {
            songListMA = getAllSongs()
            setupRecyclerView()
        }
    }

    private fun setupRecyclerView() {
        binding.rcv.setHasFixedSize(true)
        binding.rcv.setItemViewCacheSize(13)
        binding.rcv.layoutManager = LinearLayoutManager(this)
        songAdapter = SongRcvAdapter(this, songListMA)
        binding.rcv.adapter = songAdapter

        binding.totalSongs.text = buildString {
            append("Total Songs: ")
            append(songAdapter.itemCount)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_READ_MEDIA_AUDIO || requestCode == REQUEST_CODE_READ_EXTERNAL_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Storage Permission Granted", Toast.LENGTH_SHORT).show()
                initializeSongList()
            } else {
                Toast.makeText(this, "Storage Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Read storage and retrieve songs data
    @SuppressLint("Recycle")
    private fun getAllSongs(): MutableList<SongsLists> {
        val audioFiles = mutableListOf<SongsLists>()
        val selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0"
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID
        )
        val cursor = contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            MediaStore.Audio.Media.DATE_ADDED + " DESC"
        )
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    val id = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID))
                    val title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                    val album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM))
                    val artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                    val duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))
                    val path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
                    // Get song picture
                    val albumId = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)).toString()
                    val uri = Uri.parse("content://media/external/audio/albumart")
                    val artUri = Uri.withAppendedPath(uri, albumId).toString()

                    if (id != null && path != null) {
                        val formattedDuration = formatSongDuration(duration)
                        audioFiles.add(SongsLists(id, title, album, artist, path, formattedDuration, artUri))
                    }
                } while (cursor.moveToNext())
                cursor.close()
            }
        }
        return audioFiles
    }

    // Navigation items selection
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item))
            return true
        return super.onOptionsItemSelected(item)
    }



    private fun hasReadMediaAudioPermission() =
        ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED

    private fun hasReadExternalStoragePermission() =
        ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

    //run when app is closed
    override fun onDestroy() {
        super.onDestroy()
        if (!SongActivity.isPlaying && SongActivity.musicService != null){
            terminateApp()
        }
    }
}
