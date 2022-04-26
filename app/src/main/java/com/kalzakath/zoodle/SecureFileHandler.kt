package com.kalzakath.zoodle

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import java.io.*
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class SecureFileHandler(val context: Context) {

    private val SHARED_PREF_SECRET_KEY = "secretKey"

    private fun getSecretKey(): SecretKey {
        val mPrefs = context.getSharedPreferences("Pref", AppCompatActivity.MODE_PRIVATE)
        val key = mPrefs.getString(SHARED_PREF_SECRET_KEY, null)

        if (key == null) {
            val secretKey = generateSecretKey()
            saveSecretKey(mPrefs, secretKey)
            return secretKey
        }

        val decodeKey = Base64.getDecoder().decode(key)

        return SecretKeySpec(decodeKey, 0, decodeKey.size, "AES")
    }

    private fun generateSecretKey(): SecretKey {
        val secureRandom = SecureRandom()
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(256, secureRandom)
        return keyGen.generateKey()
    }

    private fun saveSecretKey(sharedPref: SharedPreferences, secretKey: SecretKey) {
        val encodedKey = Base64.getEncoder().encodeToString(secretKey.encoded)
        sharedPref.edit().putString(SHARED_PREF_SECRET_KEY, encodedKey).apply()
    }

    private fun encryptData(secretKey: SecretKey, fileData: ByteArray): ByteArray {
        val data = secretKey.encoded
        val sKeySpec = SecretKeySpec(data, 0, data.size, "AES/CBC/PKCS5Padding")
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, sKeySpec, IvParameterSpec(ByteArray(cipher.blockSize)))
        return cipher.doFinal(fileData)
    }

     fun write(jsonString: String, filename: String) {
        val secretKey = getSecretKey()
        val fileData = jsonString.toByteArray(Charsets.UTF_32)
        val encoded = encryptData(secretKey, fileData)

        val fos: FileOutputStream = context.openFileOutput(filename, AppCompatActivity.MODE_PRIVATE)
        val bos = BufferedOutputStream(fos)
        bos.write(encoded)
        bos.flush()
        bos.close()
    }

     fun write(data: ArrayList<*>, filename: String = "testData.json") {
        val gson = Gson()
        val jsonString: String = gson.toJson(data)
         write(jsonString, filename)
    }

     fun write(data: Settings, filename: String = "settings.json") {
        val gson = Gson()
        val jsonString: String = gson.toJson(data)
         write(jsonString, filename)
    }

    private fun readDataFromFile(filename: String): ByteArray? {
        val path = context.filesDir.absoluteFile

        val file = File("$path/$filename")
        if (file.isFile) {
            val fileContents = file.readBytes()
            val inputBuffer = BufferedInputStream(FileInputStream(file))
            inputBuffer.read(fileContents)
            inputBuffer.close()

            return fileContents
        }
        return null
    }

    private fun decrypt(secretKey: SecretKey, fileData: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(ByteArray(cipher.blockSize)))
        return cipher.doFinal(fileData)
    }

    fun read(filename: String = "testData.json"): String? {
        val data = readDataFromFile(filename)
        if (data?.isNotEmpty() == true) {
            val secretKey = getSecretKey()
            val decryptedData = decrypt(secretKey, data)
            return decryptedData.toString(Charsets.UTF_32)
        }
        return null
    }
}