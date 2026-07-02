package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Query("SELECT * FROM calculation_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<CalculationSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: CalculationSession): Long

    @Query("DELETE FROM calculation_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: Int)

    @Query("DELETE FROM calculation_sessions")
    suspend fun deleteAllSessions()
}
