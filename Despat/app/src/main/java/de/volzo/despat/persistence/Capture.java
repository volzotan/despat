package de.volzo.despat.persistence;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.volzo.despat.preferences.Config;

import static androidx.room.ForeignKey.CASCADE;

@Entity(foreignKeys = @ForeignKey(  entity = Session.class,
                                    parentColumns = "id",
                                    childColumns = "session_id",
                                    onDelete = CASCADE),
        indices = {@Index("session_id")})

public class Capture {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "session_id")
    private long sessionId;

    @ColumnInfo(name = "recording_time")
    private Date recordingTime;

    @ColumnInfo(name = "image_path")
    private File image;

    @ColumnInfo(name = "exposure_time")
    private long exposureTime;

    @ColumnInfo(name = "aperture")
    private double aperture;

    @ColumnInfo(name = "iso")
    private int iso;

    @ColumnInfo(name = "autofocus_state")
    private int autofocusState;

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

    public long getExposureTime() {
        return exposureTime;
    }

    public void setExposureTime(long exposureTime) {
        this.exposureTime = exposureTime;
    }

    public double getAperture() {
        return aperture;
    }

    public void setAperture(double aperture) {
        this.aperture = aperture;
    }

    public int getIso() {
        return iso;
    }

    public void setIso(int iso) {
        this.iso = iso;
    }

    public int getAutofocusState() {
        return autofocusState;
    }

    public void setAutofocusState(int autofocusState) {
        this.autofocusState = autofocusState;
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

    public String toString() {
        SimpleDateFormat df = new SimpleDateFormat(Config.DATEFORMAT);
        return String.format(
                "[%d] %s (%b|%b)",
                this.id,
                df.format(this.recordingTime),
                this.isProcessed_detector(),
                this.isProcessed_compressor()
        );
    }
}