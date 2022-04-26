package com.kalzakath.zoodle

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.EntryXComparator
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class TrendViewActivity : AppCompatActivity() {

    private var moodData = ArrayList<MoodEntryModel>()
    private var filter = "default"
    private var settings: Settings? = null

    class MyFormat(val context: Context, val settings: Settings): ValueFormatter() {
        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
           when (settings.mood_numerals) {
               "true" -> return value.toString()
           }
            return ""
        }
    }

    class ChartValueFormatter(private val filter: String): ValueFormatter() {
        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            val formatter = when (filter) {
                "year" -> SimpleDateFormat("yyyy-MM", Locale.ENGLISH)
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

        val secureFileHandler = SecureFileHandler(applicationContext)

        val jsonString = secureFileHandler.read()
        if (jsonString != null) moodData = getMoodListFromJSON(jsonString)

        settings = intent.getParcelableExtra("Settings")
        if (moodData.isNotEmpty()) setLineChartData()

        val bViewData: Button = findViewById(R.id.bViewData)
        val bReset: Button = findViewById(R.id.bTrendReset)
        val bMonth: Button = findViewById(R.id.bTrendMonth)
        val bYear: Button = findViewById(R.id.bTrendYear)

        bReset.setOnClickListener {
            filter = "default"
            setLineChartData()
        }

        bMonth.setOnClickListener {
            filter = "month"
            setLineChartData()
        }

        bYear.setOnClickListener {
            filter = "year"
            setLineChartData()
        }

        bViewData.setOnClickListener {
            finish()
        }
    }

    private fun setLineChartData() {
        val entryList: ArrayList<Entry> = ArrayList()
        val timeArray = mutableMapOf<String, ArrayList<Int>>()
        var subStringLength = 4
        var dateFormat = SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.ENGLISH
        )
        var format = "yyyy"

        for (moods in moodData) {
            var moodNumber = moods.mood?.value
            var timePeriod = ""

            if (settings?.mood_numerals == "true") moodNumber = moods.mood?.toNumber() ?: "3"

            if (filter == "month") {
                format = "yyyy-MM"
                subStringLength = 7
            } else if (filter == "year") format = "yyyy"

            if (filter != "default") {
                dateFormat = SimpleDateFormat(
                    format,
                    Locale.ENGLISH
                )

                if (timePeriod == "" || timePeriod != moods.date?.substring(0, subStringLength)) {
                    timePeriod =
                        moods.date?.substring(0, subStringLength) as String
                    if (timeArray[timePeriod] == null) timeArray[timePeriod] = ArrayList()
                    val value = moods.mood?.toNumber()?.toInt() as Int
                    timeArray[timePeriod]?.add(value)
                }
            } else {
                entryList.add(
                    Entry(
                        dateFormat.parse(moods.date.toString())?.time?.toFloat() ?: 0.0.toFloat(),
                        (moodNumber?.toFloat() ?: 0.0) as Float
                    )
                )
            }
        }

            if (filter != "default") {
                for ((key) in timeArray) {
                    var average = 0
                    for (data in timeArray[key]!!) {
                        average += data
                    }
                    average /= timeArray[key]!!.size
                    entryList.add(
                    Entry(
                        dateFormat.parse(key)?.time?.toFloat() ?: 0.0.toFloat(),
                        (average.toFloat())
                    ))
                }
            }

        Collections.sort(entryList, EntryXComparator())

        val lineDataSet = LineDataSet(entryList, "Mood")
        lineDataSet.color = Color.WHITE
        lineDataSet.circleRadius = 1f
        lineDataSet.valueTextSize = 10F
        lineDataSet.lineWidth = 2f
        lineDataSet.fillColor = Color.WHITE
        lineDataSet.valueTextColor = Color.WHITE
        lineDataSet.mode = LineDataSet.Mode.HORIZONTAL_BEZIER

        val data = LineData(lineDataSet)
        data.setValueTextColor(Color.WHITE)
        data.setValueFormatter(ChartValueFormatter(filter))

        val chart: LineChart = findViewById(R.id.getTheGraph)

        chart.data = data
        chart.setBackgroundColor(Color.BLACK)
        chart.animateXY(2000, 2000, Easing.EaseInCubic)

        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.textColor = Color.WHITE
        xAxis.valueFormatter = ChartValueFormatter(filter)
        xAxis.labelRotationAngle = 90f
        xAxis.setDrawGridLines(false)


        val yAxis = chart.axisLeft
        yAxis.textColor = Color.WHITE
        yAxis.axisMaximum = settings!!.mood_max?.toFloat() ?: 5f
        yAxis.axisMinimum = 0f
        yAxis.granularity = 1f
        if (moodData.isNotEmpty()) if (settings!!.mood_numerals == "false") yAxis.valueFormatter = MyFormat(applicationContext,
            settings!!
        )
        yAxis.setDrawGridLines(false)
    }

    private fun getMoodListFromJSON(jsonString: String): ArrayList<MoodEntryModel> {
        val moodList = ArrayList<MoodEntryModel>()

        if (jsonString.isNotEmpty()) {
            val gson = GsonBuilder().create()
            val type = object: TypeToken<Array<MoodEntryModel>>() {}.type
            val moodEntries = gson.fromJson<Array<MoodEntryModel>>(jsonString, type)

            for(x in moodEntries.indices) {
                moodList.add(moodEntries[x])
            }
        }

        return moodList
    }
}