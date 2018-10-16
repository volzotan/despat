package de.volzo.despat.persistence;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.File;

import static androidx.room.ForeignKey.CASCADE;

@Entity(foreignKeys = @ForeignKey(  entity = Capture.class,
                                    parentColumns = "id",
                                    childColumns = "capture_id",
                                    onDelete = CASCADE),
        indices = {@Index("capture_id")})

public class Position {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "capture_id")
    private long captureId;

    @ColumnInfo(name = "minx")
    private Float minx;

    @ColumnInfo(name = "miny")
    private Float miny;

    @ColumnInfo(name = "maxx")
    private Float maxx;

    @ColumnInfo(name = "maxy")
    private Float maxy;

    @ColumnInfo(name = "x")
    private Float x;

    @ColumnInfo(name = "y")
    private Float y;

    @ColumnInfo(name = "latitude")
    private Double latitude;

    @ColumnInfo(name = "longitude")
    private Double longitude;

    @ColumnInfo(name = "type")
    private String type;

    @ColumnInfo(name = "type_id")
    private int typeId;

    @ColumnInfo(name = "recognition_confidence")
    private Float recognitionConfidence;

    @ColumnInfo(name = "position_confidence")
    private Float positionConfidence;

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

    public Float getMinx() {
        return minx;
    }

    public void setMinx(Float minx) {
        this.minx = minx;
    }

    public Float getMiny() {
        return miny;
    }

    public void setMiny(Float miny) {
        this.miny = miny;
    }

    public Float getMaxx() {
        return maxx;
    }

    public void setMaxx(Float maxx) {
        this.maxx = maxx;
    }

    public Float getMaxy() {
        return maxy;
    }

    public void setMaxy(Float maxy) {
        this.maxy = maxy;
    }

    public Float getX() {
        return x;
    }

    public void setX(Float x) {
        this.x = x;
    }

    public Float getY() {
        return y;
    }

    public void setY(Float y) {
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public Float getRecognitionConfidence() {
        return recognitionConfidence;
    }

    public void setRecognitionConfidence(Float recognitionConfidence) {
        this.recognitionConfidence = recognitionConfidence;
    }

    public Float getPositionConfidence() {
        return positionConfidence;
    }

    public void setPositionConfidence(Float positionConfidence) {
        this.positionConfidence = positionConfidence;
    }
}