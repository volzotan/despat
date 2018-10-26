package de.volzo.despat.persistence;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

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

    @Query("SELECT COUNT(*) FROM capture WHERE session_id = :sessionId AND processed_compressor = 1")
    int getNumberOfCompressorProcessedCaptures(long sessionId);

    @Insert
    long[] insert(Capture... captures);

    @Update
    void update(Capture... captures);

    @Delete
    void delete(Capture capture);

    @Query("DELETE FROM capture")
    public void dropTable();
}