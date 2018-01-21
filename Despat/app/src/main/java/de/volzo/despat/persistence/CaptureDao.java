package de.volzo.despat.persistence;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface CaptureDao {

    @Query("SELECT * FROM capture")
    List<Capture> getAll();

    @Query("SELECT * FROM capture WHERE session_id = :sessionId")
    List<Capture> getAllBySession(long sessionId);

    @Query("SELECT * FROM capture WHERE session_id = :sessionId ORDER BY recording_time DESC LIMIT 1")
    Capture getLastFromSession(long sessionId);

    @Insert
    void insert(Capture... captures);

    @Delete
    void delete(Capture capture);
}