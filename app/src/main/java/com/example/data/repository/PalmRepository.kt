package com.example.data.repository

import com.example.data.database.PalmReading
import com.example.data.database.PalmReadingDao
import kotlinx.coroutines.flow.Flow

class PalmRepository(private val palmReadingDao: PalmReadingDao) {
    val allReadings: Flow<List<PalmReading>> = palmReadingDao.getAllReadings()

    suspend fun getReadingById(id: Int): PalmReading? {
        return palmReadingDao.getReadingById(id)
    }

    suspend fun insertReading(reading: PalmReading): Long {
        return palmReadingDao.insertReading(reading)
    }

    suspend fun deleteReading(reading: PalmReading) {
        palmReadingDao.deleteReading(reading)
    }

    suspend fun deleteReadingById(id: Int) {
        palmReadingDao.deleteReadingById(id)
    }
}
