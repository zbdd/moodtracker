package com.kalzakath.zoodle

import android.graphics.Color
import android.os.Bundle
import android.widget.CheckBox
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import java.time.LocalDate
import java.time.LocalDateTime

class FrontPageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_front_page)

        val moodEntry = prepMoodEntry()
        initButtons(moodEntry)
        initActivities(moodEntry)
    }

    private fun initActivities(moodEntry: MoodEntryModel) {

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
    }
}