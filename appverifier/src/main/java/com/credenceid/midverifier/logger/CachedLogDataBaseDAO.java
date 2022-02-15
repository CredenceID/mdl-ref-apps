package com.credenceid.midverifier.logger;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface CachedLogDataBaseDAO {

    @Insert
    long
    insert(MIDDetails log);

    @Query("SELECT * FROM MIDDetails")
    List<MIDDetails>
    get();

    @Query("DELETE FROM MIDDetails WHERE id = :logID")
    void
    delete(long logID);
}