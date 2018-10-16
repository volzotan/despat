package de.volzo.despat.persistence;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import android.graphics.Bitmap;
import android.location.Location;
import android.util.Size;

import java.io.File;
import java.util.Date;

import de.volzo.despat.preferences.CameraConfig;
import de.volzo.despat.preferences.DetectorConfig;

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

    @ColumnInfo(name = "image_width")
    private int imageWidth;

    @ColumnInfo(name = "image_height")
    private int imageHeight;

    @ColumnInfo(name = "exclusion_image", typeAffinity = ColumnInfo.BLOB)
    private byte[] exclusionImage;

    @ColumnInfo(name = "compressed_image")
    private File compressedImage;

    @ColumnInfo(name = "homography_matrix")
    private Double[][] homographyMatrix;

    @ColumnInfo(name = "resumed")
    private boolean resumed = false;

    // --- settings --- //

    @Embedded(prefix = "detectorconfig_")
    private DetectorConfig detectorConfig;

    @Embedded(prefix = "cameraconfig_")
    private CameraConfig cameraConfig;

    @ColumnInfo(name = "exposure_threshold")
    private Double exposureThreshold;

    @ColumnInfo(name = "exposure_compensation")
    private Double exposureCompensation;

    // --- non standard getter and setter --- //

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

    public Size getImageSize() {
        return new Size(imageWidth, imageHeight);
    }

    public void setImageSize(Size imageSize) {
        imageWidth = imageSize.getWidth();
        imageHeight = imageSize.getHeight();
    }

    public Integer getShutterInterval() {
        if (cameraConfig != null) {
            return cameraConfig.getShutterInterval();
        } else {
            return null;
        }
    }

    // ---------------------------------------------------------------------------------------------

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

    public int getImageWidth() {
        return imageWidth;
    }

    public void setImageWidth(int imageWidth) {
        this.imageWidth = imageWidth;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    public void setImageHeight(int imageHeight) {
        this.imageHeight = imageHeight;
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

    public Double[][] getHomographyMatrix() {
        return homographyMatrix;
    }

    public void setHomographyMatrix(Double[][] homographyMatrix) {
        this.homographyMatrix = homographyMatrix;
    }

    public boolean isResumed() {
        return resumed;
    }

    public void setResumed(boolean resumed) {
        this.resumed = resumed;
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

    public DetectorConfig getDetectorConfig() {
        return detectorConfig;
    }

    public void setDetectorConfig(DetectorConfig detectorConfig) {
        this.detectorConfig = detectorConfig;
    }

    public CameraConfig getCameraConfig() {
        return cameraConfig;
    }

    public void setCameraConfig(CameraConfig cameraConfig) {
        this.cameraConfig = cameraConfig;
    }

    public String toString() {
        return "[" + this.id + "] " + this.sessionName;
    }
}