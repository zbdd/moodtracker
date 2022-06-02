package com.kalzakath.zoodle

import java.io.Serializable

class Mood (nValue: String = "5"): Serializable {
    var value: String? = nValue

    constructor(moodMap: HashMap<String, Any>) : this() {
        value = moodMap["value"].toString()
    }

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
}