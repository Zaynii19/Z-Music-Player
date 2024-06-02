package com.example.zplayer

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.zplayer.databinding.FragmentNowPlayingBinding

class NowPlayingFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        //set Theme
        requireContext().theme.applyStyle(HomeActivity.currentTheme[HomeActivity.themeIndex], true)

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_now_playing, container, false)
        binding = FragmentNowPlayingBinding.bind(view)
        binding.root.visibility = View.INVISIBLE

        binding.playPauseBtn.setOnClickListener {
            if (SongActivity.isPlaying)
                pauseSong()
            else
                playSong()
        }

        binding.nextBtn.setOnClickListener {
            setSongPosition(next = true)
            SongActivity.musicService!!.createMediaPlayer()

            //change the layout of Now Playing Fragment
            Glide.with(this)
                .load(SongActivity.songListSA[SongActivity.songIndex].artUri)
                .apply(RequestOptions().placeholder(R.drawable.music_player))  //if error in loading song album pic or have no pic
                .into(binding.songPicF)

            binding.songNameF.text = SongActivity.songListSA[SongActivity.songIndex].title
            binding.songNameF.setSelected(true)

            SongActivity.musicService!!.showNotification(R.drawable.noti_pause)

            playSong()
        }

        //when user click on now playing layout direct to song player layout
        binding.main.setOnClickListener {
            val intent = Intent(requireContext(), SongActivity::class.java)
            intent.putExtra("index", SongActivity.songIndex)
            intent.putExtra("class", "NowPlaying")
            ContextCompat.startActivity(requireContext(), intent, null)
        }



        return view
    }

    override fun onResume() {
        super.onResume()
        if (SongActivity.musicService !=  null){
            binding.root.visibility = View.VISIBLE
            //set song album image using glide
            Glide.with(this)
                .load(SongActivity.songListSA[SongActivity.songIndex].artUri)
                .apply(RequestOptions().placeholder(R.drawable.music_player))  //if error in loading song album pic or have no pic
                .into(binding.songPicF)

            binding.songNameF.text = SongActivity.songListSA[SongActivity.songIndex].title
            binding.songNameF.setSelected(true)

            if (SongActivity.isPlaying)
                binding.playPauseBtn.setImageResource(R.drawable.pause)
            else
                binding.playPauseBtn.setImageResource(R.drawable.play)
        }
    }

    private fun playSong(){
        SongActivity.musicService!!.mediaPlayer!!.start()
        binding.playPauseBtn.setImageResource(R.drawable.pause)
        SongActivity.musicService!!.showNotification(R.drawable.noti_pause)
        SongActivity.binding.pauseSong.setIconResource(R.drawable.pause)
        SongActivity.isPlaying = true
    }

    private fun pauseSong(){
        SongActivity.musicService!!.mediaPlayer!!.pause()
        binding.playPauseBtn.setImageResource(R.drawable.play)
        SongActivity.musicService!!.showNotification(R.drawable.noti_play)
        SongActivity.binding.pauseSong.setIconResource(R.drawable.play)
        SongActivity.isPlaying = false
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var binding : FragmentNowPlayingBinding

    }
}