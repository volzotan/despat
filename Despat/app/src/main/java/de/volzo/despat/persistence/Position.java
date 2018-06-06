package de.volzo.despat.persistence;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;

import java.io.File;

import static android.arch.persistence.room.ForeignKey.CASCADE;

/**
 * Created by christophergetschmann on 24.11.17.
 */

@Entity(foreignKeys = @ForeignKey(  entity = Capture.class,
                                    parentColumns = "id",
                                    childColumns = "capture_id",
                                    onDelete = CASCADE))

public class Position {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "capture_id")
    private long captureId;

    @ColumnInfo(name = "x")
    private int x;

    @ColumnInfo(name = "y")
    private int y;

    @ColumnInfo(name = "latitude")
    private String latitude;

    @ColumnInfo(name = "longitude")
    private String longitude;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCaptureId() {
        return captureId;
    }

    public void setCaptureId(long captureId) {
        this.captureId = captureId;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}