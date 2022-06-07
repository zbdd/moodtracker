package com.kalzakath.zoodle

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.GsonBuilder
import java.time.LocalDate
import java.time.LocalDateTime

class FrontPageActivity : AppCompatActivity() {
    private lateinit var getActivitiesActivityResult: ActivityResultLauncher<Intent>
    private lateinit var getFeelingsActivityResult: ActivityResultLauncher<Intent>
    private lateinit var secureFileHandler: SecureFileHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_front_page)
        secureFileHandler = SecureFileHandler(applicationContext)

        val moodEntry = prepMoodEntry()

        initActivityListeners()
        initButtons(moodEntry)

    }

    private fun initActivityListeners() {
        getActivitiesActivityResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                val data = it.data?.getStringArrayListExtra("AvailableActivities")
                if (data != null) {
                    secureFileHandler.write(data as ArrayList<*>, "available.json")
                    val updateMoodEntry = it.data?.getSerializableExtra("MoodEntry") as MoodEntryModel

                }
            }

        getFeelingsActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val data = it.data?.getStringArrayListExtra("AvailableFeelings")
            if (data != null) {
                secureFileHandler.write(data as ArrayList<*>, "feelings.json")
                val moodEntryUpdate = it.data?.getSerializableExtra("MoodEntry") as MoodEntryModel
            }
        }
    }

    private fun startActivityActivities(moodEntry: MoodEntryModel) {
        val intent = Intent(this, ActivitiesActivity::class.java)
        val jsonArray = secureFileHandler.read("available.json")

        // Get activities that are stored in local json file
        if (jsonArray?.isNotEmpty() == true) {
            val gson = GsonBuilder().create()
            val activities = gson.fromJson(jsonArray, ArrayList::class.java)
            if (activities.isNotEmpty()) {
                val data = activities.filterIsInstance<String>() as ArrayList<String>
                intent.putStringArrayListExtra("AvailableActivities", data)
            }
        }

        intent.putExtra("MoodEntry", moodEntry)
        getActivitiesActivityResult.launch(intent)
    }

    private fun startActivityFeelings(moodEntry: MoodEntryModel) {
        val intent = Intent(this, FeelingsActivity::class.java)
        val jsonArray = secureFileHandler.read("feelings.json")

        // Get activities that are stored in local json file
        if (jsonArray?.isNotEmpty() == true) {
            val gson = GsonBuilder().create()
            val feelings = gson.fromJson(jsonArray, ArrayList::class.java)
            var data = ArrayList<String>()
            if (feelings.isNotEmpty()) data = feelings.filterIsInstance<String>() as ArrayList<String>
            intent.putStringArrayListExtra("AvailableFeelings", data)
        }

        intent.putExtra("MoodEntry", moodEntry)

        getFeelingsActivityResult.launch(intent)
    }

    private fun prepMoodEntry(): MoodEntryModel {
        return MoodEntryModel(LocalDate.now().toString(), LocalDateTime.now().toString().subSequence(0,5).toString())
    }

    private fun initButtons(moodEntry: MoodEntryModel) {
        val moodVeryBad: ImageButton = findViewById(R.id.ibFrontVeryBad)
        val moodBad: ImageButton = findViewById(R.id.ibFrontBad)
        val moodOk: ImageButton = findViewById(R.id.ibFrontOk)
        val moodGood: ImageButton = findViewById(R.id.ibFrontGood)
        val moodVeryGood: ImageButton = findViewById(R.id.ibFrontVeryGood)

        val sleepVeryBad: ImageButton = findViewById(R.id.ibSleepVeryBad)
        val sleepBad: ImageButton = findViewById(R.id.ibSleepBad)
        val sleepOk: ImageButton = findViewById(R.id.ibSleepOk)
        val sleepGood: ImageButton = findViewById(R.id.ibSleepGood)
        val sleepVeryGood: ImageButton = findViewById(R.id.ibSleepVeryGood)

        val medicationCheck: CheckBox = findViewById(R.id.cbFrontMedication)

        val activityTitle: TextView = findViewById(R.id.tvFrontActivitiesTitle)
        val feelingsTitle: TextView = findViewById(R.id.tvFrontFeelingsTitle)

        val btnMoodArray = arrayOf(moodVeryBad, moodBad, moodOk, moodGood, moodVeryGood)
        val btnSleepArray = arrayOf(sleepVeryBad, sleepBad, sleepOk, sleepGood, sleepVeryGood)
        var moodChoice: ImageButton
        var sleepChoice: ImageButton

        var medicationChoice = medicationCheck.isChecked

        btnMoodArray.forEach { btn -> btn.setOnClickListener {
            it.setBackgroundColor(Color.WHITE)
            moodEntry.mood!!.value = (btnMoodArray.indexOf(it) + 1).toString()
            btnMoodArray.forEach { ib ->
                if (ib != it) ib.setBackgroundColor(Color.DKGRAY)
            } } }

        btnSleepArray.forEach { btn -> btn.setOnClickListener {
            it.setBackgroundColor(Color.WHITE)
            sleepChoice = btn
            btnSleepArray.forEach { ib ->
                if (ib != it) ib.setBackgroundColor(Color.DKGRAY)
            } } }

        activityTitle.setOnClickListener {
            startActivityActivities(moodEntry)
        }

        feelingsTitle.setOnClickListener {
            startActivityFeelings(moodEntry)
        }
    }
}