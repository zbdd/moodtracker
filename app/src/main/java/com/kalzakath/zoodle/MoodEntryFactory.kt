package com.kalzakath.zoodle

import android.content.Context
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.random.Random

class MoodEntryFactory: RowEntryFactory() {
    fun create(): MoodEntryModel {
        return MoodEntryModel()
    }

    fun createDebug(
        context: Context,
        dateTime: LocalDateTime = LocalDateTime.now(),
        settings: Settings
    ): MoodEntryModel {
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
        val date = dateTime.format(dateTimeFormatter)

        dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val time = dateTime.format(dateTimeFormatter)

        return when (Settings.moodMode) {
            Mood.MOOD_MODE_NUMBERS -> MoodEntryModel(
                date,
                time,
                Mood("3"),
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

/*
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

        return when (settings.moodMode) {
            Mood.MOOD_MODE_NUMBERS -> MoodEntryModel(
                date,
                time,
                Mood("3"),
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
 */