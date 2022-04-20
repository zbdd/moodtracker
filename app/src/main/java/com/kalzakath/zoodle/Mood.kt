package com.kalzakath.zoodle

import java.io.Serializable

class Mood (nValue: String = "5", mode: Int = MOOD_MODE_NUMBERS): Serializable {
    var value: String? = nValue
    var moodMode: Int = mode

    fun toFaces(convertValue: String? = value): String {
        val num = when (convertValue) {
            "1" -> "Terrible"
            "2" -> "Unhappy"
            "4" -> "Happy"
            "5" -> "Ecstatic"
            "3" -> "Average"
            else -> convertValue
        }
        return num as String
    }

    fun toNumber(convertValue: String? = value): String {
        val num = when (convertValue) {
            "Ecstatic" -> "5"
            "Happy" -> "4"
            "Unhappy" -> "2"
            "Terrible" -> "1"
            "Average" -> "3"
            else -> convertValue
        }
        return num as String
    }

    companion object {
        const val MOOD_MODE_NUMBERS: Int = 0
        const val MOOD_MODE_FACES: Int = 1
    }
}