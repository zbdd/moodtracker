package com.example.rows

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ActivitiesActivity: AppCompatActivity()  {

    private lateinit var activitiesRecycleView: RecyclerView
    private lateinit var selectedAdaptor: ActivitiesRecycleViewAdaptor
    private lateinit var availableRecycleView: RecyclerView
    private lateinit var availableAdaptor: ActivitiesRecycleViewAdaptor
    private lateinit var moodEntry: MoodEntryModel
    private var availableActivities: ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activities_recycle_view)

        val bConfirm: Button = findViewById(R.id.bActivitiesConfirm)
        val bActivityAddNew: ImageButton = findViewById(R.id.bActivityAddNew)
        val arrayData = intent.getStringArrayListExtra("AvailableActivities")
        val bActivityAdd: Button = findViewById(R.id.bActivityAdd)
        val bActivityCancel: Button = findViewById(R.id.bActivityCancel)
        val llAddActivity: LinearLayout = findViewById(R.id.llAddNewActivity)
        val etAddActivity: EditText = findViewById(R.id.etNewActivityName)
        val bDeleteActivity: Button = findViewById(R.id.bActivitiesDelete)
        val bRestoreDefaults: Button = findViewById(R.id.bRestoreDefaults)

        moodEntry = intent.getSerializableExtra("MoodEntry") as MoodEntryModel

        if (arrayData != null) {
            availableActivities.addAll(arrayData)
        }

        // Remove accidental duplicates
        for (item in moodEntry.activities) {
            availableActivities.remove(item)
        }

        activitiesRecycleView = findViewById(R.id.rvSelected)
        activitiesRecycleView.layoutManager = LinearLayoutManager(this)
        selectedAdaptor = ActivitiesRecycleViewAdaptor(applicationContext, moodEntry.activities,
        { activity -> moveToAvailable(activity) },
        { activity -> removeFromAvailable(activity)})

        activitiesRecycleView.adapter = selectedAdaptor

        availableRecycleView = findViewById(R.id.rvAvailable)
        availableRecycleView.layoutManager = LinearLayoutManager(this)
        availableAdaptor = ActivitiesRecycleViewAdaptor(applicationContext, availableActivities.toMutableList(),
            { activity -> moveToSelected(activity) },
            { activity -> removeFromAvailable(activity)})

        availableRecycleView.adapter = availableAdaptor

        if (availableActivities.size == 0) {
            restoreDefaults()
        }

        bConfirm.setOnClickListener {
            val finishIntent = Intent()
            finishIntent.putExtra("MoodEntry", moodEntry)
            finishIntent.putStringArrayListExtra("AvailableActivities", availableActivities)
            setResult(RESULT_OK, finishIntent)
            finish()
        }

        bActivityAddNew.setOnClickListener {
            llAddActivity.visibility = View.VISIBLE
        }

        bActivityCancel.setOnClickListener {
            llAddActivity.visibility = View.INVISIBLE
            etAddActivity.setText(applicationContext.resources.getString(R.string.activities_add_new_activity_name))
        }

        bActivityAdd.setOnClickListener {
            selectedAdaptor.addItem(etAddActivity.text.toString())

            llAddActivity.visibility = View.INVISIBLE
            etAddActivity.setText(applicationContext.resources.getString(R.string.activities_add_new_activity_name))
        }

        bDeleteActivity.setOnClickListener {
            selectedAdaptor.toggleDeleteButton()
            availableAdaptor.toggleDeleteButton()
        }

        bRestoreDefaults.setOnClickListener {
            restoreDefaults()
        }
    }

    private fun removeFromAvailable(activity: String) {
        availableActivities.remove(activity)
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

    private fun restoreDefaults() {
        val stringArray =
            applicationContext.resources.getStringArray(R.array.available_activities)
        for (activity in stringArray) {
            availableAdaptor.addItem(activity)
            if (!availableActivities.contains(activity)) availableActivities.add(activity)
        }

    }
}