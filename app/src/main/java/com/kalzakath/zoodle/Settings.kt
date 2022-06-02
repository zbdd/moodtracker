package com.kalzakath.zoodle

object Settings {
    object Default {
        const val moodMode: Int = MoodModes.NUMBERS
        const val moodMax: Int = 5
        const val onlineEnabled = false
        const val debugMode = true
        const val isPremiumEdition = true
    }

    object MoodModes {
        const val NUMBERS = 0
        const val FACES = 1
    }

    var moodMode: Int = Default.moodMode
    var moodMax: Int = Default.moodMax
    var onlineEnabled = Default.onlineEnabled
    var debugMode = Default.debugMode
    var isPremiumEdition = Default.isPremiumEdition
}