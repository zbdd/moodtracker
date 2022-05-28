package com.kalzakath.zoodle

import android.content.Context
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

@TestInstance(Lifecycle.PER_CLASS)
class RecyclerViewAdaptorTest {

    class TestViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)

    @MockK lateinit var recyclerViewAdaptor: RecyclerViewAdaptor
    @MockK lateinit var rowController: RowController
    @MockK lateinit var mockAdapter: RecyclerViewAdaptor
    @MockK lateinit var context: Context
    @MockK lateinit var layoutInflater: LayoutInflater

    @BeforeAll
    internal fun setup () {
        MockKAnnotations.init(this, relaxed = true)

        mockkStatic(LayoutInflater::class)
        val date = LocalDateTime.now()
        val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val timeFormat = DateTimeFormatter.ofPattern("HH:mm")
        val dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        val arrayMood = arrayListOf<RowEntryModel>()
        arrayMood.add(MoodEntryModel())
        arrayMood.add(MoodEntryModel())
        arrayMood.add(MoodEntryModel())
        arrayMood.add(MoodEntryModel())
        arrayMood.add(MoodEntryModel(
            dateFormat.format(date),
            timeFormat.format(date),
            Mood(),
            arrayListOf("nothing"),
            arrayListOf("nothing"),
            "testKey",
            dateTimeFormat.format(date)
        ))

        recyclerViewAdaptor = RecyclerViewAdaptor(
            { _, _ -> onItemDismissed() },
            { run { } },
            { _,_ -> run { } },
            { run { } },
            { run { } })

        rowController = RowController(recyclerViewAdaptor) { run {} }

        mockkStatic(Looper::class)
        val looper = mockk<Looper> {
            every { thread } returns Thread.currentThread()
        }

        every { Looper.getMainLooper() } returns looper
        every { LayoutInflater.from(context) } returns layoutInflater

        rowController.update(arrayMood)

    }

    @BeforeEach
    fun setupBeforeEach() {
        mockAdapter = spyk(RecyclerViewAdaptor(
            { _, _ -> onItemDismissed() },
            { run { } },
            { _,_ -> run { } },
            { run { } },
            { run { } })) {
            every { notifyDataSetChanged() } returns Unit
            every { notifyItemChanged(any()) } returns Unit
        }
    }

    @Test
    fun `can set viewHolder as a TestViewHolder`() {
        recyclerViewAdaptor.viewHolder = TestViewHolder(
            LayoutInflater.from(context).inflate(R.layout.mood_entry_layout, null, false)
        )
        assertEquals(TestViewHolder::class.java,recyclerViewAdaptor.viewHolder::class.java)
    }

    @Test
    fun `can check viewHolder was assigned as TestViewHolder`() {
        val itmView = mockk<View>()
        every { mockAdapter.viewHolder } returns TestViewHolder(itmView)
        assertEquals(TestViewHolder::class.java,mockAdapter.viewHolder::class.java)
    }

    @Test
    fun `can correctly GET moodlist array`() {
        assertEquals(0, mockAdapter.itemCount)
        rowController.update(arrayListOf(MoodEntryModel(), MoodEntryModel()))
        assertEquals(4, mockAdapter.itemCount)     // why 4 - 2x filter rows are made
    }

    @Test
    fun `create FileViewHolder`() {
        val cntx = mockk<Context>()
        val view = mockk<ViewGroup> {
            every { context } returns cntx
            every { findViewById<TextView>(any()) } returns mockk()
        }
        every { LayoutInflater.from(cntx).inflate(any<Int>(), view, false) } returns view
        val viewType = FilterEntryModel().viewType
        val holder = mockAdapter.onCreateViewHolder(view, viewType)
        assertEquals(FilterViewHolder::class, holder::class)
    }

    @Test
    fun `create MoodViewHolder`() {
        val cntx = mockk<Context>()
        val view = mockk<ViewGroup> {
            every { context } returns cntx
            every { findViewById<TextView>(any()) } returns mockk()
        }
        every { LayoutInflater.from(cntx).inflate(any<Int>(), view, false) } returns view
        val viewType = MoodEntryModel().viewType
        val holder = mockAdapter.onCreateViewHolder(view, viewType)
        assertEquals(MoodViewHolder::class, holder::class)
    }

    @Test
    fun `can update settings and this updates mood entries`() {
        Settings.moodMax = 100
        Settings.moodMode = Mood.MOOD_MODE_NUMBERS

        val data = arrayListOf<RowEntryModel>()
        for (i in 1..Random.nextInt(1,12)) {
            data.add(MoodEntryModel())
        }
        rowController.update(data)
        var moodToTest: MoodEntryModel? = null
        do {
            val row = mockAdapter.getItem(Random.nextInt(mockAdapter.itemCount - 1))
            if (row?.viewType == MoodEntryModel().viewType) moodToTest = row as MoodEntryModel
        } while (moodToTest == null)
        assertEquals(Mood.MOOD_MODE_FACES, moodToTest.mood!!.moodMode)

        //mockAdapter.updateListConfig()
        assertEquals(Settings.moodMode, moodToTest.mood!!.moodMode)
        assertEquals(Mood.MOOD_MODE_NUMBERS, moodToTest.mood!!.moodMode)
    }

    @Test
    fun `can add 5 MoodEntries to MoodList`() {
        val arrayMood = arrayListOf<RowEntryModel>()
        arrayMood.add(MoodEntryModel())
        arrayMood.add(MoodEntryModel())
        arrayMood.add(MoodEntryModel())
        arrayMood.add(MoodEntryModel())
        arrayMood.add(MoodEntryModel())

        rowController.update(arrayMood)
        verify(atLeast = 5) { mockAdapter.notifyDataSetChanged() }
        assertEquals(7, mockAdapter.itemCount)
        // +2 for the extra filter views automatically added
    }

    @Test
    fun `can successfully update a mood entry row`() {

        assert(mockAdapter.itemCount == 0)
        rowController.update(arrayListOf(MoodEntryModel()))
        mockAdapter.getItem(0)!!.key = "testkey"

        val moodVal = "3"
        val viewHolder = mockk<MoodViewHolder>()
        every { mockAdapter.viewHolder } returns viewHolder
        every { viewHolder.itemViewType } returns MoodEntryModel().viewType
        every { viewHolder.moodText.text } returns moodVal
        every { viewHolder.moodText.setBackgroundResource(0) } returns Unit

        val date = LocalDateTime.now()
        val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val timeFormat = DateTimeFormatter.ofPattern("HH:mm")
        val dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val mood = MoodEntryModel(
            dateFormat.format(date),
            timeFormat.format(date),
            Mood(moodVal),
            arrayListOf("test", "test2"),
            arrayListOf("test3", "test4"),
            "testkey",
            dateTimeFormat.format(date)
        )
        rowController.update(mood)

        var item: MoodEntryModel? = null
        for(i in 0..mockAdapter.itemCount) {
            val data = mockAdapter.getItem(i)
            if (data?.key == mood.key) {
                item = data as MoodEntryModel
                break
            }
        }

        assertEquals(item?.key, "testkey")
        assertEquals(item?.mood!!.value, moodVal)
        assertEquals(item.feelings, arrayListOf("test", "test2"))
        assertEquals(item.activities, arrayListOf("test3", "test4"))
        assertEquals(item.lastUpdated, dateTimeFormat.format(date))
        assertEquals(item.date, dateFormat.format(date))
        assertEquals(item.time, timeFormat.format(date))

    }

    @Test
    fun `can get two items from moodList, one of each RowEntryType`() {
        rowController.update(arrayListOf(MoodEntryModel()))
        assertEquals(MoodEntryModel().viewType, mockAdapter.getItemViewType(0))
    }

    @Test
    fun `count the size of the MoodList array`() {
        assertEquals(mockAdapter.itemCount, mockAdapter.itemCount)
        rowController.update(arrayListOf(MoodEntryModel()))
        // Why 2? Well, in creating a valid MoodEntry, a FilterView will be added (because it'll detect the TODAY condition)
        assertEquals(1, mockAdapter.itemCount)
    }

    @Test
    fun `assert onItemMove is always returning false`() {
        assertFalse(mockAdapter.onItemMove(Random.nextInt(),Random.nextInt()))
    }

    @Test
    fun `dismissing an MoodEntry calls removal and notify functions`() {
        val data = arrayListOf<RowEntryModel>()
        for (i in 1..Random.nextInt(4,12)) {
            data.add(MoodEntryModel())
        }
        rowController.update(data)

        var index: Int
        every { mockAdapter.notifyItemRemoved(any()) } returns Unit
        do {
            index = Random.nextInt(mockAdapter.itemCount - 1)
        } while (mockAdapter.getItem(index)!!.viewType != MoodEntryModel().viewType)

        mockAdapter.onItemDismiss(index)

        verify { mockAdapter.onItemDismiss(index) }
    }

    @Test
    fun `can change settings`() {
        val testMax = 42
        val testNumerals = 101
        Settings.moodMode = testNumerals
        Settings.moodMax = testMax

        assertEquals(testMax, Settings.moodMax)
        assertEquals(testNumerals, Settings.moodMode)
    }

    private fun onItemDismissed() {
        assert(true)
    }
}