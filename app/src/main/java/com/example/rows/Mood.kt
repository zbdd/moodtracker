package com.example.rows

import java.io.Serializable

class Mood (nValue: String): Serializable {
    var value: String? = nValue
        get() {
            if (moodMode == MOOD_MODE_FACES) return toFaces(field)
            return field
        }
    set (nValue) {
        if (moodMode == MOOD_MODE_FACES) {
            field = toFaces(nValue)
        }
    }

    private var faces: MutableMap<String, Int> = mutableMapOf()
    var moodMode: Int = MOOD_MODE_FACES

    init {
        faces["Ecstatic"] = R.string.mood_ecstatic
        faces["Happy"] = R.string.mood_happy
        faces["Average"] = R.string.mood_average
        faces["Unhappy"] = R.string.mood_unhappy
        faces["Terrible"] = R.string.mood_terrible
    }

    fun toFaces(mValue: String?): String? {
        val myValue = when (mValue) {
            "1" -> "Terrible"
            "2" -> "Unhappy"
            "4" -> "Happy"
            "5" -> "Ecstatic"
            else -> "Average"
        }
        return myValue
    }

    fun toEmoji(mValue: String?): Int? {
        return faces[mValue]
    }

    fun toNumber(mValue: String?): String? {
        val _value = when (mValue) {
            "Ecstatic" -> "5"
            "Happy" -> "4"
            "Poor" -> "2"
            "Terrible" -> "1"
            else -> "3"
        }
        return _value
    }

    companion object {
        const val MOOD_MODE_NUMBERS: Int = 0
        const val MOOD_MODE_FACES: Int = 1
    }
}