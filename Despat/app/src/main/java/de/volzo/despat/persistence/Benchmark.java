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

    public static final int TYPE_IMAGE = 1;
    public static final int TYPE_TILE  = 2;

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "session_id")
    private Long sessionId; // can be null, Long instead of long

    @ColumnInfo(name = "timestamp")
    private Date timestamp;

    @ColumnInfo(name = "type")
    private int type;

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

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
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