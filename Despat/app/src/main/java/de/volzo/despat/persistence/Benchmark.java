package de.volzo.despat.persistence;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;

import java.io.File;
import java.util.Date;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity(foreignKeys = @ForeignKey(  entity = Session.class,
                                    parentColumns = "id",
                                    childColumns = "session_id",
                                    onDelete = CASCADE))

public class Benchmark {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "session_id")
    private long sessionId;

    @ColumnInfo(name = "timestamp")
    private Date timestamp;

    @ColumnInfo(name = "detector")
    private String detector;

    @ColumnInfo(name = "inference_time")
    private double inferenceTime;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getSessionId() {
        return sessionId;
    }

    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getDetector() {
        return detector;
    }

    public void setDetector(String detector) {
        this.detector = detector;
    }

    public double getInferenceTime() {
        return inferenceTime;
    }

    public void setInferenceTime(double inferenceTime) {
        this.inferenceTime = inferenceTime;
    }
}