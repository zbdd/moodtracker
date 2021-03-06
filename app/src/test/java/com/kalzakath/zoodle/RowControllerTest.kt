package com.kalzakath.zoodle

import com.kalzakath.zoodle.interfaces.DataController
import com.kalzakath.zoodle.interfaces.RowEntryModel
import com.kalzakath.zoodle.model.MoodEntryModel
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import io.mockk.spyk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class RowControllerTest {

    @MockK lateinit var testRowController: DataController
    @MockK lateinit var testRVAdaptor: RecyclerViewAdaptor
    //private val Log = Logger.getLogger(MainActivity::class.java.name)

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        testRowController = spyk(RowController())
    }

    @Test
    fun `add a row and retrieve it`() {
        val testDate = "2020-11-11"
        val testTime = "20:20"
        val moodEntry = MoodEntryModel(testDate,testTime)

        assert(testRowController.size() == 0)
        testRowController.add(moodEntry)

        assert(testRowController.size() == 1)
        assert(testRowController.get(0) == moodEntry)
    }

    @Test
    fun `delete a row`() {
        val testMood = MoodEntryModel()
        testRowController.add(testMood)

        assert(testRowController.size() == 1)
        testRowController.remove(testMood)
        assert(testRowController.size() == 0)
    }

    @Test
    fun `add a list and delete a list`() {
        val listToDelete: ArrayList<RowEntryModel> = arrayListOf()
        for (item in 1..12) { listToDelete.add(MoodEntryModel()) }

        assert(testRowController.size() == 0)
        testRowController.add(listToDelete)
        assert(testRowController.size() == 12)
        testRowController.remove(listToDelete)
        assert(testRowController.size() == 0)
    }

    @Test
    fun `update a single MoodEntryModel in MoodCommander`() {
        val testArray = ArrayList<RowEntryModel>(0)
        Array(10) { testArray.add(MoodEntryModel()) }
        testRowController.add(testArray)

        val testUpdateMoodEntryModel = MoodEntryModel(
            "2020-12-01",
            "09:09",
            5
        )
        testUpdateMoodEntryModel.key = testArray[6].key
        testUpdateMoodEntryModel.lastUpdated = LocalDateTime.now().plusSeconds(1).toString()

        assert(testArray.size == 10)
        assert(testArray[0].javaClass == MoodEntryModel().javaClass)
        assert(testArray[6] != testUpdateMoodEntryModel)

        testRowController.update(testUpdateMoodEntryModel)
        val compareMood = testRowController.get(testRowController.indexOf(testArray[6])) as MoodEntryModel

        assert(compareMood.key == testUpdateMoodEntryModel.key)
        assert(compareMood.date == testUpdateMoodEntryModel.date)
        assert(compareMood.mood == testUpdateMoodEntryModel.mood)

    }
}