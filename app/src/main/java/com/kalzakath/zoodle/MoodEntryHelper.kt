package com.kalzakath.zoodle

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

object MoodEntryHelper {
    private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH)

    fun convertStringToDateTime(date: String): LocalDateTime {
        val subDate = date.substring(0,19)
        return LocalDateTime.parse(subDate, dateFormat)
    }
}