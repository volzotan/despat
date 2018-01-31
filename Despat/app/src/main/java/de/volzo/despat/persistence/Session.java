package de.volzo.despat.persistence;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;
import android.graphics.Bitmap;
import android.location.Location;

import java.io.File;
import java.util.Date;

/**
 * Created by christophergetschmann on 24.11.17.
 */

@Entity

public class Session {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "name")
    private String sessionName;

    @ColumnInfo(name = "start")
    private Date start;

    @ColumnInfo(name = "end")
    private Date end;

    @ColumnInfo(name = "latitude")
    private Double latitude;

    @ColumnInfo(name = "longitude")
    private Double longitude;

    @ColumnInfo(name = "exclusion_image", typeAffinity = ColumnInfo.BLOB)
    private byte[] exclusionImage;

    @ColumnInfo(name = "compressed_image")
    private File compressedImage;

    @ColumnInfo(name = "resumed")
    private boolean resumed = false;

    // --- additional information --- //

    @ColumnInfo(name = "shutter_interval")
    private Integer shutterIntervall;

    @ColumnInfo(name = "shutter_interval")
    private Double exposureThreshold;

    @ColumnInfo(name = "exposure_compensation")
    private Double exposureCompensation;

    public Location getLocation() {

        if (getLatitude() == null || getLongitude() == null) return null;

        Location loc = new Location("despatProvider");
        loc.setLatitude(getLatitude());
        loc.setLongitude(getLongitude());
        return loc;
    }

    public void setLocation(Location location) {
        if (location == null) return;

        setLatitude(location.getLatitude());
        setLongitude(location.getLongitude());
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
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

    public byte[] getExclusionImage() {
        return exclusionImage;
    }

    public void setExclusionImage(byte[] exclusionImage) {
        this.exclusionImage = exclusionImage;
    }

    public File getCompressedImage() {
        return compressedImage;
    }

    public void setCompressedImage(File compressedImage) {
        this.compressedImage = compressedImage;
    }

    public boolean isResumed() {
        return resumed;
    }

    public void setResumed(boolean resumed) {
        this.resumed = resumed;
    }

    public Integer getShutterIntervall() {
        return shutterIntervall;
    }

    public void setShutterIntervall(Integer shutterIntervall) {
        this.shutterIntervall = shutterIntervall;
    }

    public Double getExposureThreshold() {
        return exposureThreshold;
    }

    public void setExposureThreshold(Double exposureThreshold) {
        this.exposureThreshold = exposureThreshold;
    }

    public Double getExposureCompensation() {
        return exposureCompensation;
    }

    public void setExposureCompensation(Double exposureCompensation) {
        this.exposureCompensation = exposureCompensation;
    }
}