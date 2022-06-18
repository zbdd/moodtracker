package com.kalzakath.zoodle

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.GsonBuilder
import com.kalzakath.zoodle.model.MoodEntryModel
import com.kalzakath.zoodle.model.update
import com.kalzakath.zoodle.model.updateDateTime
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.logging.Logger

class FrontPageActivity : AppCompatActivity() {
    private lateinit var getActivitiesActivityResult: ActivityResultLauncher<Intent>
    private lateinit var getFeelingsActivityResult: ActivityResultLauncher<Intent>
    private lateinit var secureFileHandler: SecureFileHandler
    private lateinit var securityHandler: SecurityHandler
    private val log = Logger.getLogger(MainActivity::class.java.name + "****************************************")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_front_page)

        securityHandler = SecurityHandler(applicationContext)
        secureFileHandler = SecureFileHandler(securityHandler)

        val data = intent.getSerializableExtra("MoodEntry")
        val moodEntry = if (data != null) data as MoodEntryModel
        else prepMoodEntry()

        initActivityListeners(moodEntry)
        if (data != null) initButtons(moodEntry, true)
        else initButtons(moodEntry)
    }

    private fun initActivityListeners(moodEntry: MoodEntryModel) {

        getActivitiesActivityResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                val data = it.data?.getStringArrayListExtra("AvailableActivities")
                if (data != null) {
                    secureFileHandler.write(data as ArrayList<*>, "available.json")
                    moodEntry.update(it.data?.getSerializableExtra("MoodEntry") as MoodEntryModel)
                    updateButtons(moodEntry)
                }
            }

        getFeelingsActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val data = it.data?.getStringArrayListExtra("AvailableFeelings")
            if (data != null) {
                secureFileHandler.write(data as ArrayList<*>, "feelings.json")
                moodEntry.update(it.data?.getSerializableExtra("MoodEntry") as MoodEntryModel)
                updateButtons(moodEntry)
            }
        }
    }

    private fun startActivityActivities(moodEntry: MoodEntryModel) {
        val intent = Intent(this, ActivitiesActivity::class.java)
        val jsonArray = secureFileHandler.read("available.json")

        // Get activities that are stored in local json file
        if (jsonArray.isNotEmpty()) {
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
        if (jsonArray.isNotEmpty()) {
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
        val timeFormat = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH)
        return MoodEntryModel(LocalDate.now().toString(), timeFormat.format(LocalDateTime.now()))
    }

    private fun updateButtons(moodEntry: MoodEntryModel) {
        val activities: TextView = findViewById(R.id.tvFrontActivities)
        val feelings: TextView = findViewById(R.id.tvFrontFeelings)
        val activityTitle: TextView = findViewById(R.id.tvFrontActivitiesTitle)
        val feelingsTitle: TextView = findViewById(R.id.tvFrontFeelingsTitle)
        val medicationCheck: CheckBox = findViewById(R.id.cbFrontMedication)

        if (moodEntry.activities.isNotEmpty()) {
            activityTitle.text = resources.getString(R.string.main_row_activities)
            activities.text = moodEntry.activities.toString().removeSurrounding("[","]")
            activities.visibility = View.VISIBLE
        } else {
            activities.text = ""
            activities.visibility = View.INVISIBLE
            activityTitle.text = resources.getString(R.string.activities_add_new)
        }

        if (moodEntry.feelings.isNotEmpty()) {
            feelingsTitle.text = resources.getString(R.string.main_row_feelings)
            feelings.text = moodEntry.feelings.toString().removeSurrounding("[","]")
            feelings.visibility = View.VISIBLE
        } else {
            feelings.text = ""
            feelings.visibility = View.INVISIBLE
            feelingsTitle.text = resources.getString(R.string.feelings_add_new)
        }

        medicationCheck.isChecked = moodEntry.medication
    }

    private fun initButtons(moodEntry: MoodEntryModel, preset: Boolean = false) {
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

        val activityTitle: LinearLayout = findViewById(R.id.llActivities)
        val feelingsTitle: LinearLayout = findViewById(R.id.llFeelings)

        val mainActivity: Button = findViewById(R.id.bFrontSeeData)

        val btnMoodArray = arrayOf(moodVeryBad, moodBad, moodOk, moodGood, moodVeryGood)
        val btnSleepArray = arrayOf(sleepVeryBad, sleepBad, sleepOk, sleepGood, sleepVeryGood)

        val date: TextView = findViewById(R.id.tvFrontDate)
        val time: TextView = findViewById(R.id.tvFrontTime)

        date.text = moodEntry.date
        time.text = moodEntry.time

        if (preset) {
            btnMoodArray.indices.forEach { if(it == moodEntry.mood-1) btnMoodArray[it].setBackgroundColor(Color.WHITE) else btnMoodArray[it].setBackgroundColor(Color.DKGRAY) }
            btnSleepArray.indices.forEach { if(it == moodEntry.sleep-1) btnSleepArray[it].setBackgroundColor(Color.WHITE) else btnSleepArray[it].setBackgroundColor(Color.DKGRAY) }
            updateButtons(moodEntry)
        }

        val dtPicker = DateTimePicker()
        dtPicker.onUpdateListener = {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            val timeFormat = SimpleDateFormat("HH:mm", Locale.ENGLISH)

            date.text = dateFormat.format(it.time)
            time.text = timeFormat.format(it.time)
            moodEntry.updateDateTime(it)
        }

        date.setOnClickListener {
            dtPicker.show(this)
        }

        time.setOnClickListener {
            dtPicker.show(this)
        }

        btnMoodArray.forEach { btn -> btn.setOnClickListener {
            it.setBackgroundColor(resources.getColor(R.color.white, theme))
            moodEntry.mood = btnMoodArray.indexOf(it) + 1
            btnMoodArray.forEach { ib ->
                if (ib != it) ib.setBackgroundColor(resources.getColor(R.color.dark_gray, theme))
            } } }

        btnSleepArray.forEach { btn -> btn.setOnClickListener {
            it.setBackgroundColor(resources.getColor(R.color.white, theme))
            moodEntry.sleep = btnSleepArray.indexOf(it) + 1
            btnSleepArray.forEach { ib ->
                if (ib != it) ib.setBackgroundColor(resources.getColor(R.color.dark_gray, theme))
            } } }

        activityTitle.setOnClickListener {
            startActivityActivities(moodEntry)
        }

        feelingsTitle.setOnClickListener {
            startActivityFeelings(moodEntry)
        }

        mainActivity.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("MoodEntry", moodEntry)
            setResult(RESULT_OK, intent)
            finish()
        }

        medicationCheck.setOnClickListener {
            moodEntry.medication = medicationCheck.isChecked
        }
    }
}