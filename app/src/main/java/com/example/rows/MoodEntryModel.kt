package com.example.rows

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class MoodEntryModel (
    val date: String? = null,
    val mood: String? = null,
    val activity: String? = null
)