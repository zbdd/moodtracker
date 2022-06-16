package com.kalzakath.zoodle

import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties

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

    fun setDefaultSettings() {
        for (prop in Settings::class.memberProperties) {
            val defaultVal =
                Default::class.java.declaredFields.find { it.name == prop.name }
            if (defaultVal != null) {
                if (prop is KMutableProperty<*>) {
                    prop.setter.call(Settings, defaultVal.get(Default))
                }
            }
        }
    }
}