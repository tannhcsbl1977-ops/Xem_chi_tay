package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "palm_readings")
data class PalmReading(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val handType: String, // Earth, Water, Air, Custom
    val linesDescription: String,
    val mountsDescription: String,
    val specialSigns: String,
    val analysisResult: String,
    val imageUriString: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isAiAnalysis: Boolean = false,
    val creator: String = "Trần Minh Tân"
)
