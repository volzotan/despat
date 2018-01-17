package de.volzo.despat.persistence;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface SessionDao {

    @Query("SELECT * FROM session")
    List<Status> getAll();

    @Insert
    void insertAll(Session... sessions);

    @Delete
    void delete(Session session);
}