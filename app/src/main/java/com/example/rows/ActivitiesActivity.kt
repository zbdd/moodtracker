package com.example.rows

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ActivitiesActivity: AppCompatActivity()  {

    private lateinit var activitiesRecycleView: RecyclerView
    private lateinit var selectedAdaptor: ActivitiesRecycleViewAdaptor
    private lateinit var availableRecycleView: RecyclerView
    private lateinit var availableAdaptor: ActivitiesRecycleViewAdaptor
    private lateinit var moodEntry: MoodEntryModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activities_recycle_view)

        //selectedActivities = intent.getStringArrayListExtra("Activities") as ArrayList<String>
        moodEntry = intent.getSerializableExtra("MoodEntry") as MoodEntryModel

        val stringArray = applicationContext.resources.getStringArray(R.array.available_activities)
        val availableActivities = stringArray.toMutableList()

        for (item in moodEntry.activities) {
            availableActivities.remove(item)
        }

        activitiesRecycleView = findViewById(R.id.rvSelected)
        activitiesRecycleView.layoutManager = LinearLayoutManager(this)
        selectedAdaptor = ActivitiesRecycleViewAdaptor(applicationContext, moodEntry.activities) { activity ->
            moveToAvailable(
                activity
            )
        }
        activitiesRecycleView.adapter = selectedAdaptor

        availableRecycleView = findViewById(R.id.rvAvailable)
        availableRecycleView.layoutManager = LinearLayoutManager(this)
        availableAdaptor = ActivitiesRecycleViewAdaptor(applicationContext, availableActivities.toMutableList()) { activity ->
            moveToSelected(
                activity
            )
        }
        availableRecycleView.adapter = availableAdaptor

        val bConfirm: Button = findViewById(R.id.bConfirm)
        bConfirm.setOnClickListener {
            val finishIntent = Intent()
            finishIntent.putExtra("MoodEntry", moodEntry)
            setResult(RESULT_OK, finishIntent)
            finish()
        }
    }

    private fun moveToSelected(activity: String) {
        selectedAdaptor.run {
            addItem(activity)
        }
    }

    private fun moveToAvailable(activity: String) {
        availableAdaptor.run {
            addItem(activity)
        }
    }
}