package com.kalzakath.zoodle

import org.junit.jupiter.api.Assertions.assertEquals

class MoodTest {
    @org.junit.jupiter.api.Test
    fun `test converting mood values to strings`() {
        assertEquals("Ecstatic", toFaces(5))
        assertEquals("0", toFaces(0))
        assertEquals("Average", toFaces(3))
    }

    @org.junit.jupiter.api.Test
    fun `test converting mood strings to numbers`() {
        assertEquals(5, toNumber("Ecstatic"))
        assertEquals(1, toNumber("Terrible"))
        assertEquals(-1, toNumber("Failed"))
    }
}