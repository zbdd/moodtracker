package com.kalzakath.zoodle

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

open class SecurityHandler(val context: Context) {
    private val sharedPrefSecretKey = "secretKey"

    private fun getSecretKey(): SecretKey {
        val mPrefs = context.getSharedPreferences("Pref", AppCompatActivity.MODE_PRIVATE)
        val key = mPrefs.getString(sharedPrefSecretKey, null)

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
        sharedPref.edit().putString(sharedPrefSecretKey, encodedKey).apply()
    }

    open fun encryptData(fileData: ByteArray): ByteArray {
        val secretKey = getSecretKey()
        val data = secretKey.encoded
        val sKeySpec = SecretKeySpec(data, 0, data.size, "AES/CBC/PKCS5Padding")
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, sKeySpec, IvParameterSpec(ByteArray(cipher.blockSize)))
        return cipher.doFinal(fileData)
    }

    open fun decrypt(fileData: ByteArray): ByteArray {
        val secretKey = getSecretKey()
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(ByteArray(cipher.blockSize)))
        return cipher.doFinal(fileData)
    }
}