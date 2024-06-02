package com.example.zplayer

import android.graphics.Color
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.zplayer.databinding.ActivitySettingBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SettingActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivitySettingBinding.inflate(layoutInflater)
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

        supportActionBar?.title = "Settings"

        when(HomeActivity.themeIndex){
            0 -> binding.coolPinkTheme.setBackgroundColor(Color.YELLOW)
            1 -> binding.coolBlueTheme.setBackgroundColor(Color.YELLOW)
            2 -> binding.coolGreenTheme.setBackgroundColor(Color.YELLOW)
            3 -> binding.purpleTheme.setBackgroundColor(Color.YELLOW)
            4 -> binding.blackTheme.setBackgroundColor(Color.YELLOW)
        }

        binding.coolPinkTheme.setOnClickListener {
            saveTheme(0)
        }
        binding.coolBlueTheme.setOnClickListener {
            saveTheme(1)
        }
        binding.coolGreenTheme.setOnClickListener {
            saveTheme(2)
        }
        binding.purpleTheme.setOnClickListener {
            saveTheme(3)
        }
        binding.blackTheme.setOnClickListener {
            saveTheme(4)
        }
    }

    private fun saveTheme(index:Int){
        if (HomeActivity.themeIndex != index){

            val editor = getSharedPreferences("THEMES", MODE_PRIVATE).edit()
            editor.putInt("themeIndex", index)
            editor.apply()

            val builder = MaterialAlertDialogBuilder(this)
            builder.setTitle("Apply Theme")
                .setMessage("Do you want to Apply This Theme")
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
}