package com.kalzakath.zoodle

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import com.kalzakath.zoodle.model.MoodEntryModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class MoodEntryPicker(context: Context, val onCreateMoodEntry: (MoodEntryModel) -> Unit) : Dialog(context) {

    private var dialog = Dialog(context)

    private fun initButtons() {
        val bmpCancel = dialog.findViewById<Button>(R.id.bmpCancel)
        val bmpCreate = dialog.findViewById<Button>(R.id.bmpCreate)

        bmpCancel.setOnClickListener {
            dialog.dismiss()
        }

        bmpCreate.setOnClickListener {
            onCreateMoodEntry(createNewMoodEntry())
            dialog.dismiss()
        }
    }

    private fun createNewMoodEntry(): MoodEntryModel {
        val date: TextView = dialog.findViewById(R.id.tvmpDateValue)
        val time: TextView = dialog.findViewById(R.id.tvmpTimeValue)
        val mood: TextView = dialog.findViewById(R.id.tvmpMoodValue)

        return MoodEntryModel(
            date.text.toString(),
            time.text.toString(),
            mood.text.toString().toInt()
        )
    }

    private fun loadDefaults() {
        val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val date: TextView = dialog.findViewById(R.id.tvmpDateValue)
        val timeFormat = DateTimeFormatter.ofPattern("HH:mm")
        val time: TextView = dialog.findViewById(R.id.tvmpTimeValue)

        date.text = dateFormat.format(LocalDate.now())
        time.text = timeFormat.format(LocalTime.now())
    }

    fun showPopup() {
        val view = LayoutInflater.from(context).inflate(R.layout.mood_picker, null, false)
        dialog.setContentView(view)
        dialog.create()

        loadDefaults()
        initButtons()
        dialog.show()
    }
}