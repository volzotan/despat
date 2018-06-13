package de.volzo.despat.persistence;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface HomographyPointDao {

    @Query("SELECT * FROM homographypoint")
    List<HomographyPoint> getAll();

    @Query("SELECT * FROM homographypoint WHERE id IN (:ids)")
    List<HomographyPoint> getAllById(List<Integer> ids);

    @Query("SELECT * FROM homographypoint WHERE session_id = :sessionId")
    List<HomographyPoint> getAllBySession(long sessionId);

    @Query("SELECT * FROM homographypoint WHERE session_id = :sessionId ORDER BY modification_time DESC LIMIT 1")
    HomographyPoint getLastFromSession(long sessionId);

    @Insert
    void insert(HomographyPoint... homographypoints);

    @Update
    void update(HomographyPoint... homographypoints);

    @Delete
    void delete(HomographyPoint homographypoint);

    @Query("DELETE FROM homographypoint")
    public void dropTable();
}