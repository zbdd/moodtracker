package com.example.rows

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import androidx.appcompat.widget.SwitchCompat
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsActivity() : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val sMoodNumerals: Switch = findViewById(R.id.sMoodNumerals)
        val bSettingsConfirm: Button = findViewById(R.id.bSettingsConfirm)

        var settings = intent.getParcelableExtra<Settings>("Settings")
        sMoodNumerals.isChecked = settings?.mood_numerals?.equals("true") ?: false

        sMoodNumerals.setOnCheckedChangeListener { compoundButton, isChecked ->
            run {
                settings?.mood_numerals = isChecked.toString()
            }
        }

        bSettingsConfirm.setOnClickListener {
            val finishIntent = Intent()
            finishIntent.putExtra("Settings", settings)
            setResult(RESULT_OK, finishIntent)
            finish()
        }
    }
}