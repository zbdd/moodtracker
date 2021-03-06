package com.kalzakath.zoodle.interfaces

interface MoodTracker {
    fun convertToArrayList(jsonString: String): ArrayList<RowEntryModel>
    fun readSettingsDataFromJson(jsonSettings: String?)

    fun loadLocalData()
    fun loadOnlineData()
    fun loadSettingData()
    fun saveLocalDataToOnline()
}