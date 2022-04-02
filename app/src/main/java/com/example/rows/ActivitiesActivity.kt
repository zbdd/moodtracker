package com.example.rows

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.auth.data.model.Resource

class ActivitiesActivity: AppCompatActivity()  {

    private lateinit var activitiesRecycleView: RecyclerView
    private lateinit var availableRecycleView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activities_recycle_view)
        println("Loading...")
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
        val adaptor = ActivitiesRecycleViewAdaptor(applicationContext, selectedActivities as List<String>)
        activitiesRecycleView.adapter = adaptor

        availableRecycleView = findViewById(R.id.rvAvailable)
        availableRecycleView.layoutManager = LinearLayoutManager(this)
        val availAdaptor = ActivitiesRecycleViewAdaptor(applicationContext, availableActivities.toList())
        availableRecycleView.adapter = availAdaptor
    }
}