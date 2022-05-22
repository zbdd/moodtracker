package com.kalzakath.zoodle

object Settings {
    object Default {
        const val moodMode: Int = Mood.MOOD_MODE_NUMBERS
        const val moodMax: Int = 5
        const val onlineEnabled = false
        const val debugMode = true
        const val isPremiumEdition = true
    }

    var moodMode: Int = Default.moodMode
    var moodMax: Int = Default.moodMax
    var onlineEnabled = Default.onlineEnabled
    var debugMode = Default.debugMode
    var isPremiumEdition = Default.isPremiumEdition
}