package com.kalzakath.zoodle

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MoodTest {

    @Test
    fun getValue() {
        val mood = Mood()
        assertEquals("5", mood.value)

        val mood_test_2 = Mood("1")
        assertEquals("1", mood_test_2.value)
    }

    @Test
    fun setValue() {
        val mood = Mood()
        mood.value = "10"
        assertEquals("10", mood.value)
    }

    @org.junit.jupiter.api.Test
    fun toFaces() {
        val mood = Mood()
        assertEquals("Ecstatic", mood.toFaces())
        assertEquals("0", mood.toFaces("0"))
        assertEquals("Average", mood.toFaces("3"))
    }

    @org.junit.jupiter.api.Test
    fun toNumber() {
        val mood = Mood()
        assertEquals("5", mood.toNumber())
        assertEquals("1", mood.toNumber("Terrible"))
        assertEquals("Failed", mood.toNumber("Failed"))
    }
}