package de.volzo.despat.persistence;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface CaptureDao {

    @Query("SELECT * FROM capture")
    List<Capture> getAll();

    @Query("SELECT * FROM capture WHERE id IN (:ids)")
    List<Capture> getAllById(List<Integer> ids);

    @Query("SELECT * FROM capture WHERE session_id = :sessionId")
    List<Capture> getAllBySession(long sessionId);

    @Query("SELECT * FROM capture WHERE session_id = :sessionId ORDER BY recording_time DESC LIMIT 1")
    Capture getLastFromSession(long sessionId);

    @Query("SELECT * FROM capture WHERE session_id = :sessionId ORDER BY recording_time DESC LIMIT 3")
    List<Capture> getLast3FromSession(long sessionId);

    @Insert
    void insert(Capture... captures);

    @Update
    void update(Capture capture);

    @Delete
    void delete(Capture capture);

    @Query("DELETE FROM capture")
    public void dropTable();
}