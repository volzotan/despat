package de.volzo.despat.messaging;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

/**
 * Created by christophergetschmann on 24.11.17.
 */

@Dao
public interface StatusMessageDao {
    @Query("SELECT * FROM user")
    List<StatusMessage> getAll();

    @Query("SELECT * FROM user WHERE uid IN (:userIds)")
    List<StatusMessage> loadAllByIds(int[] userIds);

    @Query("SELECT * FROM user WHERE first_name LIKE :first AND "
            + "last_name LIKE :last LIMIT 1")
    StatusMessage findByName(String first, String last);

    @Insert
    void insertAll(StatusMessage... users);

    @Delete
    void delete(StatusMessage user);
}