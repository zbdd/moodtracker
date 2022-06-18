package com.kalzakath.zoodle.interfaces

import com.kalzakath.zoodle.RecyclerViewAdaptor
import com.kalzakath.zoodle.model.MoodEntryModel

interface MainActivityInterface {
    fun startActivityActivities(moodEntry: MoodEntryModel)
    fun startActivitySettings()
    fun startActivityFrontPage(moodEntry: MoodEntryModel?)
    fun startActivityTrendView()
    fun startActivityFeelings(moodEntry: MoodEntryModel)

    fun setupRecycleView(): RecyclerViewAdaptor

    fun createMoodValuePicker(moodEntry: MoodEntryModel)
    fun createMoodEntryPicker()

    fun launchSignInEvent()
}