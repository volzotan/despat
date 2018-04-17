package de.volzo.despat.persistence;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface ErrorDao {

    @Query("SELECT * FROM error")
    List<Error> getAll();

    @Query("SELECT * FROM error WHERE id IN (:ids)")
    List<Error> getAllById(List<Integer> ids);

    @Query("SELECT * FROM error WHERE session_id = :sessionId")
    List<Error> getAllBySession(long sessionId);

    @Query("SELECT * FROM error WHERE session_id = :sessionId ORDER BY occurence_time DESC LIMIT 1")
    Error getLastFromSession(long sessionId);

    @Insert
    void insert(Error... errors);

    @Delete
    void delete(Error error);

    @Query("DELETE FROM error")
    public void dropTable();
}