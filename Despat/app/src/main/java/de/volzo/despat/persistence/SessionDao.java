package de.volzo.despat.persistence;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface SessionDao {

    @Query("SELECT * FROM session WHERE sid = :sessionId")
    Session getById(long sessionId);

    @Query("SELECT * FROM session")
    List<Session> getAll();

    @Query("SELECT COUNT(*) FROM capture WHERE session_id = :sessionId")
    int getNumberOfCaptures(int sessionId);

    @Insert
    long[] insertAll(Session... sessions);

    @Delete
    void delete(Session session);
}