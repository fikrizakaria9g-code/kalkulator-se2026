package com.example.data

import kotlinx.coroutines.flow.Flow

class SessionRepository(private val sessionDao: SessionDao) {
    val allSessions: Flow<List<CalculationSession>> = sessionDao.getAllSessions()

    suspend fun insert(session: CalculationSession): Long {
        return sessionDao.insertSession(session)
    }

    suspend fun deleteById(id: Int) {
        sessionDao.deleteSessionById(id)
    }

    suspend fun deleteAll() {
        sessionDao.deleteAllSessions()
    }
}
