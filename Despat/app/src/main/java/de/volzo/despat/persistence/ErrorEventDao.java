package de.volzo.despat.persistence;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

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