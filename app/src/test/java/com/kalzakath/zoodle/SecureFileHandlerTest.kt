package com.kalzakath.zoodle

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.spyk
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.io.FileOutputStream
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SecureFileHandlerTest {

    lateinit var secureFileHandler: SecureFileHandler
    lateinit var secretKey: SecretKey

    @BeforeAll
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

        val context = mockk<Context> {
            every { getSharedPreferences("Pref", AppCompatActivity.MODE_PRIVATE) } returns sharedPref
            every { openFileOutput(any(), AppCompatActivity.MODE_PRIVATE) } returns mkFOS
        }
        secureFileHandler = spyk(SecureFileHandler(context))
    }

    private fun generateSecretKey(): SecretKey {
        val secureRandom = SecureRandom()
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(256, secureRandom)
        return keyGen.generateKey()
    }

    @Test
    fun write() {
        val jsonString = "test data"
        val fileName = "myFile.json"
        val fileData = jsonString.toByteArray(Charsets.UTF_32)

        mockkConstructor(Cipher::class)
        every { secureFileHandler.invokeNoArgs("getSecretKey") } returns secretKey
        every { secureFileHandler.invoke("encryptData").withArguments(listOf(secretKey, fileData))} returns ByteArray(1)

        assert(secureFileHandler.write(jsonString, fileName))
    }

    @Test
    fun testWrite() {
    }

    @Test
    fun testWrite1() {
    }

    @Test
    fun read() {
    }

    @Test
    fun getContext() {
    }
}