package de.volzo.despat.persistence;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.Date;
import java.util.List;

@Dao
public interface DeviceLocationDao {

    @Query("SELECT * FROM deviceLocation")
    List<DeviceLocation> getAll();

    @Query("SELECT * FROM deviceLocation WHERE id IN (:ids)")
    List<DeviceLocation> getAllById(List<Integer> ids);

    @Query("SELECT * FROM deviceLocation WHERE timestamp BETWEEN (:start) AND (:end)")
    List<DeviceLocation> getAllBetween(Date start, Date end);

    @Query("SELECT * FROM deviceLocation ORDER BY timestamp DESC LIMIT 1")
    DeviceLocation getLast();

    @Insert
    void insert(DeviceLocation... deviceLocations);

    @Delete
    void delete(DeviceLocation deviceLocation);

    @Query("DELETE FROM deviceLocation")
    public void dropTable();
}