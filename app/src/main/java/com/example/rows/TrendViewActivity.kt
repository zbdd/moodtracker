package com.example.rows

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import com.example.rows.databinding.ActivityMainBinding
import com.example.rows.databinding.ActivityTrendViewBinding
import com.firebase.ui.auth.AuthUI
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.io.OutputStreamWriter
import java.nio.charset.Charset
import com.github.mikephil.charting.data.LineDataSet
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class TrendViewActivity() : AppCompatActivity() {

    private var moodData = ArrayList<MoodEntryModel>()
    private lateinit var binding: ActivityTrendViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trend_view)
        binding = ActivityTrendViewBinding.inflate(layoutInflater)
        readFromLocalStore()
        setLineChartData()
    }

    private fun setLineChartData() {
        var entryList: ArrayList<Entry> = ArrayList()
        var xx = 0.0f
        for (moods in moodData) {
            //SimpleDateFormat("yyyy-MM-dd").parse(moods.date).time.toFloat()
            entryList.add(Entry(xx++,
                (moods.mood?.toFloat() ?: 0.0) as Float)
            )
        }
        val lineDataSet = LineDataSet(entryList, "First")
        lineDataSet.setColor(Color.RED)
        lineDataSet.circleRadius = 10f
        //lineDataSet.setDrawFilled(true)
        lineDataSet.valueTextSize = 20F
        //lineDataSet.fillColor = resources.getColor(R.color.)
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        val data = LineData(lineDataSet)
        data.setValueTextColor(Color.BLUE)

        val chart: LineChart = findViewById(R.id.getTheGraph)

        chart.data = data
        chart.setBackgroundColor(Color.WHITE)
        chart.animateXY(2000, 2000, Easing.EaseInCubic)
       // chart.notifyDataSetChanged()
    }

    private fun readFromLocalStore() {
        val jsonArray = loadFromJSONAsset()

        if (jsonArray.isNotEmpty()) {
            val gson = GsonBuilder().create()
            val type = object: TypeToken<Array<Array<MoodEntryModel>>>() {}.type
            val moodEntries = gson.fromJson<Array<Array<MoodEntryModel>>>(jsonArray, type)
            for(x in moodEntries[0].indices) {
                moodData.add(moodEntries[0][x])
            }
        }
    }

    private fun loadFromJSONAsset(): String {
        var json = ""

        println("File to read: " + this.filesDir.absoluteFile)
        val path = this.filesDir.absoluteFile

        val file = File("$path/testData.json")
        if (file.isFile) {
            val fileReader = FileReader("$path/testData.json")
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
}