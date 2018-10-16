package de.volzo.despat.persistence;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ErrorEventDao {

    @Query("SELECT * FROM errorEvent")
    List<ErrorEvent> getAll();

    @Query("SELECT * FROM errorEvent WHERE id IN (:ids)")
    List<ErrorEvent> getAllById(List<Integer> ids);

    @Query("SELECT * FROM errorEvent WHERE session_id = :sessionId")
    List<ErrorEvent> getAllBySession(long sessionId);

    @Query("SELECT * FROM errorEvent WHERE session_id = :sessionId ORDER BY timestamp DESC LIMIT 1")
    ErrorEvent getLastFromSession(long sessionId);

    @Insert
    void insert(ErrorEvent... errorEvents);

    @Delete
    void delete(ErrorEvent errorEvent);

    @Query("DELETE FROM errorEvent")
    public void dropTable();
}