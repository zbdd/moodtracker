package com.kalzakath.zoodle

fun toFaces(convertValue: Int): String {
    val num = when (convertValue) {
        1 -> "Terrible"
        2 -> "Unhappy"
        3 -> "Average"
        4 -> "Happy"
        5 -> "Ecstatic"
        else -> convertValue.toString()
    }
    return num
}

fun toNumber(convertValue: String): Int {
    val num = when (convertValue) {
        "Ecstatic" -> 5
        "Happy" -> 4
        "Average" -> 3
        "Unhappy" -> 2
        "Terrible" -> 1
        else -> -1
    }
    return num
}