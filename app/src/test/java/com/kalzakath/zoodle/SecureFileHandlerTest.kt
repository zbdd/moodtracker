package com.kalzakath.zoodle

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.io.File
import java.io.FileOutputStream
import javax.crypto.SecretKey

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SecureFileHandlerTest {

    lateinit var secureFileHandler: SecureFileHandler
    lateinit var secretKey: SecretKey

    @BeforeEach
    internal fun setUp() {
        secretKey = mockk {
            every { encoded } returns ByteArray(256)
        }

        val sharedPref = mockk<SharedPreferences> {
            every { getString("secretKey", null) } returns "asdasdasd"
        }
        val mkFOS = mockk<FileOutputStream> {
            every { write(any<ByteArray>()) } returns Unit
            every { flush() } returns Unit
            every { close() } returns Unit
        }

        val mkFile = mockk<File> {
            every { absoluteFile } returns mockk()
        }

        val context = mockk<Context> {
            every { getSharedPreferences("Pref", AppCompatActivity.MODE_PRIVATE) } returns sharedPref
            every { openFileOutput(any(), AppCompatActivity.MODE_PRIVATE) } returns mkFOS
            every { filesDir } returns mkFile
        }

        secureFileHandler = spyk(SecureFileHandler(context))
        every { secureFileHandler.invokeNoArgs("getSecretKey") } returns secretKey
    }

    @AfterEach
    internal fun setDown() {
        clearMocks(secureFileHandler)
    }

    @Test
    fun `can write a json, encrypt and save to file`() {
        val jsonString = "test data"
        val fileName = "myFile.json"
        val fileData = jsonString.toByteArray(Charsets.UTF_32)
        every { secureFileHandler.invokeNoArgs("getSecretKey") } returns secretKey
        every { secureFileHandler.invoke("encryptData").withArguments(listOf(secretKey, fileData))} returns ByteArray(1)

        assert(secureFileHandler.write(jsonString, fileName))
    }

    @Test
    fun `can write data as arraylist to file`() {
        val testList = arrayListOf(MoodEntryModel())
        val testFile = "testicles.json"
        val gson = Gson()
        val fileData = gson.toJson(testList).toByteArray(Charsets.UTF_32)

        every { secureFileHandler.invoke("encryptData").withArguments(listOf(secretKey, fileData))} returns ByteArray(1)

        assert(secureFileHandler.write(testList))
        assert(secureFileHandler.write(testList, testFile))
    }

    @Test
    fun `can write data as arraylist RowEntryModel to file`() {
        val testList = arrayListOf(MoodEntryModel())
        val testFile = "cheese.json"
        val gson = Gson()
        val fileData = gson.toJson(testList).toByteArray(Charsets.UTF_32)

        every { secureFileHandler.invoke("encryptData").withArguments(listOf(secretKey, fileData))} returns ByteArray(1)

        assert(secureFileHandler.write(testList))
        assert(secureFileHandler.write(testList, testFile))
    }

    @Test
    fun `can write settings data to file`() {
        val testFile = "settingFile.json"
        val gson = Gson()
        val fileData = gson.toJson(Settings).toByteArray(Charsets.UTF_32)
        every { secureFileHandler.invoke("encryptData").withArguments(listOf(secretKey, fileData))} returns ByteArray(1)

        assert(secureFileHandler.write(Settings))
        assert(secureFileHandler.write(Settings, testFile))
    }

    @Test
    fun `check read defaults`() {
        val testData = ByteArray(256)
        val testDecryptedData = ByteArray(6)
        val testRawData = testDecryptedData.toString(Charsets.UTF_32)

        assert(secureFileHandler.read() == null)
        every { secureFileHandler.invoke("readDataFromFile").withArguments(listOf("testData.json")) } returns testData
        every { secureFileHandler.invokeNoArgs("getSecretKey") } returns secretKey
        every { secureFileHandler.invoke("decrypt").withArguments(listOf(secretKey, testData ))} returns testDecryptedData
        assertEquals(testRawData, secureFileHandler.read())
    }

    @Test
    fun getContext() {
        assertEquals(mockk<Context>()::class, secureFileHandler.context::class)
    }
}