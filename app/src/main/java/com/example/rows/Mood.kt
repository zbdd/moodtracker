package com.example.rows

import java.io.Serializable

class Mood (nValue: String = "5", mode: Int = MOOD_MODE_NUMBERS): Serializable {
    var value: String? = nValue
    get() {
        return when (moodMode) {
            MOOD_MODE_FACES -> toFaces()
            else -> toNumber()
        }
    }
    set(myValue) {
        field = myValue
        storedValue = myValue as String
    }
    private lateinit var storedValue: String
    private var faces: MutableMap<String, Int> = mutableMapOf()
    var moodMode: Int = mode

    init {
        value = nValue
        faces["Ecstatic"] = R.string.mood_ecstatic
        faces["Happy"] = R.string.mood_happy
        faces["Average"] = R.string.mood_average
        faces["Unhappy"] = R.string.mood_unhappy
        faces["Terrible"] = R.string.mood_terrible
    }

    fun toFaces(): String {
        storedValue = when (storedValue) {
            "1" -> "Terrible"
            "2" -> "Unhappy"
            "4" -> "Happy"
            "5" -> "Ecstatic"
            "3" -> "Average"
            else -> storedValue
        }
        value = storedValue
        moodMode = MOOD_MODE_FACES
        return storedValue as String
    }

    fun toEmoji(): Int? {
        return faces[storedValue]
    }

    fun toNumber(): String {
        storedValue = when (storedValue) {
            "Ecstatic" -> "5"
            "Happy" -> "4"
            "Unhappy" -> "2"
            "Terrible" -> "1"
            "Average" -> "3"
            else -> storedValue
        }
        value = storedValue
        moodMode = MOOD_MODE_NUMBERS
        return storedValue as String
    }

    companion object {
        const val MOOD_MODE_NUMBERS: Int = 0
        const val MOOD_MODE_FACES: Int = 1
    }
}