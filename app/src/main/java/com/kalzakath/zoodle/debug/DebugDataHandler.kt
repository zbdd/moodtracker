package com.kalzakath.zoodle.debug

import android.content.Context
import com.kalzakath.zoodle.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.random.Random

class DebugDataHandler (secureFileHandler: SecureFileHandler,
                        private var context: Context
): DataHandler(secureFileHandler, context) {

    override fun read(): ArrayList<MoodEntryModel> {
        val debugArrayList = arrayListOf<MoodEntryModel>()
        for (i in 1..121) {
            debugArrayList.add(createNewMoodEntry())
        }
        val dateTimeNow = LocalDateTime.now()
        debugArrayList.add(MoodEntryModel(DateTimeFormatter.ofPattern("yyyy-MM-dd").format(dateTimeNow)))
        return debugArrayList
    }

    private fun createNewMoodEntry(): MoodEntryModel {
        val random = Random
        val randomYear = random.nextInt(2010, 2021).toString()
        var randMonth = random.nextInt(1, 12).toString()
        if (randMonth.toInt() < 10) randMonth = "0$randMonth"
        var randDay = random.nextInt(1, 28).toString()
        if (randDay.toInt() < 10) randDay = "0$randDay"
        val randMood = random.nextInt(1, 5).toString()

        val availFeelings = context.resources.getStringArray(R.array.available_feelings)

        val choices: MutableList<String> = ArrayList()
        choices.add("Programming")
        choices.add("Gaming")
        choices.add("Reading")
        choices.add("Going out")
        choices.add("School")
        choices.add("Rugby")
        choices.add("DnD")
        choices.add("Hanging out")

        val list: MutableList<String> = ArrayList()
        for (i in 1..random.nextInt(4)) {
            list.add(choices[random.nextInt(0, choices.size - 1)])
        }

        val feelings: MutableList<String> = ArrayList()
        for (i in 1..random.nextInt(4)) {
            feelings.add(availFeelings[random.nextInt(0, availFeelings.size - 1)])
        }

        var mood = MoodEntryModel()
        when (Settings.moodMode) {
            Mood.MOOD_MODE_NUMBERS -> mood = MoodEntryModel(
                "$randomYear-$randMonth-$randDay",
                "12:34",
                Mood(randMood),
                feelings,
                list,
                UUID.randomUUID().toString()
            )
            Mood.MOOD_MODE_FACES -> {
                mood = MoodEntryModel(
                    "$randomYear-$randMonth-$randDay",
                    "12:34",
                    Mood("3", Mood.MOOD_MODE_FACES),
                    feelings,
                    list,
                    UUID.randomUUID().toString()
                )
            }
        }
        return mood
    }
}