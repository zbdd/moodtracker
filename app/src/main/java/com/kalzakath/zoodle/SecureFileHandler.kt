package com.kalzakath.zoodle

import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.*
import java.lang.reflect.Modifier
import java.util.logging.Logger

open class SecureFileHandler(securityHandler: SecurityHandler) {
    private val log = Logger.getLogger(MainActivity::class.java.name + "****************************************")
    private val _securityHandler = securityHandler

    fun write(jsonString: String, filename: String): Boolean {

             val fileData = jsonString.toByteArray(Charsets.UTF_32)
             val encoded = _securityHandler.encryptData(fileData)

             return writeEncodedToFile(encoded, filename)
    }

    open fun writeEncodedToFile(encoded: ByteArray, filename: String): Boolean {
        return try {
            val fos: FileOutputStream =
                _securityHandler.context.openFileOutput(filename, AppCompatActivity.MODE_PRIVATE)
            val bos = BufferedOutputStream(fos)
            bos.write(encoded)
            bos.flush()
            bos.close()
            log.info("Writing to local storage: SUCCESS")
            true
        } catch (e: Exception) {
            log.info("Writing to local storage: FAIL")
            log.info(e.toString())
            false
        }
    }

     fun write(data: ArrayList<*>, filename: String = "testData.json"): Boolean {
         val gson = Gson()
         val jsonString: String = gson.toJson(data as ArrayList<MoodEntryModel>)
         return write(jsonString, filename)
    }

     fun write(data: Settings, filename: String = "settings.json"): Boolean {
        val gson = GsonBuilder().excludeFieldsWithModifiers(Modifier.TRANSIENT).create()
        val jsonString: String = gson.toJson(data)
         return write(jsonString, filename)
    }

    private fun readDataFromFile(filename: String): ByteArray? {
        val path = _securityHandler.context.filesDir.absoluteFile
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

    fun read(filename: String = "testData.json"): String {
        val logString = "Reading from local file: $filename: "
        val data = readDataFromFile(filename)
        if (data?.isNotEmpty() == true) {
            val decryptedData = _securityHandler.decrypt(data)
            log.info(logString + "SUCCESS")
            return decryptedData.toString(Charsets.UTF_32)
        } else log.info(logString + "FAILURE")
        return ""
    }
}