package de.volzo.despat.persistence;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import de.volzo.despat.preferences.Config;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import static androidx.room.ForeignKey.CASCADE;

@Entity(foreignKeys = @ForeignKey(  entity = Session.class,
                                    parentColumns = "id",
                                    childColumns = "session_id",
                                    onDelete = CASCADE),
        indices = {@Index("session_id")})

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

    @ColumnInfo(name = "tilesize")
    private int tilesize;

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

    public int getTilesize() {
        return tilesize;
    }

    public void setTilesize(int tilesize) {
        this.tilesize = tilesize;
    }

    public double getInferenceTime() {
        return inferenceTime;
    }

    public void setInferenceTime(double inferenceTime) {
        this.inferenceTime = inferenceTime;
    }

    public String toString() {
        String typeString = "";

        switch (type) {
            case TYPE_IMAGE: {
                typeString = "image";
                break;
            }
            case TYPE_TILE: {
                typeString = "tile";
                break;
            }
            default: {
                typeString = "unknown";
                break;
            }
        }

        SimpleDateFormat sdf = new SimpleDateFormat(Config.DATEFORMAT_SHORT);
        StringBuilder sb = new StringBuilder();
        sb.append("["); sb.append(id); sb.append("]");
        sb.append(" ");
        sb.append(sdf.format(timestamp));
        sb.append(" ");
        sb.append(typeString);
        sb.append(" ");
        sb.append(detector);
        sb.append("@");
        sb.append(tilesize);
        sb.append(" | ");
        sb.append(inferenceTime);

        return sb.toString();
    }
}