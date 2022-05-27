package com.kalzakath.zoodle

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class RowControllerTest {

    @MockK lateinit var testRowController: RowController
    @MockK lateinit var testRVAdaptor: RecyclerViewAdaptor
    //private val Log = Logger.getLogger(MainActivity::class.java.name)

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        testRVAdaptor = spyk(
            RecyclerViewAdaptor(
                { _, _ -> run { } },
                { run { } },
                { _,_ -> run { } },
                { run { } },
                { run { } })
        ) {
            every { notifyDataSetChanged() } returns Unit
            every { notifyItemChanged(any()) } returns Unit
        }

        testRowController = RowController(testRVAdaptor)
    }

    @Test
    fun `update a single MoodEntryModel in MoodCommander`() {
        val testArray = ArrayList<RowEntryModel>(0)
        Array(10) { testArray.add(MoodEntryModel()) }
        testRowController.add(testArray)

        val testUpdateMoodEntryModel = MoodEntryModel(
            "2020-12-1",
            "09:09",
            Mood("5")
        )
        testUpdateMoodEntryModel.key = testArray[6].key
        testUpdateMoodEntryModel.lastUpdated = LocalDateTime.now().plusSeconds(1).toString()

        val testUpdateArray = ArrayList<RowEntryModel>(1)
        testUpdateArray.add(testUpdateMoodEntryModel)

        assert(testArray.size == 10)
        assert(testArray[0].javaClass == MoodEntryModel().javaClass)
        assert(testArray[6] != testUpdateMoodEntryModel)

        testRowController.update(testUpdateArray)
        val compareMood = testRowController.get(6) as MoodEntryModel

        assert(compareMood.compare(testUpdateMoodEntryModel))
        verify(atMost = 1) { testRVAdaptor.notifyItemChanged(6) }
    }
}