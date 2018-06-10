package de.volzo.despat.persistence;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface PositionDao {

    @Query("SELECT * FROM position")
    List<Position> getAll();

    @Query("SELECT * FROM position WHERE id IN (:ids)")
    List<Position> getAllById(List<Integer> ids);

    @Query("SELECT * FROM position WHERE capture_id = :captureId")
    List<Position> getAllByCapture(long captureId);

    @Query("SELECT * FROM position WHERE capture_id IN (SELECT id FROM capture WHERE session_id = :sessionId)")
    List<Position> getAllBySession(long sessionId);

    @Query("SELECT COUNT(*) FROM position WHERE capture_id IN (SELECT id FROM capture WHERE session_id = :sessionId)")
    int getCountBySession(long sessionId);

    @Insert
    void insert(Position... positions);

    @Delete
    void delete(Position positions);

    @Query("DELETE FROM position")
    public void dropTable();
}