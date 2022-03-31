package com.example.rows

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class RecyclerViewAdaptor(val onSwiped: (MoodEntryModel, ArrayList<MoodEntryModel>) -> Unit, val onListUpdated: (ArrayList<MoodEntryModel>) -> Unit, val onMoodValueClicked: (MoodEntryModel) -> Unit):
    RecyclerView.Adapter<RecyclerViewAdaptor.ViewHolder>(), SwipeHelperCallback.ItemTouchHelperAdaptor {

    private var moodList: ArrayList<MoodEntryModel> = ArrayList()
    private var sortBy = "date"
    private lateinit var view: View

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        view = LayoutInflater.from(parent.context).inflate(R.layout.mood_entry_layout, parent, false);
        return ViewHolder(view)
    }

    fun updateList(data: ArrayList<MoodEntryModel> = ArrayList(0)) {
        var removeList: MutableList<MoodEntryModel> = ArrayList()

        if (data.isNotEmpty()) {
            for (entry in data) {
                val position = moodList.indexOfFirst { it.key == entry.key }
                if (position != -1) {
                    if (moodList[position].compare(entry)) {
                        removeList.add(moodList[position])
                        moodList[position] = entry
                        notifyItemChanged(position)
                    }
                } else {
                    for (x in moodList.indices) {
                        if (moodList[x].compare(entry)) {
                            moodList[x] = entry
                            notifyItemChanged(x)
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
        onListUpdated(moodList)
    }

    fun updateMoodEntry(mood: MoodEntryModel) {
        val position = moodList.indexOfFirst { it.key == mood.key }
        if (position != -1) {
            moodList[position] = mood
            notifyItemChanged(position)
            sortList()
            onListUpdated(moodList)
        }
    }

    private fun sortList() {
        val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)
        val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH)
        val comparator = compareBy({ mood: MoodEntryModel -> LocalDate.parse(mood.date, dateFormatter) }, { mood: MoodEntryModel -> LocalTime.parse(mood.time, timeFormatter) })

        val sorted = when (sortBy) {
            "date" -> { moodList.sortedWith(comparator) }
            "mood" -> moodList.sortedByDescending { moodEntry -> moodEntry.mood }
            "activity" -> moodList.sortedByDescending { moodEntry -> moodEntry.activity}
            else -> moodList
        }

        moodList.clear()
        moodList.addAll(sorted)

        for (x in moodList.indices) {
            if (sorted.contains(moodList[x])) {
                if (sorted.indexOf(moodList[x]) != x) {
                    notifyItemChanged(x)
                }
            }
        }
    }

    private fun updateDateText(calendar: Calendar, holder: ViewHolder) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        holder.dateText.text = dateFormat.format(calendar.time)

        val timeFormat = SimpleDateFormat("HH:mm", Locale.ENGLISH)
        holder.timeText.text = timeFormat.format(calendar.time)

        updateList()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val moodViewHolder = moodList[position]
        holder.dateText.text = moodViewHolder.date
        holder.timeText.text = moodViewHolder.time
        holder.moodText.text = moodViewHolder.mood
        holder.activityText.setText(moodViewHolder.activity)

        val calendar: Calendar = Calendar.getInstance(TimeZone.getDefault())

        holder.moodText.setOnClickListener {
            onMoodValueClicked(moodList[position])
        }

        val timeSetListener = TimePickerDialog.OnTimeSetListener {_, hour, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
            updateDateText(calendar, holder)
        }

        val dateSetListener =
            DatePickerDialog.OnDateSetListener { _, year, month, day ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, day)

                TimePickerDialog(
                    holder.itemView.context,
                    timeSetListener,
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()

            }

        holder.dateText.setOnClickListener {
            DatePickerDialog(
                holder.itemView.context, dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        holder.timeText.setOnClickListener {
            DatePickerDialog(
                holder.itemView.context, dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    override fun getItemCount(): Int {
        return moodList.size
    }

    class ViewHolder(ItemView: View): RecyclerView.ViewHolder(ItemView) {
        val dateText: TextView = itemView.findViewById(R.id.tvMoodDate)
        val timeText: TextView = itemView.findViewById(R.id.tvMoodTime)
        val moodText: TextView = itemView.findViewById(R.id.tvMoodRating)
        val activityText: EditText = itemView.findViewById(R.id.etActivityText)
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        return false
    }

    override fun onItemDismiss(position: Int) {
        val moodEntry = moodList[position]
        moodList.removeAt(position)

        notifyItemRemoved(position)
        onSwiped(moodEntry, moodList)
    }
}