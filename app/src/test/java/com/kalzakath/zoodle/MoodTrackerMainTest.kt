package com.kalzakath.zoodle

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.io.File
import java.lang.reflect.Modifier

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MoodTrackerMainTest {

    private lateinit var main: MoodTrackerMain
    private lateinit var secureFH: SecureFileHandler
    private lateinit var testJsonArrayString: String
    private lateinit var testJsonArray: ArrayList<MoodEntryModel>
    private lateinit var rowController: RowController

    @BeforeAll
    fun setup() {
        val gson = Gson()
        testJsonArray = arrayListOf(MoodEntryModel(), MoodEntryModel())
        testJsonArrayString = gson.toJson(testJsonArray)

        val context = mockk<Context>() {
            every { filesDir } returns File("")
        }
        val securityHandler = SecurityHandler(context)
        secureFH = mockk<SecureFileHandler>() {
            every { read() } returns testJsonArrayString
            every { read("settings.json") } returns ""
        }
        rowController = RowController()
        val onlineDH = FirebaseConnectionHandler()
        main = MoodTrackerMain(secureFH,rowController,onlineDH)
    }

    @Test
    fun convertToArrayList() {
        val gson = Gson()
        val testArray = arrayListOf(MoodEntryModel(), MoodEntryModel())
        val jsonString = gson.toJson(testArray)

        val jsonArray = main.convertToArrayList(jsonString)
        jsonArray.indices.forEach { assert((jsonArray[it] as MoodEntryModel).compare(testArray[it])) }
    }

    @Test
    fun loadLocalData() {
        main.loadLocalData()
        rowController.mainRowEntryList.indices.forEach { (rowController.mainRowEntryList[it] as MoodEntryModel).compare(testJsonArray[it]) }
    }

    @Test
    fun loadOnlineData() {
    }

    @Test
    fun loadSettingData() {
        val gsonBuilder = GsonBuilder().excludeFieldsWithModifiers(Modifier.TRANSIENT).create()

        val testMoodMode = 100
        val testMoodMax = 1000
        Settings.moodMode = testMoodMode
        Settings.moodMax = testMoodMax
        val testJsonSettingsString = gsonBuilder.toJson(Settings)
        every { secureFH.read("settings.json") } returns testJsonSettingsString
        Settings.setDefaultSettings()

        assert(Settings.moodMax == Settings.Default.moodMax)
        assert(Settings.moodMode == Settings.Default.moodMode)

        main.loadSettingData()

        assert(Settings.moodMax == testMoodMax)
        assert(Settings.moodMode == testMoodMode)

    }

    @Test
    fun onUpdateFromDataController() {
    }

    @Test
    fun saveLocalDataToOnline() {
    }

    @Test
    fun onUpdateFromDatabase() {
    }

    @Test
    fun onLoginUpdateFromDatabase() {
    }

    @Test
    fun readSettingsDataFromJson() {
    }
}