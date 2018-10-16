package de.volzo.despat.persistence;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.Date;
import java.util.List;

@Dao
public interface StatusDao {

    @Query("SELECT * FROM status")
    List<Status> getAll();

    @Query("SELECT * FROM status WHERE id IN (:ids)")
    List<Status> getAllById(List<Integer> ids);

    @Query("SELECT * FROM status WHERE timestamp BETWEEN (:start) AND (:end)")
    List<Status> getAllBetween(Date start, Date end);

    @Query("SELECT * FROM status ORDER BY timestamp DESC LIMIT 1")
    Status getLast();

//    @Query("SELECT id, timestamp FROM status")
//    List<Status> getIdsForSyncCheck();

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
    void insert(Status... statuses);

    @Delete
    void delete(Status status);

    @Query("DELETE FROM status")
    public void dropTable();
}