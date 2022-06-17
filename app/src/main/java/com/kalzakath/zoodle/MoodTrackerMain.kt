package com.kalzakath.zoodle

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.kalzakath.zoodle.interfaces.*
import java.lang.reflect.Modifier

class MoodTrackerMain(secureFileHandler: SecureFileHandler,
                      rowController: DataController,
                      onlineDataHandler: OnlineDataHandler
                        ): DataControllerEventListener, OnlineDataHandlerEventListener,
                        MoodTracker {
    private val _secureFileHandler = secureFileHandler
    private val _rowController = rowController
    private val _onlineDataHandler = onlineDataHandler

    init {
        _rowController.registerForUpdates(this)
        _onlineDataHandler.registerForUpdates(this)

        loadLocalData()
        loadOnlineData()
        loadSettingData()
    }

    override fun convertToArrayList(jsonString: String): ArrayList<RowEntryModel> {
        val myArrayList = arrayListOf<RowEntryModel>()

        if (jsonString.isNotEmpty()) {
            val gson = GsonBuilder().create()
            val type = object : TypeToken<Array<MoodEntryModel>>() {}.type
            val moodEntries = gson.fromJson<Array<MoodEntryModel>>(jsonString, type)

            for (x in moodEntries.indices) {
                myArrayList.add(moodEntries[x])
            }
        }

        return myArrayList
    }

    override fun loadLocalData() {
        val data = _secureFileHandler.read()
        if (data.isNotEmpty()) _rowController.update(convertToArrayList(data), false)
    }

    override fun loadOnlineData() {
        val data = _onlineDataHandler.read()
        if (data.isNotEmpty()) _rowController.update(data, false)
    }

    override fun loadSettingData() {
        readSettingsDataFromJson(_secureFileHandler.read("settings.json"))
    }

    override fun onUpdateFromDataController(event: RowControllerEvent) {
        // this writes a local dump of the whole list
        _secureFileHandler.write(_rowController.mainRowEntryList)

        // this is more specific in it's operations
        when (event.type) {
            RowControllerEvent.REMOVE -> { _onlineDataHandler.remove(event.data) }
            else -> { _onlineDataHandler.write (event.data) }
        }
    }

    override fun saveLocalDataToOnline() {
        _onlineDataHandler.write(_rowController.mainRowEntryList)
    }

    override fun onUpdateFromDatabase(data: ArrayList<RowEntryModel>) {
        _rowController.update(data)
    }

    override fun onLoginUpdateFromDatabase(result: String) {
        when (result) {
            "SUCCESS" -> {
                loadOnlineData()
                saveLocalDataToOnline()
            }
        }
    }

    override fun readSettingsDataFromJson(jsonSettings: String?) {
        val gson = GsonBuilder().excludeFieldsWithModifiers(Modifier.TRANSIENT).create()
        val type = object : TypeToken<Settings>() {}.type
        val data = gson.fromJson<Settings>(jsonSettings, type)
        if (data != null) {
            Settings.moodMode = data.moodMode
            Settings.moodMax = data.moodMax
        }
    }

}