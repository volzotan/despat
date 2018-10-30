package de.volzo.despat.persistence;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

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

    @Query("SELECT * FROM position WHERE (latitude IS NULL OR longitude IS NULL) AND capture_id IN (SELECT id FROM capture WHERE session_id = :sessionId)")
    List<Position> getAllWithoutLatLonBySession(long sessionId);

    @Insert
    void insert(Position... positions);

    @Update
    void update(Position... positions);

    @Delete
    void delete(Position positions);

    @Query("DELETE FROM position")
    public void dropTable();
}