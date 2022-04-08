package com.example.rows

class Mood (var value: Int? = 5) {
    var mood: String? = "Average"
    private var faces: MutableMap<String, String> = mutableMapOf()
    var moodMode: Int = MOOD_MODE_NUMBERS

    init {
        faces["Ecstatic"] = "U+1F603"
        faces["Happy"] = "U+1F642"
        faces["Average"] = "U+1F610"
        faces["Poor"] = "U+1F614"
        faces["Terrible"] = "U+1F62D"
    }

    fun toFaces(): String? {
        mood = when (value) {
            1 -> faces["Terrible"]
            2 -> faces["Poor"]
            4 -> faces["Happy"]
            5 -> faces["Esctatic"]
            else -> faces["Average"]
        }
        return mood
    }

    fun toInt(): Int {
        value = when (mood) {
            "Ecstatic" -> 5
            "Happy" -> 4
            "Poor" -> 2
            "Terrible" -> 1
            else -> 3
        }
        return value as Int
    }

    companion object {
        const val MOOD_MODE_NUMBERS: Int = 0
        const val MOOD_MODE_FACES: Int = 1
    }
}