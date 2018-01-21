package de.volzo.despat.persistence;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

/**
 * Created by christophergetschmann on 24.11.17.
 */

@Dao
public interface EventDao {

    @Query("SELECT * FROM event")
    List<Event> getAll();

    @Insert
    void insert(Event... events);

    @Delete
    void delete(Event event);
}