package com.kalzakath.zoodle

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.*
import java.lang.reflect.Modifier
import java.util.logging.Logger

class SecureFileHandler(context: Context): SecurityHandler(context) {
    private val log = Logger.getLogger(MainActivity::class.java.name + "****************************************")

     fun write(jsonString: String, filename: String): Boolean {

         try {
             val fileData = jsonString.toByteArray(Charsets.UTF_32)
             log.info("Data in STRING")
             println(jsonString)
             val encoded = encryptData(fileData)

             val fos: FileOutputStream =
                 context.openFileOutput(filename, AppCompatActivity.MODE_PRIVATE)
             val bos = BufferedOutputStream(fos)
             bos.write(encoded)
             log.info("Writing to local storage: SUCCESS")
             bos.flush()
             bos.close()
        } catch (e: Exception) {
             log.info("Writing to local storage: FAIL")
             return false
         }
         return true
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

    fun read(filename: String = "testData.json"): String {
        val logString = "Reading from local file: $filename: "
        val data = readDataFromFile(filename)
        if (data?.isNotEmpty() == true) {
            val decryptedData = decrypt(data)
            log.info(logString + "SUCCESS")
            return decryptedData.toString(Charsets.UTF_32)
        } else log.info(logString + "FAILURE")
        return ""
    }
}