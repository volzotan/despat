package de.volzo.despat.persistence;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.util.Date;

import de.volzo.despat.detector.Detector;

import static androidx.room.ForeignKey.CASCADE;

@Entity(foreignKeys = @ForeignKey(  entity = Session.class,
                                    parentColumns = "id",
                                    childColumns = "session_id",
                                    onDelete = CASCADE),
        indices = {@Index("session_id")})

public class HomographyPoint {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "session_id")
    private long sessionId;

    @ColumnInfo(name = "modification_time")
    private Date modificationTime;

    @ColumnInfo(name = "x")
    private Double x;

    @ColumnInfo(name = "y")
    private Double y;

    @ColumnInfo(name = "latitude")
    private Double latitude;

    @ColumnInfo(name = "longitude")
    private Double longitude;

    public HomographyPoint() {}

    @Ignore
    public HomographyPoint(Double x, Double y, Double latitude, Double longitude) {
        this.x = x;
        this.y = y;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Ignore
    public HomographyPoint(int x, int y, Double latitude, Double longitude) {
        this.x = (double) x;
        this.y = (double) y;
        this.latitude = latitude;
        this.longitude = longitude;
    }

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

    public Date getModificationTime() {
        return modificationTime;
    }

    public void setModificationTime(Date modificationTime) {
        this.modificationTime = modificationTime;
    }

    public Double getX() {
        return x;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public Double getY() {
        return y;
    }

    public void setY(Double y) {
        this.y = y;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public LatLng getLocation() {
        return new LatLng(getLatitude(), getLongitude());
    }
}