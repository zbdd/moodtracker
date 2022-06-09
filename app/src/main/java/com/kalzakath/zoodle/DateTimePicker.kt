package com.kalzakath.zoodle

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class DateTimePicker {

    private val calendar
            : Calendar = Calendar.getInstance(TimeZone.getDefault())

    var onUpdateListener: ((MoodEntryModel)->Unit)? = null

    private fun updateDateText(calendar: Calendar, holder: MoodViewHolder, mood: MoodEntryModel) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        holder.dateText.text = dateFormat.format(calendar.time)

        val timeFormat = SimpleDateFormat("HH:mm", Locale.ENGLISH)
        holder.timeText.text = timeFormat.format(calendar.time)

        mood.date = dateFormat.format(calendar.time)
        mood.time = timeFormat.format(calendar.time)

        onUpdateListener?.invoke(mood)
    }

    fun initButtons(viewHolder: RecyclerView.ViewHolder, row: RowEntryModel) {

        if (row.viewType != MoodEntryModel().viewType) return

        val moodEntry = row as MoodEntryModel
        val mHolder = viewHolder as MoodViewHolder

        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
            updateDateText(calendar, mHolder, moodEntry)
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
                mHolder.itemView.context, dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        mHolder.timeText.setOnClickListener {
            DatePickerDialog(
                mHolder.itemView.context, dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }
}