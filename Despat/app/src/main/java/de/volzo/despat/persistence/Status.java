package de.volzo.despat.persistence;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;

import java.util.Date;

/**
 * Created by christophergetschmann on 24.11.17.
 */

@Entity
public class Status {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "timestamp")
    private Date timestamp;

    @ColumnInfo(name = "images_taken")
    private int numberImagesTaken;

    @ColumnInfo(name = "images_in_memory")
    private int numberImagesInMemory;

    @ColumnInfo(name = "free_space_internal")
    private float freeSpaceInternal;

    @ColumnInfo(name = "free_space_external")
    private float freeSpaceExternal;

    @ColumnInfo(name = "battery_internal")
    private int batteryInternal;

    @ColumnInfo(name = "battery_external")
    private int batteryExternal;

    @ColumnInfo(name = "state_charging")
    private boolean stateCharging;

    @ColumnInfo(name = "temperature_device")
    private float temperatureDevice;

    @ColumnInfo(name = "temperature_battery")
    private float temperatureBattery;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public int getNumberImagesTaken() {
        return numberImagesTaken;
    }

    public void setNumberImagesTaken(int numberImagesTaken) {
        this.numberImagesTaken = numberImagesTaken;
    }

    public int getNumberImagesInMemory() {
        return numberImagesInMemory;
    }

    public void setNumberImagesInMemory(int numberImagesInMemory) {
        this.numberImagesInMemory = numberImagesInMemory;
    }

    public float getFreeSpaceInternal() {
        return freeSpaceInternal;
    }

    public void setFreeSpaceInternal(float freeSpaceInternal) {
        this.freeSpaceInternal = freeSpaceInternal;
    }

    public float getFreeSpaceExternal() {
        return freeSpaceExternal;
    }

    public void setFreeSpaceExternal(float freeSpaceExternal) {
        this.freeSpaceExternal = freeSpaceExternal;
    }

    public int getBatteryInternal() {
        return batteryInternal;
    }

    public void setBatteryInternal(int batteryInternal) {
        this.batteryInternal = batteryInternal;
    }

    public int getBatteryExternal() {
        return batteryExternal;
    }

    public void setBatteryExternal(int batteryExternal) {
        this.batteryExternal = batteryExternal;
    }

    public boolean isStateCharging() {
        return stateCharging;
    }

    public void setStateCharging(boolean stateCharging) {
        this.stateCharging = stateCharging;
    }

    public float getTemperatureDevice() {
        return temperatureDevice;
    }

    public void setTemperatureDevice(float temperatureDevice) {
        this.temperatureDevice = temperatureDevice;
    }

    public float getTemperatureBattery() {
        return temperatureBattery;
    }

    public void setTemperatureBattery(float temperatureBattery) {
        this.temperatureBattery = temperatureBattery;
    }
}