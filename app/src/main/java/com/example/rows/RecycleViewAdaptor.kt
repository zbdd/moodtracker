package com.example.rows

import android.app.DatePickerDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class RecyclerViewAdaptor(val onSwiped: (MoodEntryModel) -> Unit, val onListUpdated: (ArrayList<MoodEntryModel>) -> Unit):
    RecyclerView.Adapter<RecyclerViewAdaptor.ViewHolder>(), SwipeHelperCallback.ItemTouchHelperAdaptor {

    private var moodList: ArrayList<MoodEntryModel> = ArrayList()
    private var sortBy = "date"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.mood_entry_layout, parent, false);

        return ViewHolder(view)
    }

    fun updateList(data: ArrayList<MoodEntryModel>) {
        for(entry in data) {
            if (!moodList.contains(entry)) moodList.add(entry)
        }
        data.clear()
        sortList()
        if (moodList.size > 0) onListUpdated(moodList)
        notifyDataSetChanged()
    }

    private fun sortList() {
        val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        val sorted = when (sortBy) {
            "date" -> moodList.sortedByDescending { moodEntry -> LocalDate.parse(moodEntry.date, dateTimeFormatter)}
            "mood" -> moodList.sortedByDescending { moodEntry -> moodEntry.mood}
            "activity" -> moodList.sortedByDescending { moodEntry -> moodEntry.activity}
            else -> moodList
        }

        moodList.clear()
        moodList.addAll(sorted)
    }

    private fun updateDateText(calendar: Calendar, dateText: TextView) {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        dateText.text = date.format(calendar.time)
        sortList()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val moodViewHolder = moodList[position]
        holder.dateText.text = moodViewHolder.date
        holder.moodText.setText(moodViewHolder.mood)
        holder.activityText.setText(moodViewHolder.activity)

        val calendar: Calendar = Calendar.getInstance(TimeZone.getDefault())

        val calendarButton: ImageButton = holder.itemView.findViewById(R.id.calendarButton)

        val dateSetListener =
            DatePickerDialog.OnDateSetListener { view, year, month, day ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, day)
                updateDateText(calendar, holder.dateText)
            }

        calendarButton.setOnClickListener(object: View.OnClickListener {
            override fun onClick(view: View) {
                DatePickerDialog(holder.itemView.context, dateSetListener,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        })
    }

    override fun getItemCount(): Int {
        return moodList.size
    }

    class ViewHolder(ItemView: View): RecyclerView.ViewHolder(ItemView) {
        val dateText: TextView = itemView.findViewById(R.id.dateText)
        val moodText: EditText = itemView.findViewById(R.id.moodText)
        val activityText: EditText = itemView.findViewById(R.id.activityText)
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        return false
    }

    override fun onItemDismiss(position: Int) {
        val moodEntry = moodList[position]
        moodList.removeAt(position)

        notifyItemRemoved(position)
        onListUpdated(moodList)
        onSwiped(moodEntry)
    }
}