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
public interface StatusDao {

    @Query("SELECT * FROM status")
    List<Status> getAll();

//    @Query("SELECT * FROM status SORT BY ... ASC LIMIT 1")
//    Status getLast();
//
//    @Query("SELECT * FROM user WHERE uid IN (:userIds)")
//    List<Status> loadAllByIds(int[] userIds);
//
//    @Query("SELECT * FROM user WHERE first_name LIKE :first AND "
//            + "last_name LIKE :last LIMIT 1")
//    Status findByName(String first, String last);

    @Insert
    void insertAll(Status... users);

    @Delete
    void delete(Status user);
}