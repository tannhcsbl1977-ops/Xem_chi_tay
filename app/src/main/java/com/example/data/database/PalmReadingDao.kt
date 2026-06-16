package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PalmReadingDao {
    @Query("SELECT * FROM palm_readings ORDER BY timestamp DESC")
    fun getAllReadings(): Flow<List<PalmReading>>

    @Query("SELECT * FROM palm_readings WHERE id = :id LIMIT 1")
    suspend fun getReadingById(id: Int): PalmReading?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReading(reading: PalmReading): Long

    @Delete
    suspend fun deleteReading(reading: PalmReading)

    @Query("DELETE FROM palm_readings WHERE id = :readingId")
    suspend fun deleteReadingById(readingId: Int)
}
