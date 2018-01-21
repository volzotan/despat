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

    /*

    sid

    name
    start
    end

    location

    exclusion_image
    compressed_image

    [pointer to positionSets]

     */

    @PrimaryKey(autoGenerate = true)
    private long sid;

    @ColumnInfo(name = "name")
    private String sessionName;

    @ColumnInfo(name = "start")
    private Date start;

    @ColumnInfo(name = "end")
    private Date end;

    @ColumnInfo(name = "latitude")
    private double latitude;

    @ColumnInfo(name = "longitude")
    private double longitude;

    @ColumnInfo(name = "exclusion_image", typeAffinity = ColumnInfo.BLOB)
    private byte[] exclusionImage;

    @ColumnInfo(name = "compressed_image")
    private File compressedImage;

    @ColumnInfo(name = "resumed")
    private boolean resumed = false;



    public Location getLocation() {
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

    public long getSid() {
        return sid;
    }

    public void setSid(long sid) {
        this.sid = sid;
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

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
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
}