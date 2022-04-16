package com.example.rows

import android.app.Application
import android.content.Context
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
    private var filter = "default"

    class MyFormat(val context: Context): ValueFormatter() {
        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            val mood = Mood(value.toInt().toString())
            mood.toFaces()

            if (mood.toEmoji() == null) return value.toString()

            return context.resources.getString(mood.toEmoji() as Int)
        }
    }

    class ChartValueFormatter(val filter: String): ValueFormatter() {
        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            val formatter = when (filter) {
                "month" -> SimpleDateFormat("yyyy-MM", Locale.ENGLISH)
                else -> SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            }
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
        val bReset: Button = findViewById(R.id.bTrendReset)
        val bMonth: Button = findViewById(R.id.bTrendMonth)

        bReset.setOnClickListener {
            filter = "default"
            setLineChartData()
        }

        bMonth.setOnClickListener {
            filter = "month"
            setLineChartData()
        }

        bViewData.setOnClickListener {
            finish()
        }
    }

    private fun setLineChartData() {
        val entryList: ArrayList<Entry> = ArrayList()
        var monthArray = mutableMapOf<String, ArrayList<Int>>()

        for (moods in moodData) {
            var moodNumber = moods.mood?.value
            var month = ""

            if (moods.mood?.moodMode == Mood.MOOD_MODE_FACES) moodNumber = moods.mood.toNumber()

            if (filter == "month") {
                if (month == "" || month != moods.date?.substring(0, 7)) {
                    month =
                        moods.date?.substring(0, 7) as String
                    if (monthArray[month] == null) monthArray[month] = ArrayList()
                    val value = moods.mood?.toNumber()?.toInt() as Int
                    monthArray[month]?.add(value)
                }
            } else {
                entryList.add(
                    Entry(
                        SimpleDateFormat(
                            "yyyy-MM-dd",
                            Locale.ENGLISH
                        ).parse(moods.date).time.toFloat(),
                        (moodNumber?.toFloat() ?: 0.0) as Float
                    )
                )
            }
        }

            if (filter == "month") {
                for ((key, value) in monthArray) {
                    var average = 0
                    for (data in monthArray[key]!!) {
                        average += data
                    }
                    average /= monthArray[key]!!.size
                    entryList.add(
                    Entry(
                        SimpleDateFormat(
                            "yyyy-MM",
                            Locale.ENGLISH
                        ).parse(key).time.toFloat(),
                        (average?.toFloat() ?: 0.0) as Float
                    ))
                }
            }

        Collections.sort(entryList, EntryXComparator())

        val lineDataSet = LineDataSet(entryList, "Mood")
        lineDataSet.color = Color.WHITE
        lineDataSet.circleRadius = 0f
        lineDataSet.valueTextSize = 10F
        lineDataSet.lineWidth = 2f
        lineDataSet.fillColor = Color.WHITE
        lineDataSet.valueTextColor = Color.WHITE
        lineDataSet.mode = LineDataSet.Mode.HORIZONTAL_BEZIER;

        val data = LineData(lineDataSet)
        data.setValueTextColor(Color.WHITE)
        data.setValueFormatter(ChartValueFormatter(filter))

        val chart: LineChart = findViewById(R.id.getTheGraph)

        chart.data = data
        chart.setBackgroundColor(Color.BLACK)
        chart.animateXY(2000, 2000, Easing.EaseInCubic)

        var xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.textColor = Color.WHITE
        xAxis.valueFormatter = ChartValueFormatter(filter)
        xAxis.labelRotationAngle = 90f
        xAxis.setDrawGridLines(false)


        var yAxis = chart.axisLeft
        yAxis.textColor = Color.WHITE
        yAxis.axisMaximum = 5f
        yAxis.axisMinimum = 0f
        yAxis.granularity = 1f
        if (moodData.isNotEmpty()) if (moodData[0].mood?.moodMode == Mood.MOOD_MODE_FACES) yAxis.valueFormatter = MyFormat(applicationContext)
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