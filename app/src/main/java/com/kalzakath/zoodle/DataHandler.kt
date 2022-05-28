package com.kalzakath.zoodle

import android.content.Context
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.logging.Logger
import kotlin.random.Random

open class DataHandler(private var secureFileHandler: SecureFileHandler,
                       private var context: Context
) {
    private val log = Logger.getLogger(MainActivity::class.java.name + "****************************************")

     open fun read(): ArrayList<RowEntryModel> {
        val jsonArray = secureFileHandler.read()
        val moodData = ArrayList<RowEntryModel>()
        log.info("JsonArray found: ${jsonArray.toString()}")

        if (jsonArray?.isNotEmpty() == true) {
            val gson = GsonBuilder().create()
            val type = object : TypeToken<Array<MoodEntryModel>>() {}.type
            val moodEntries = gson.fromJson<Array<MoodEntryModel>>(jsonArray, type)
            if (moodEntries.isEmpty()) return moodData

            for (x in moodEntries.indices) {
                moodEntries[x].mood!!.moodMode = Settings.moodMode
                moodData.add(moodEntries[x])
            }
        }

        return moodData
    }

    open fun createNewMoodEntry(dateTimeNow: LocalDateTime = LocalDateTime.now()): MoodEntryModel {
        val random = Random(System.currentTimeMillis())

        val choices: MutableList<String> = ArrayList()
        choices.add("Programming")
        choices.add("Gaming")
        choices.add("Reading")
        choices.add("Going out")
        choices.add("School")
        choices.add("Rugby")
        choices.add("DnD")
        choices.add("Hanging out")

        val availFeelings = context.resources.getStringArray(R.array.available_feelings)

        val list: MutableList<String> = ArrayList()
        for (i in 1..random.nextInt(4)) {
            list.add(choices[random.nextInt(0, choices.size - 1)])
        }

        val feelings: MutableList<String> = ArrayList()
        for (i in 1..random.nextInt(4)) {
            feelings.add(availFeelings[random.nextInt(0, availFeelings.size - 1)])
        }

        var dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val date = dateTimeNow.format(dateTimeFormatter)

        dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val time = dateTimeNow.format(dateTimeFormatter)

        return when (Settings.moodMode) {
            Mood.MOOD_MODE_NUMBERS -> MoodEntryModel(
                date,
                time,
                Mood("3", Mood.MOOD_MODE_NUMBERS),
                feelings,
                list,
                UUID.randomUUID().toString()
            )
            else -> MoodEntryModel(
                date,
                time,
                Mood("3", Mood.MOOD_MODE_FACES),
                ArrayList(),
                ArrayList(),
                UUID.randomUUID().toString()
            )
        }
    }
}