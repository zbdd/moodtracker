package com.kalzakath.zoodle.debug

import android.content.Context
import com.kalzakath.zoodle.SecureFileHandler
import com.kalzakath.zoodle.Settings
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties

object TestSuite {
    fun setDataHandler(secureFileHandler: SecureFileHandler, context: Context): DebugDataHandler {
        return DebugDataHandler(secureFileHandler, context)
    }

    fun setDefaultSettings() {
        for (prop in Settings::class.memberProperties) {
            val defaultVal =
                Settings.Default::class.java.declaredFields.find { it.name == prop.name }
            if (defaultVal != null) {
                if (prop is KMutableProperty<*>) {
                    prop.setter.call(Settings, defaultVal.get(Settings.Default))
                }
            }
        }
    }
}