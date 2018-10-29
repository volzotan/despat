package de.volzo.despat.persistence;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface BenchmarkDao {

    @Query("SELECT * FROM benchmark")
    List<Benchmark> getAll();

    @Query("SELECT * FROM benchmark WHERE type = :type")
    List<Benchmark> getAllOfType(int type);

    @Query("SELECT * FROM benchmark WHERE detector = :detector")
    List<Benchmark> getAllByDetector(String detector);

    @Insert
    void insert(Benchmark... benchmarks);

    @Update
    void update(Benchmark... benchmarks);

    @Delete
    void delete(Benchmark benchmark);

    @Query("DELETE FROM benchmark")
    public void dropTable();
}