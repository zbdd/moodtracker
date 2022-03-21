package com.example.rows

import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerViewMain)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val data = ArrayList<MoodEntryModel>()

        val entry1: MoodEntryModel = MoodEntryModel("5555-10-20", "5","Nothing")
        val entry2: MoodEntryModel = MoodEntryModel("1111-10-20", "2","test")
        val entry3: MoodEntryModel = MoodEntryModel("3333-10-20", "6","asdasd")

        data.add(entry1)
        data.add(entry2)
        data.add(entry3)

        val adaptor = RecyclerViewAdaptor(data)
        recyclerView.adapter = adaptor

    }


    fun createDefaultRow(moodText: EditText, dateText: TextView) {
        moodText.setText("5")

        val date = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formatted = date.format(formatter)
        dateText.text = formatted
    }
}