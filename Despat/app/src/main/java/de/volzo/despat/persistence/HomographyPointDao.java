package de.volzo.despat.persistence;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface HomographyPointDao {

    @Query("SELECT * FROM homographypoint")
    List<HomographyPoint> getAll();

    @Query("SELECT * FROM homographypoint WHERE id IN (:ids)")
    List<HomographyPoint> getAllById(List<Integer> ids);

    @Query("SELECT * FROM homographypoint WHERE session_id = :sessionId")
    List<HomographyPoint> getAllBySession(long sessionId);

    @Query("SELECT COUNT(*) FROM homographypoint WHERE session_id = :sessionId")
    Integer getCountBySession(long sessionId);

    @Query("SELECT * FROM homographypoint WHERE session_id = :sessionId ORDER BY modification_time DESC LIMIT 1")
    HomographyPoint getLastFromSession(long sessionId);

    @Insert
    List<Long> insert(HomographyPoint... homographypoints);

    @Update
    void update(HomographyPoint... homographypoints);

    @Delete
    void delete(HomographyPoint homographypoint);

    @Query("DELETE FROM homographypoint")
    public void dropTable();
}