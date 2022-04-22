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
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.*
import java.util.ArrayList
import kotlin.concurrent.thread

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

        val arrayMood = arrayListOf<MoodEntryModel>()
        arrayMood.add(MoodEntryModel())
        arrayMood.add(MoodEntryModel())
        arrayMood.add(MoodEntryModel())
        arrayMood.add(MoodEntryModel())
        arrayMood.add(MoodEntryModel())

        recyclerViewAdaptor = RecyclerViewAdaptor(
            { moodEntry, moodList -> onItemDismissed(moodEntry, moodList) },
            { moodEntries -> writeEntrytoFile(moodEntries); updateDatabaseEntry(moodEntries) },
            { mood -> setupMoodPicker(mood) },
            { moodEntry -> startActivityActivities(moodEntry) },
            { moodEntry -> startActivityFeelings(moodEntry) },
            settings)

        mockAdapter = spyk(RecyclerViewAdaptor(
            { moodEntry, moodList -> onItemDismissed(moodEntry, moodList) },
            { moodEntries -> writeEntrytoFile(moodEntries); updateDatabaseEntry(moodEntries) },
            { mood -> setupMoodPicker(mood) },
            { moodEntry -> startActivityActivities(moodEntry) },
            { moodEntry -> startActivityFeelings(moodEntry) },
            settings))

        every { mockAdapter.notifyDataSetChanged() } returns Unit
        every { mockAdapter.notifyItemChanged(any<Int>()) } returns Unit
        mockkStatic(Looper::class)
        val looper = mockk<Looper> {
            every { thread } returns Thread.currentThread()
        }
        every { Looper.getMainLooper() } returns looper
        every { LayoutInflater.from(context) } returns layoutInflater

        mockAdapter.updateList(arrayMood)

    }

    @Test
    fun getViewHolder() {
        assertEquals(TestViewHolder::class.java,recyclerViewAdaptor.viewHolder::class.java)
    }

    @Test
    fun setViewHolder() {
        recyclerViewAdaptor.viewHolder = TestViewHolder(LayoutInflater.from(context).inflate(R.layout.mood_entry_layout, null, false))
        assertEquals(TestViewHolder::class.java,recyclerViewAdaptor.viewHolder::class.java)
    }

    @Test
    fun getMoodList() {
        verify(atLeast = 5) { mockAdapter.notifyDataSetChanged() }
        assertEquals(5, mockAdapter.getMoodList().size)
    }

    @Test
    fun onCreateViewHolder() {
        //Too difficult to Unit Test at this time

    }

    @Test
    fun updateListConfig() {
        var testSettings = Settings()
        testSettings.mood_max = "100"
        testSettings.mood_numerals = "test"

        recyclerViewAdaptor.updateListConfig(testSettings)
        assertEquals("100", recyclerViewAdaptor.settings.mood_max)
        assertEquals("test", recyclerViewAdaptor.settings.mood_numerals)
    }

    @Test
    fun updateList() {
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
    fun updateMoodEntry() {
    }

    @Test
    fun getItemViewType() {
    }

    @Test
    fun onBindViewHolder() {
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