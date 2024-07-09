package com.ml.quaterion.facenetdetection // Update with your package name

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ml.quaterion.facenetdetection.databinding.ActivityMenuBinding

class MenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.openCameraButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        binding.addPeopleButton.setOnClickListener {
            // Start AddPeopleActivity with an explicit Intent
            startActivity(Intent(this, AddPeopleActivity::class.java))
        }
    }
}