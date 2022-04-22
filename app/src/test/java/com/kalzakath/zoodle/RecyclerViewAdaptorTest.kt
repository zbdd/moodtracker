package com.kalzakath.zoodle

import android.content.Context
import android.content.res.Resources
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.auth.data.model.Resource
import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.*
import java.lang.Exception
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.concurrent.thread
import kotlin.random.Random
import kotlin.random.nextInt

@TestInstance(Lifecycle.PER_CLASS)
class RecyclerViewAdaptorTest {

    class TestViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    }

    companion object {
        private var settings = Settings()

    }
    @MockK lateinit var recyclerViewAdaptor: RecyclerViewAdaptor
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

        val arrayMood = arrayListOf<MoodEntryModel>()
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
            "testkey",
            dateTimeFormat.format(date)
        ))

        recyclerViewAdaptor = RecyclerViewAdaptor(
            { moodEntry, moodList -> onItemDismissed(moodEntry, moodList) },
            { moodEntries -> writeEntrytoFile(moodEntries); updateDatabaseEntry(moodEntries) },
            { mood -> setupMoodPicker(mood) },
            { moodEntry -> startActivityActivities(moodEntry) },
            { moodEntry -> startActivityFeelings(moodEntry) },
            settings)


        mockkStatic(Looper::class)
        val looper = mockk<Looper> {
            every { thread } returns Thread.currentThread()
        }

        every { Looper.getMainLooper() } returns looper
        every { LayoutInflater.from(context) } returns layoutInflater

        mockAdapter.updateList(arrayMood)

    }

    @BeforeEach
    fun setupBeforeEach() {
        mockAdapter = spyk(RecyclerViewAdaptor(
            { moodEntry, moodList -> onItemDismissed(moodEntry, moodList) },
            { moodEntries -> writeEntrytoFile(moodEntries); updateDatabaseEntry(moodEntries) },
            { mood -> setupMoodPicker(mood) },
            { moodEntry -> startActivityActivities(moodEntry) },
            { moodEntry -> startActivityFeelings(moodEntry) },
            settings)) {
            every { notifyDataSetChanged() } returns Unit
            every { notifyItemChanged(any<Int>()) } returns Unit
        }
    }

    @Test
    fun `can set viewHolder as a TestViewHolder`() {
        recyclerViewAdaptor.viewHolder = TestViewHolder(LayoutInflater.from(context).inflate(R.layout.mood_entry_layout, null, false))
        assertEquals(TestViewHolder::class.java,recyclerViewAdaptor.viewHolder::class.java)
    }

    @Test
    fun `can check viewHolder was assigned as TestViewHolder`() {
        var itmView = mockk<View>()
        every { mockAdapter.viewHolder } returns TestViewHolder(itmView)
        assertEquals(TestViewHolder::class.java,mockAdapter.viewHolder::class.java)
    }

    @Test
    fun `can correctly GET moodlist array`() {
        assertEquals(0, mockAdapter.getMoodList().size)
        mockAdapter.updateList(arrayListOf(MoodEntryModel(), MoodEntryModel()))
        assertEquals(2, mockAdapter.getMoodList().size)
    }

    @Test
    fun `create FileViewHolder`() {
        val cntx = mockk<Context>()
        val view = mockk<ViewGroup> {
            every { context } returns cntx
            every { findViewById<TextView>(any<Int>()) } returns mockk<TextView>()
        }
        every { LayoutInflater.from(cntx).inflate(any<Int>(), view, false) } returns view
        val position = 1
        val holder = mockAdapter.onCreateViewHolder(view, position)
        assertEquals(FilterViewHolder::class, holder::class)

    }

    @Test
    fun `can update settings`() {
        var testSettings = Settings()
        testSettings.mood_max = "100"
        testSettings.mood_numerals = "test"

        recyclerViewAdaptor.updateListConfig(testSettings)
        assertEquals("100", recyclerViewAdaptor.settings.mood_max)
        assertEquals("test", recyclerViewAdaptor.settings.mood_numerals)
    }

    @Test
    fun `can add 5 MoodEntries to MoodList`() {
        val arrayMood = arrayListOf<MoodEntryModel>()
        arrayMood.add(MoodEntryModel())
        arrayMood.add(MoodEntryModel())
        arrayMood.add(MoodEntryModel())
        arrayMood.add(MoodEntryModel())
        arrayMood.add(MoodEntryModel())

        mockAdapter.updateList(arrayMood)
        verify(atLeast = 5) { mockAdapter.notifyDataSetChanged() }
        assertEquals(5, mockAdapter.getMoodList().size)
    }

    @Test
    fun `can successfully update a mood entry row`() {

        assert(mockAdapter.getMoodList().size == 0)
        mockAdapter.updateList(arrayListOf(MoodEntryModel()))
        mockAdapter.getMoodList()[0].key = "testkey"

        val moodVal = "3"
        val viewHolder = mockk<MoodEntryViewHolder>()
        every { mockAdapter.viewHolder } returns viewHolder
        every { viewHolder.itemViewType } returns RowEntryModel.MOOD_ENTRY_TYPE
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
        mockAdapter.updateMoodEntry(mood)

        val list = mockAdapter.getMoodList()
        val index = list.indexOf(mood)
        val tMood = list[index]
        assert(index != -1)
        assertEquals(tMood.key, "testkey")
        assertEquals(tMood.mood!!.value, moodVal)
        assertEquals(tMood.feelings, arrayListOf("test", "test2"))
        assertEquals(tMood.activities, arrayListOf("test3", "test4"))
        assertEquals(tMood.lastUpdated, dateTimeFormat.format(date))
        assertEquals(tMood.date, dateFormat.format(date))
        assertEquals(tMood.time, timeFormat.format(date))

    }

    @Test
    fun `can get two items from moodList, one of each RowEntryType`() {

        mockAdapter.updateList(arrayListOf(MoodEntryModel()))

        val anyMood = mockAdapter.getMoodList()[0]
        // Filter type because a FilterRow would have been thrown in by now
        assertEquals(RowEntryModel.FILTER_ENTRY_TYPE, mockAdapter.getItemViewType(0))
        assertEquals(RowEntryModel.MOOD_ENTRY_TYPE, mockAdapter.getItemViewType(1))
    }

    @Test
    fun `can bind viewHolder to MoodEntryHolder then FilterViewHolder`() {
        val viewHolder = mockk<MoodEntryViewHolder> {
            every { itemViewType } returns RowEntryModel.MOOD_ENTRY_TYPE
        }
        var position = 1
        every { mockAdapter.invoke("bindToMoodViewHolder").withArguments(listOf(viewHolder, position)) } returns Unit

        try {
            mockAdapter.onBindViewHolder(viewHolder, position)
            assert(true)
        } catch (e: Exception) {
            // Not expected!
        }

        val viewHolderTest2 = mockk<FilterViewHolder> {
            every { itemViewType } returns RowEntryModel.FILTER_ENTRY_TYPE
            every { tvFilterTitle.text = any<CharSequence>() } returns Unit
        }
        position = 0
        try {
            mockAdapter.onBindViewHolder(viewHolderTest2, position)
        } catch (e: Exception) {

        }

        verify { mockAdapter.onBindViewHolder(viewHolder, 1) }
        verify { mockAdapter.onBindViewHolder(viewHolderTest2, position) }
    }

    @Test
    fun getItemCount() {
    }

    @Test
    fun onItemMove() {
    }

    @Test
    fun onItemDismiss() {
    }

    @Test
    fun getOnSwiped() {
    }

    @Test
    fun getOnListUpdated() {
    }

    @Test
    fun getOnMoodValueClicked() {
    }

    @Test
    fun getOnStartActivitiesActivity() {
    }

    @Test
    fun getStartFeelingsActivity() {
    }

    @Test
    fun getSettings() {
    }

    @Test
    fun setSettings() {
    }

    private fun startActivityFeelings(moodEntry: MoodEntryModel) {

    }

    private fun startActivityActivities(moodEntry: MoodEntryModel) {

    }

    private fun updateDatabaseEntry(moodEntries: ArrayList<MoodEntryModel>) {

    }

    private fun setupMoodPicker(mood: MoodEntryModel) {

    }

    private fun writeEntrytoFile(moodEntries: ArrayList<MoodEntryModel>) {

    }

    private fun onItemDismissed(moodEntry: MoodEntryModel, moodList: ArrayList<MoodEntryModel>) {

    }
}