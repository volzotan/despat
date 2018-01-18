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

    @Insert
    void insertAll(Capture... captures);

    @Delete
    void delete(Capture capture);
}