package com.kalzakath.zoodle.interfaces

import com.kalzakath.zoodle.RowEntryModel

interface MoodTracker {
    fun convertToArrayList(jsonString: String): ArrayList<RowEntryModel>
    fun readSettingsDataFromJson(jsonSettings: String?)

    fun loadLocalData()
    fun loadOnlineData()
    fun loadSettingData()
    fun saveLocalDataToOnline()
}