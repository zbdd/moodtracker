package com.example.rows

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView.*
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class RecyclerViewAdaptor(val onSwiped: (MoodEntryModel, ArrayList<MoodEntryModel>) -> Unit, val onListUpdated: (ArrayList<MoodEntryModel>) -> Unit, val onMoodValueClicked: (MoodEntryModel) -> Unit,
    val onStartActivitiesActivity: (MoodEntryModel) -> Unit):
    Adapter<ViewHolder>(), SwipeHelperCallback.ItemTouchHelperAdaptor {

    private var moodList: ArrayList<RowEntryModel> = ArrayList()
    private var sortBy = "date"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            RowEntryModel.FILTER_ENTRY_TYPE -> FilterViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.filter_entry_layout, parent, false))
            else -> MoodEntryViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.mood_entry_layout, parent, false))
        }
    }

    fun updateList(data: ArrayList<MoodEntryModel> = ArrayList(0)) {
        val removeList: MutableList<MoodEntryModel> = ArrayList()

        if (data.isNotEmpty()) {
            for (entry in data) {
                if (entry.viewType == RowEntryModel.MOOD_ENTRY_TYPE) {
                    val position = moodList.indexOfFirst { it.key == entry.key }

                    if (position != -1) {
                        val moodEntry = moodList[position] as MoodEntryModel
                        if (moodEntry.compare(entry)) {
                            removeList.add(moodEntry)
                            moodList[position] = entry
                            notifyItemChanged(position)
                        }
                    } else {
                        for (x in moodList.indices) {
                            if (moodList[x].viewType == RowEntryModel.MOOD_ENTRY_TYPE) {
                                val mdEntry = moodList[x] as MoodEntryModel
                                if (mdEntry.compare(entry)) {
                                    moodList[x] = entry
                                    notifyItemChanged(x)
                                }
                            }
                        }
                    }
                }
            }
            for (entry in removeList) {
                data.remove(entry)
            }
            for (entry in data) {
                moodList.add(entry)
                notifyItemInserted(moodList.size - 1)
            }
        }

        data.clear()
        sortList()

        val listToSave = ArrayList<MoodEntryModel>()
        for(item in moodList) { if (item.javaClass == MoodEntryModel::class.java) listToSave.add(item as MoodEntryModel) }
        onListUpdated(listToSave)
    }

    fun updateMoodEntry(mood: MoodEntryModel) {
        val position = moodList.indexOfFirst { it.key == mood.key }
        if (position != -1) {
            notifyItemChanged(position)
            moodList[position] = mood
            sortList()

            val listToSave = ArrayList<MoodEntryModel>()
            for(item in moodList) { if (item.javaClass == MoodEntryModel::class.java) listToSave.add(item as MoodEntryModel) }
            onListUpdated(listToSave)
        }
    }

    private fun sortList() {
        val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)
        val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH)
        val comparator = compareBy({ mood: MoodEntryModel -> LocalDate.parse(mood.date, dateFormatter) }, { mood: MoodEntryModel -> LocalTime.parse(mood.time, timeFormatter) }).reversed()

        var moods: ArrayList<MoodEntryModel> = ArrayList()

        for(i in moodList.indices) {
            if (moodList[i].javaClass == MoodEntryModel::class.java) moods.add(moodList[i] as MoodEntryModel)
            else notifyItemRemoved(i)
        }

        val sorted = when (sortBy) {
            "date" -> { moods.sortedWith (comparator) }
            "mood" -> moods.sortedByDescending { moodEntry -> moodEntry.mood }
            else -> moods
        }

        moods.clear()
        moodList.clear()
        moodList.addAll(sorted)

        for (x in moodList.indices) {
            if (sorted.contains(moodList[x])) {
                if (sorted.indexOf(moodList[x]) != x) {
                    notifyItemChanged(x)
                }
            }
        }

        addFilterView("Today")
        addFilterView("Last Week")
        addFilterView("Last Month")
        addFilterView("Last Year")
    }

    private fun addFilterView(title: String) {
        val moods: ArrayList<MoodEntryModel> = ArrayList()
        val format = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)

        for(i in moodList.indices) {
            if (moodList[i].javaClass == MoodEntryModel::class.java) moods.add(moodList[i] as MoodEntryModel)
        }

        var maxDate: LocalDate = LocalDate.now()
        var minDate: LocalDate = LocalDate.now()

        var calendar: Calendar

        when (title) {

            "Last Year" -> {
                maxDate = LocalDate.parse("${LocalDate.now().year}-01-01", format)
                minDate = LocalDate.parse("${LocalDate.now().year - 2}-12-31", format)
            }
        }

        val pos = moods.indexOfFirst { LocalDate.parse(it.date!!, format) < maxDate && LocalDate.parse(it.date, format) > minDate }

        if (pos != -1) {
            val mood = moodList[pos] as MoodEntryModel
            println(mood.date + " parse: ${LocalDate.parse(mood.date)}")
            val filterEntry = FilterEntryModel(title)
            moodList.add(pos, filterEntry)
            notifyItemInserted(pos)
        }
    }

    private fun updateDateText(calendar: Calendar, holder: MoodEntryViewHolder, mood: MoodEntryModel) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        holder.dateText.text = dateFormat.format(calendar.time)

        val timeFormat = SimpleDateFormat("HH:mm", Locale.ENGLISH)
        holder.timeText.text = timeFormat.format(calendar.time)

        val newMood = MoodEntryModel(dateFormat.format(calendar.time), timeFormat.format(calendar.time),mood.mood,mood.activities,mood.key)

        updateMoodEntry(newMood)
    }

    override fun getItemViewType(position: Int): Int {
        return moodList[position].viewType
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        when (holder.itemViewType) {
            RowEntryModel.MOOD_ENTRY_TYPE -> {
                val mHolder = holder as MoodEntryViewHolder
                val moodViewHolder = moodList[position] as MoodEntryModel
                mHolder.dateText.text = moodViewHolder.date
                mHolder.timeText.text = moodViewHolder.time
                mHolder.moodText.text = moodViewHolder.mood
                mHolder.activityText.text = moodViewHolder.activities.toString().removeSurrounding(
                    "[",
                    "]"
                )

                when {
                    mHolder.moodText.text.toString()
                        .toInt() > 5 -> mHolder.moodText.setBackgroundResource(R.drawable.mood_rating_colour_high)
                    mHolder.moodText.text.toString()
                        .toInt() < 5 -> mHolder.moodText.setBackgroundResource(R.drawable.mood_rating_colour_low)
                    mHolder.moodText.text.toString()
                        .toInt() == 5 -> mHolder.moodText.setBackgroundResource(0)
                }

                val calendar
                        : Calendar = Calendar.getInstance(TimeZone.getDefault())

                mHolder.moodText.setOnClickListener {
                    onMoodValueClicked(moodList[position] as MoodEntryModel)
                }

                mHolder.moodText.addTextChangedListener {
                    when {
                        mHolder.moodText.text.toString()
                            .toInt() > 5 -> mHolder.moodText.setBackgroundResource(R.drawable.mood_rating_colour_high)
                        mHolder.moodText.text.toString()
                            .toInt() < 5 -> mHolder.moodText.setBackgroundResource(R.drawable.mood_rating_colour_low)
                        mHolder.moodText.text.toString()
                            .toInt() == 5 -> mHolder.moodText.setBackgroundResource(0)
                    }
                }

                val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hour)
                    calendar.set(Calendar.MINUTE, minute)
                    updateDateText(calendar, mHolder, moodViewHolder)
                }

                val dateSetListener =
                    DatePickerDialog.OnDateSetListener { _, year, month, day ->
                        calendar.set(Calendar.YEAR, year)
                        calendar.set(Calendar.MONTH, month)
                        calendar.set(Calendar.DAY_OF_MONTH, day)

                        TimePickerDialog(
                            mHolder.itemView.context,
                            timeSetListener,
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true
                        ).show()

                    }

                mHolder.dateText.setOnClickListener {
                    DatePickerDialog(
                        holder.itemView.context, dateSetListener,
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }

                mHolder.timeText.setOnClickListener {
                    DatePickerDialog(
                        holder.itemView.context, dateSetListener,
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }

                mHolder.activityText.setOnClickListener {
                    onStartActivitiesActivity(moodList[position] as MoodEntryModel)
                }
            }
            RowEntryModel.FILTER_ENTRY_TYPE -> {
                val vHolder = holder as FilterViewHolder
                val filterEntry = moodList[position] as FilterEntryModel
                vHolder.tvFilterTitle.text = filterEntry.title
            }
        }
    }

    override fun getItemCount(): Int {
        return moodList.size
    }

    class MoodEntryViewHolder(itemView: View): ViewHolder(itemView) {
        val dateText: TextView = itemView.findViewById(R.id.tvMoodDate)
        val timeText: TextView = itemView.findViewById(R.id.tvMoodTime)
        val moodText: TextView = itemView.findViewById(R.id.tvMoodRating)
        val activityText: TextView = itemView.findViewById(R.id.etActivityText)
    }

    class FilterViewHolder(itemView: View): ViewHolder(itemView) {
        val tvFilterTitle: TextView = itemView.findViewById(R.id.tvFilterTitle)
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        return false
    }

    override fun onItemDismiss(position: Int) {
        val moodEntry = moodList[position]
        moodList.removeAt(position)

        notifyItemRemoved(position)
        val listToSave = ArrayList<MoodEntryModel>()
        for(item in moodList) { if (item.javaClass == MoodEntryModel::class.java) listToSave.add(item as MoodEntryModel) }
        onSwiped(moodEntry as MoodEntryModel, listToSave)
    }
}