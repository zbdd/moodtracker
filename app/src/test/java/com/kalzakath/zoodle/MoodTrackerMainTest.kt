package com.kalzakath.zoodle

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.kalzakath.zoodle.interfaces.OnlineDataHandler
import com.kalzakath.zoodle.interfaces.RowEntryModel
import com.kalzakath.zoodle.model.MoodEntryModel
import com.kalzakath.zoodle.model.compare
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.lang.reflect.Modifier

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MoodTrackerMainTest {

    private lateinit var main: MoodTrackerMain
    private lateinit var secureFH: SecureFileHandler
    private lateinit var testJsonArrayString: String
    private lateinit var testJsonArray: ArrayList<MoodEntryModel>
    private lateinit var rowController: RowController
    private lateinit var onlineDataHandler: OnlineDataHandler

    @BeforeAll
    fun setup() {
        val gson = Gson()
        testJsonArray = arrayListOf(MoodEntryModel(), MoodEntryModel())
        testJsonArrayString = gson.toJson(testJsonArray)

        secureFH = mockk {
            every { read() } returns testJsonArrayString
            every { read("settings.json") } returns ""
        }
        rowController = RowController()
        onlineDataHandler = mockk<FirebaseConnectionHandler>() {
            every { registerForUpdates(any()) } returns Unit
            every { read() } returns arrayListOf()
        }
        main = MoodTrackerMain(secureFH,rowController,onlineDataHandler)
    }

    @AfterEach
    fun setdown() {
        rowController.mainRowEntryList.clear()
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
        main.loadOnlineData()
        verify { secureFH.read() }

        val testArray: ArrayList<RowEntryModel> = arrayListOf(MoodEntryModel())
        every { onlineDataHandler.read() } returns testArray

        main.loadOnlineData()
        assert(rowController.indexOf(testArray[0]) != -1)
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
        val testArray: ArrayList<RowEntryModel> = arrayListOf(MoodEntryModel(), MoodEntryModel())
        val testEventRemove = RowControllerEvent(testArray,RowControllerEvent.REMOVE)
        every { secureFH.write(arrayListOf<RowEntryModel>()) } returns true
        every { onlineDataHandler.write(any()) } returns Unit
        every { onlineDataHandler.remove(any()) } returns Unit

        main.onUpdateFromDataController(testEventRemove)

        verify { secureFH.write(arrayListOf<RowEntryModel>()) }
        verify { onlineDataHandler.remove(testArray) }
        verify(exactly = 0) { onlineDataHandler.write(testArray) }

        val testEventOther = RowControllerEvent(testArray,RowControllerEvent.ADDITION)
        main.onUpdateFromDataController(testEventOther)

        verify { onlineDataHandler.write(testArray) }
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