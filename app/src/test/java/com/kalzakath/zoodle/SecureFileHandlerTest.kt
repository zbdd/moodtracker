package com.kalzakath.zoodle

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SecureFileHandlerTest {

    class FakeSecureFileHandler(securityHandler: SecurityHandler) : SecureFileHandler(
        securityHandler
    ) {
        override fun writeEncodedToFile(encoded: ByteArray, filename: String): Boolean {
            return true
        }
    }

    lateinit var fakeSecureFileHandler: FakeSecureFileHandler
    lateinit var securityHandler: SecurityHandler

    @BeforeEach
    internal fun setUp() {
        securityHandler = mockk() {
            every { encryptData(any()) } returns ByteArray(1) {1}
        }

        fakeSecureFileHandler = FakeSecureFileHandler(securityHandler)
    }

    @Test
    fun `can write a json, encrypt and save to file`() {
        val jsonString = "test data"
        val fileName = "myFile.json"

        assert(fakeSecureFileHandler.write(jsonString, fileName))
    }

    @Test
    fun `can write data as arraylist to file`() {
        val testList = arrayListOf(MoodEntryModel())
        val testFile = "test.json"

        assert(fakeSecureFileHandler.write(testList))
        assert(fakeSecureFileHandler.write(testList, testFile))
    }

    @Test
    fun `can write data as arraylist RowEntryModel to file`() {
        val testList = arrayListOf(MoodEntryModel())
        val testFile = "cheese.json"

        assert(fakeSecureFileHandler.write(testList))
        assert(fakeSecureFileHandler.write(testList, testFile))
    }

    @Test
    fun `can write settings data to file`() {
        val testFile = "settingFile.json"

        assert(fakeSecureFileHandler.write(Settings))
        assert(fakeSecureFileHandler.write(Settings, testFile))
    }
}