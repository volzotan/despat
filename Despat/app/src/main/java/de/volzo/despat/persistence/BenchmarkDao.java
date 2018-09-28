package de.volzo.despat.persistence;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface BenchmarkDao {

    @Query("SELECT * FROM benchmark")
    List<Benchmark> getAll();

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