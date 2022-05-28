package com.kalzakath.zoodle

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

object MoodEntryHelper {

    fun convertStringToDateTime(date: String, indexTo: Int = 19): LocalDateTime {
        val subDate = date.substring(0,indexTo)
        val subFormat = "yyyy-MM-dd'T'HH:mm:ss".substring(0, indexTo+2)
        val dateFormat = DateTimeFormatter.ofPattern(subFormat, Locale.ENGLISH)
        return LocalDateTime.parse(subDate, dateFormat)
    }
}