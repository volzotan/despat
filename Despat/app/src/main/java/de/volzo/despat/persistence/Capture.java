package de.volzo.despat.persistence;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;

import java.io.File;
import java.util.Date;

/**
 * Created by christophergetschmann on 24.11.17.
 */

@Entity(foreignKeys = @ForeignKey(  entity = Session.class,
                                    parentColumns = "sid",
                                    childColumns = "session_id"))

public class Capture {

    @PrimaryKey(autoGenerate = true)
    private long cid;

    @ColumnInfo(name = "session_id")
    private long sessionId;

    @ColumnInfo(name = "time")
    private Date recordingTime;

    @ColumnInfo(name = "image_path")
    private File image;

    public long getCid() {
        return cid;
    }

    public void setCid(long cid) {
        this.cid = cid;
    }

    public long getSessionId() {
        return sessionId;
    }

    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

    public Date getRecordingTime() {
        return recordingTime;
    }

    public void setRecordingTime(Date recordingTime) {
        this.recordingTime = recordingTime;
    }

    public File getImage() {
        return image;
    }

    public void setImage(File image) {
        this.image = image;
    }
}