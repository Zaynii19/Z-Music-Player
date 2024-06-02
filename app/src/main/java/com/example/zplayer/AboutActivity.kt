package com.example.zplayer

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.zplayer.databinding.ActivityAboutBinding
import com.example.zplayer.databinding.ActivitySettingBinding

class AboutActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityAboutBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTheme(HomeActivity.currentThemeNav[HomeActivity.themeIndex])

        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        supportActionBar?.title = "About"

        binding.about.text = buildString {
            append("Developed by: ALI ZAIN ")
            append("\n\n  Version: 1.0")
        }
    }
}