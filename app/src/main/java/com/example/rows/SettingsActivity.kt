package com.example.rows


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.w3c.dom.Text
import java.lang.Exception
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class SettingsActivity() : AppCompatActivity() {

    private lateinit var getImportJsonFileResult: ActivityResultLauncher<Intent>
    private lateinit var getExportJsonFileResult: ActivityResultLauncher<Intent>

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val sMoodNumerals: Switch = findViewById(R.id.sMoodNumerals)
        val tvSettingsImport: TextView = findViewById(R.id.tvSettingsImport)
        val tvSettingsExport: TextView = findViewById(R.id.tvSettingsExport)
        val bSettingsConfirm: Button = findViewById(R.id.bSettingsConfirm)
        val moodData = ArrayList<MoodEntryModel>()

        val settings = intent.getParcelableExtra<Settings>("Settings")
        sMoodNumerals.isChecked = settings?.mood_numerals?.equals("true") ?: false

        sMoodNumerals.setOnCheckedChangeListener { compoundButton, isChecked ->
            run {
                settings?.mood_numerals = isChecked.toString()
            }
        }

        tvSettingsExport.setOnClickListener {
            val intent = Intent()
                .setType("text/json")
                .addCategory(Intent.CATEGORY_OPENABLE)
                .setAction(Intent.ACTION_CREATE_DOCUMENT)
            getExportJsonFileResult.launch(intent)
        }

        tvSettingsImport.setOnClickListener {
            val intent = Intent()
                .setType("*/*")
                .setAction(Intent.ACTION_GET_CONTENT)

            run { getImportJsonFileResult.launch(intent) }
        }

        bSettingsConfirm.setOnClickListener {
            val finishIntent = Intent()
            finishIntent.putExtra("Settings", settings)
            finishIntent.putParcelableArrayListExtra("MoodEntries", moodData as java.util.ArrayList<out Parcelable>)
            setResult(RESULT_OK, finishIntent)
            finish()
        }

        getExportJsonFileResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { exportResult ->
            println(exportResult)
        }

        getImportJsonFileResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
                val path = activityResult.data?.data
                var inputAsString = ""
                var exception = 0

                try {
                    val jsonFile = path?.let { it -> contentResolver.openInputStream(it) }
                    inputAsString =
                        jsonFile?.bufferedReader().use { it?.readText() ?: "Failed to read" }
                } catch (e: Exception) {
                    Toast.makeText(this, "Unable to open file", Toast.LENGTH_SHORT).show()
                    exception = 1
                }

                if (inputAsString.isNotEmpty() && exception == 0) {
                    val gson = GsonBuilder().create()
                    val type = object : TypeToken<Array<HashMap<String, String>>>() {}.type

                    try {
                        val moodEntries =
                            gson.fromJson<Array<HashMap<String, String>>>(inputAsString, type)

                        for (mood in moodEntries) {
                            val moodFeelings = when (mood["feelings"]) {
                                null -> ArrayList<String>()
                                else -> mood["feelings"]?.let { (it.split(",")) } as MutableList<String>
                            }
                            val moodActivities = when (mood["activities"]) {
                                null -> ArrayList<String>()
                                else -> mood["activities"]?.let { (it.split(",")) } as MutableList<String>
                            }
                            var date = "1987-11-06"
                            var exceptions = 0
                            try {
                                val format =
                                    DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)
                                val localDate = LocalDate.parse(mood["date"])
                                date = format.format(localDate)
                            } catch (e: Exception) {
                                exceptions++
                            }

                            try {
                                val format =
                                    DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)
                                val localDate = LocalDate.parse(
                                    mood["date"],
                                    DateTimeFormatter.ofPattern("MM/dd/yyyy")
                                )
                                date = format.format(localDate)
                                exceptions = 0
                            } catch (e: Exception) {
                                exceptions++
                            }

                            val time = mood["time"]?.substring(0, 5)

                            if (exceptions != 0) {
                                Toast.makeText(
                                    this,
                                    "Date must be of format yyyy-MM-dd",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            moodData.add(
                                MoodEntryModel(
                                    date.toString(),
                                    time,
                                    mood["mood"]?.let { Mood(it) },
                                    moodFeelings,
                                    moodActivities,
                                    mood["key"]?.let { UUID.randomUUID().toString() }
                                )
                            )
                        }
                        Toast.makeText(this, "File processed correctly", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(this, "Unable to process as JSON", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }
}