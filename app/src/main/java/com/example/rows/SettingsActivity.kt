package com.example.rows


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity


class SettingsActivity() : AppCompatActivity() {

    private lateinit var getImportJsonFileResult: ActivityResultLauncher<Intent>

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val sMoodNumerals: Switch = findViewById(R.id.sMoodNumerals)
        val tvSettingsImport: TextView = findViewById(R.id.tvSettingsImport)
        val bSettingsConfirm: Button = findViewById(R.id.bSettingsConfirm)

        val settings = intent.getParcelableExtra<Settings>("Settings")
        sMoodNumerals.isChecked = settings?.mood_numerals?.equals("true") ?: false

        sMoodNumerals.setOnCheckedChangeListener { compoundButton, isChecked ->
            run {
                settings?.mood_numerals = isChecked.toString()
            }
        }

        tvSettingsImport.setOnClickListener {
            val intent = Intent()
                .setType("*/*")
                .setAction(Intent.ACTION_GET_CONTENT)

            getImportJsonFileResult.launch(intent)
        }

        bSettingsConfirm.setOnClickListener {
            val finishIntent = Intent()
            finishIntent.putExtra("Settings", settings)
            setResult(RESULT_OK, finishIntent)
            finish()
        }

        getImportJsonFileResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                val data = it.data
                println(data)
            }
    }
}