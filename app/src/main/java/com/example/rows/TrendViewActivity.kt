package com.example.rows

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.rows.databinding.ActivityTrendViewBinding
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileReader
import java.nio.charset.Charset
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.EntryXComparator
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.SimpleFormatter
import kotlin.collections.ArrayList

class TrendViewActivity() : AppCompatActivity() {

    private var moodData = ArrayList<MoodEntryModel>()

    class ChartValueFormatter: ValueFormatter() {
        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            return formatter.format(Date(value.toLong()))
        }

        override fun getFormattedValue(value: Float): String {
            return value.toInt().toString()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trend_view)
        readFromLocalStore()
        setLineChartData()

        val bViewData: Button = findViewById(R.id.bViewData)
        bViewData.setOnClickListener {
            finish()
        }
    }

    private fun setLineChartData() {
        val entryList: ArrayList<Entry> = ArrayList()
        for (moods in moodData) {
            entryList.add(Entry(SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(moods.date ?: "2000-00-01").time.toFloat(),
                (moods.mood?.value?.toFloat() ?: 0.0) as Float)
            )
        }

        Collections.sort(entryList, EntryXComparator())

        val lineDataSet = LineDataSet(entryList, "Mood")
        lineDataSet.color = Color.WHITE
        lineDataSet.circleRadius = 10f
        lineDataSet.valueTextSize = 20F
        lineDataSet.lineWidth = 4f
        lineDataSet.fillColor = Color.WHITE
        lineDataSet.valueTextColor = Color.WHITE
        lineDataSet.mode = LineDataSet.Mode.HORIZONTAL_BEZIER;

        val data = LineData(lineDataSet)
        data.setValueTextColor(Color.WHITE)
        data.setValueFormatter(ChartValueFormatter())

        val chart: LineChart = findViewById(R.id.getTheGraph)

        chart.data = data
        chart.setBackgroundColor(Color.BLACK)
        chart.animateXY(2000, 2000, Easing.EaseInCubic)

        var xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.textColor = Color.WHITE
        xAxis.valueFormatter = ChartValueFormatter()
        xAxis.labelRotationAngle = 90f
        xAxis.setDrawGridLines(false)

        var yAxis = chart.axisLeft
        yAxis.textColor = Color.WHITE
        yAxis.axisMaximum = 10f
        yAxis.axisMinimum = 0f
        yAxis.setDrawGridLines(false)
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
        var json: String

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