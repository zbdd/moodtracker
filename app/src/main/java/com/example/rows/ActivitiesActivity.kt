package com.example.rows

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.auth.data.model.Resource

class ActivitiesActivity: AppCompatActivity()  {

    private lateinit var activitiesRecycleView: RecyclerView
    private lateinit var selectedAdaptor: ActivitiesRecycleViewAdaptor
    private lateinit var availableRecycleView: RecyclerView
    private lateinit var availableAdaptor: ActivitiesRecycleViewAdaptor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activities_recycle_view)

        val selectedActivities = intent.getStringArrayListExtra("Activities")
        val stringArray = applicationContext.resources.getStringArray(R.array.available_activities)
        var availableActivities = stringArray.toMutableList()
        if (selectedActivities != null) {
            for (item in selectedActivities) {
                availableActivities.remove(item)
            }
        }

        activitiesRecycleView = findViewById(R.id.rvSelected)
        activitiesRecycleView.layoutManager = LinearLayoutManager(this)
        selectedAdaptor = ActivitiesRecycleViewAdaptor(applicationContext, selectedActivities as MutableList<String>) { activity ->
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