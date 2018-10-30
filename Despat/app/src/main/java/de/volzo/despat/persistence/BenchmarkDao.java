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

    @Query("SELECT * FROM benchmark WHERE detector = :detector AND type = :type ORDER BY timestamp DESC LIMIT 3")
    List<Benchmark> getLast3ByDetectorOfType(String detector, int type);

    @Insert
    void insert(Benchmark... benchmarks);

    @Update
    void update(Benchmark... benchmarks);

    @Delete
    void delete(Benchmark benchmark);

    @Query("DELETE FROM benchmark")
    public void dropTable();
}