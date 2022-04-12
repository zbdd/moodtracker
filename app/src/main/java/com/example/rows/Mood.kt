package com.example.rows

import java.io.Serializable

class Mood (nValue: String = "5", mode: Int = MOOD_MODE_NUMBERS): Serializable {
    var value: String? = nValue

    private var faces: MutableMap<String, Int> = mutableMapOf()
    var moodMode: Int = mode

    init {
        faces["Ecstatic"] = R.string.mood_ecstatic
        faces["Happy"] = R.string.mood_happy
        faces["Average"] = R.string.mood_average
        faces["Unhappy"] = R.string.mood_unhappy
        faces["Terrible"] = R.string.mood_terrible
    }

    fun toFaces(): String {
        value = when (value) {
            "1" -> "Terrible"
            "2" -> "Unhappy"
            "4" -> "Happy"
            "5" -> "Ecstatic"
            else -> "Average"
        }
        moodMode = MOOD_MODE_FACES
        return value as String
    }

    fun toEmoji(): Int? {
        return faces[value]
    }

    fun toNumber(): String {
        value = when (value) {
            "Ecstatic" -> "5"
            "Happy" -> "4"
            "Poor" -> "2"
            "Terrible" -> "1"
            else -> "3"
        }
        moodMode = MOOD_MODE_NUMBERS
        return value as String
    }

    companion object {
        const val MOOD_MODE_NUMBERS: Int = 0
        const val MOOD_MODE_FACES: Int = 1
    }
}