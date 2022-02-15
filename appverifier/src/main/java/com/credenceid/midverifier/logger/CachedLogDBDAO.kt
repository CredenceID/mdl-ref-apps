package com.credenceid.midverifier.logger

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface CachedLogDBDAO {

    @Insert
    suspend fun insert(log: MIDDetails?): Long

    @Query("SELECT * FROM MIDDetails")
    suspend fun get(): List<MIDDetails>

    @Query("DELETE FROM MIDDetails WHERE id = :logID")
    suspend fun delete(logID: Long)
}