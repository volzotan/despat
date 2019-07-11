package de.volzo.despat.persistence;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.Date;

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

    @ColumnInfo(name = "free_memory_heap")
    private long freeMemoryHeap;

    @ColumnInfo(name = "free_memory_heap_native")
    private long freeMemoryHeapNative;

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

    public long getFreeMemoryHeap() {
        return freeMemoryHeap;
    }

    public void setFreeMemoryHeap(long freeMemoryHeap) {
        this.freeMemoryHeap = freeMemoryHeap;
    }

    public long getFreeMemoryHeapNative() {
        return freeMemoryHeapNative;
    }

    public void setFreeMemoryHeapNative(long freeMemoryHeapNative) {
        this.freeMemoryHeapNative = freeMemoryHeapNative;
    }
}