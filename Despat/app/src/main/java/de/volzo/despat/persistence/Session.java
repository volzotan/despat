package de.volzo.despat.persistence;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
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

    position

    exclusion_image
    compressed_image

    [pointer to positionSets]

     */

    @PrimaryKey(autoGenerate = true)
    private int sid;

    @ColumnInfo(name = "name")
    private String sessionName;

    @ColumnInfo(name = "start")
    private Date start;

    @ColumnInfo(name = "end")
    private Date end;

    @ColumnInfo(name = "position")
    private Location position;

    @ColumnInfo(name = "exclusion_image")
    private Bitmap exclusionImage;

    @ColumnInfo(name = "compressed_image")
    private File compressedImage;


    public int getSid() {
        return sid;
    }

    public void setSid(int sid) {
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

    public Location getPosition() {
        return position;
    }

    public void setPosition(Location position) {
        this.position = position;
    }

    public Bitmap getExclusionImage() {
        return exclusionImage;
    }

    public void setExclusionImage(Bitmap exclusionImage) {
        this.exclusionImage = exclusionImage;
    }

    public File getCompressedImage() {
        return compressedImage;
    }

    public void setCompressedImage(File compressedImage) {
        this.compressedImage = compressedImage;
    }
}