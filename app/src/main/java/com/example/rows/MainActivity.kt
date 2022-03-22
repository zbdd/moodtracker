package com.example.rows

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.*
import java.nio.charset.Charset
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerViewMain)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val data = ArrayList<MoodEntryModel>()

        val jsonArray = loadFromJSONAsset()
        val gson = GsonBuilder().create()
        val moodEntries = gson.fromJson(jsonArray, Array<MoodEntryModel>::class.java).toList()
        for(x in moodEntries.indices) {
            data.add(moodEntries[x])
        }

        val adaptor = RecyclerViewAdaptor(data)
        recyclerView.adapter = adaptor

        val addNewButton: ImageButton = findViewById(R.id.addNewButton)
        addNewButton.setOnClickListener {
            val moodEntry = createNewMoodEntry()
            data.add(moodEntry)
            writeEntrytoFile(moodEntry)

            adaptor.run {
                notifyDataSetChanged()
            }
        }
    }

    fun writeEntrytoFile(moodEntryModel: MoodEntryModel) {
        var gson = Gson()
        var jsonString: String = gson.toJson(moodEntryModel)
        val fileout: FileOutputStream = openFileOutput("testData.json", MODE_PRIVATE)
        val outwrite: OutputStreamWriter = OutputStreamWriter(fileout)
        outwrite.append(jsonString)
        outwrite.close()
        fileout.close()
    }

    fun loadFromJSONAsset(): String {
        var json = ""

        println("File to read: " + this.filesDir.absoluteFile)

        val file = File("testData.json")
        if (file.isFile) {
            val fileReader: FileReader = FileReader("testData.json")
            json = fileReader.readLines().toString()

        }
        else {
            val inputStream = assets.open("testData.json")
            val size = inputStream.available()
            val buffer = ByteArray(size)
            val charset: Charset = Charsets.UTF_8
            inputStream.read(buffer)
            inputStream.close()
            json = String(buffer, charset)
        }

        return json
    }

    fun createNewMoodEntry(): MoodEntryModel {

        val date = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formatted = date.format(formatter)

        return MoodEntryModel(formatted, "5", "New")
    }
}