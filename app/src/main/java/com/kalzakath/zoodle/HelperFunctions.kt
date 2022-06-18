package com.kalzakath.zoodle

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

fun convertStringToDateTime(date: String, indexTo: Int = 19): LocalDateTime {
    val subDate = date.substring(0,indexTo)
    val subFormat = "yyyy-MM-dd'T'HH:mm:ss".substring(0, indexTo+2)
    val dateFormat = DateTimeFormatter.ofPattern(subFormat, Locale.ENGLISH)
    return LocalDateTime.parse(subDate, dateFormat)
}

fun convertStringToDate(date: String): LocalDate {
    val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)
    return LocalDate.parse(date, dateFormat)
}

fun convertStringToTime(time: String): LocalTime {
    val timeFormat = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH)
    return LocalTime.parse(time, timeFormat)
}
