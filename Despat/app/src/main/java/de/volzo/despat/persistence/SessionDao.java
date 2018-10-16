package de.volzo.despat.persistence;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public interface SessionDao {

    @Query("SELECT * FROM session WHERE id = :sessionId")
    Session getById(long sessionId);

    @Query("SELECT * FROM session")
    List<Session> getAll();

    @Query("SELECT * FROM session WHERE id IN (:ids)")
    List<Session> getAllById(List<Integer> ids);

    @Query("SELECT * FROM session ORDER BY start DESC LIMIT 1")
    Session getLast();

    @Query("SELECT COUNT(*) FROM capture WHERE session_id = :sessionId")
    int getNumberOfCaptures(long sessionId);

    @Query("SELECT COUNT(*) FROM errorEvent WHERE session_id = :sessionId")
    int getNumberOfErrors(long sessionId);

    @Insert
    long[] insert(Session... sessions);

    @Update
    void update(Session session);

    @Delete
    void delete(Session session);

    @Query("DELETE FROM session")
    void dropTable();
}