package de.volzo.despat.persistence;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.Date;
import java.util.List;

@Dao
public interface EventDao {

    @Query("SELECT * FROM event")
    List<Event> getAll();

    @Query("SELECT * FROM event WHERE id IN (:ids)")
    List<Event> getAllById(List<Integer> ids);

    @Query("SELECT * FROM event WHERE timestamp BETWEEN (:start) AND (:end)")
    List<Event> getAllBetween(Date start, Date end);

    @Insert
    void insert(Event... events);

    @Delete
    void delete(Event event);

    @Query("DELETE FROM event")
    public void dropTable();
}