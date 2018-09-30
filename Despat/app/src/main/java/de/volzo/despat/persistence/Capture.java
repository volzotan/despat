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

public class Capture {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "session_id")
    private long sessionId;

    @ColumnInfo(name = "recording_time")
    private Date recordingTime;

    @ColumnInfo(name = "image_path")
    private File image;

    @ColumnInfo(name = "processed_detector")
    private boolean processed_detector;

    @ColumnInfo(name = "processed_compressor")
    private boolean processed_compressor;

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

    public boolean isProcessed_detector() {
        return processed_detector;
    }

    public void setProcessed_detector(boolean processed_detector) {
        this.processed_detector = processed_detector;
    }

    public boolean isProcessed_compressor() {
        return processed_compressor;
    }

    public void setProcessed_compressor(boolean processed_compressor) {
        this.processed_compressor = processed_compressor;
    }
}