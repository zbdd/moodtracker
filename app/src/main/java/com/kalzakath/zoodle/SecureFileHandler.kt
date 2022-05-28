package com.kalzakath.zoodle

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.reflect.Modifier
import java.util.logging.Logger

class SecureFileHandler(context: Context): SecurityHandler(context) {
    private val Log = Logger.getLogger(MainActivity::class.java.name + "****************************************")

     fun write(jsonString: String, filename: String): Boolean {
         Log.info("Writing to local storage")
         //try {
             val fileData = jsonString.toByteArray(Charsets.UTF_32)
             val encoded = encryptData(fileData)

             val fos: FileOutputStream =
                 context.openFileOutput(filename, AppCompatActivity.MODE_PRIVATE)
             //val bos = BufferedOutputStream(fos)
             fos.write(encoded)
             fos.flush()
             fos.close()
        //} catch (e: Exception) {
          //   return false
        // }
         return true
    }

     fun write(data: ArrayList<*>, filename: String = "testData.json"): Boolean {
         val gson = Gson()
         val jsonString: String = gson.toJson(data)
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

    fun read(filename: String = "testData.json"): String? {
        Log.info("Reading from local storage")
        val data = readDataFromFile(filename)
        if (data?.isNotEmpty() == true) {
            val decryptedData = decrypt(data)
            return decryptedData.toString(Charsets.UTF_32)
        } else Log.info("No local storage to read from")
        return null
    }
}