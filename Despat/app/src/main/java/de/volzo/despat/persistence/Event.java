package de.volzo.despat.persistence;

import android.util.Log;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity
public class Event {

    private static final String TAG = Event.class.getSimpleName();

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "timestamp")
    private Date timestamp;

    @ColumnInfo(name = "type")
    private int type;

    @ColumnInfo(name = "payload")
    private String payload;

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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getTypeAsString() {
        switch (type) {
            case EventType.INIT:
                return "INIT";
            case EventType.BOOT:
                return "BOOT";
            case EventType.SHUTDOWN:
                return "SHUTDOWN";
            case EventType.SESSION_START:
                return "SESSION_START";
            case EventType.SESSION_STOP:
                return "SESSION_STOP";
            case EventType.SESSION_RESTART:
                return "SESSION_RESTART";
            case EventType.INFO:
                return "INFO";
            case EventType.ERROR:
                return "ERROR";
            case EventType.SCHEDULE_GLITCH:
                return "SCHEDULE_GLITCH";
            case EventType.SLEEP_MODE_CHANGE:
                return "SLEEP_MODE_CHANGE";
            case EventType.DISPLAY_ON:
                return "DISPLAY_ON";
            case EventType.DISPLAY_OFF:
                return "DISPLAY_OFF";
            case EventType.SYNC:
                return "SYNC";
            case EventType.LOW_BATTERY_STOP:
                return "LOW_BATTERY_STOP";
            case EventType.LOW_MEMORY_STOP:
                return "LOW_MEMORY_STOP";
            case EventType.WAKELOCK_ACQUIRE:
                return "WAKELOCK_ACQUIRE";
            case EventType.WAKELOCK_RELEASE:
                return "WAKELOCK_RELEASE";
            default:
                Log.w(TAG, "undefined EventType");
                return "UNDEFINED";
        }
    }

    public static class EventType {

        // device
        public static final int INIT                = 10;
        public static final int BOOT                = 20;
        public static final int SHUTDOWN            = 30;

        // session
        public static final int SESSION_START       = 40;
        public static final int SESSION_STOP        = 41;
        public static final int SESSION_RESTART     = 42;

        // general info
        public static final int INFO                = 45;

        // general error
        public static final int ERROR               = 50;
        public static final int SCHEDULE_GLITCH     = 51;

        // power
        public static final int SLEEP_MODE_CHANGE   = 60;
        public static final int DISPLAY_ON          = 61;
        public static final int DISPLAY_OFF         = 62;

        // sync
        public static final int SYNC                = 70;

        // warning
        public static final int LOW_BATTERY_STOP    = 80;
        public static final int LOW_MEMORY_STOP     = 81;

        // wakelock
        public static final int WAKELOCK_ACQUIRE    = 90;
        public static final int WAKELOCK_RELEASE    = 91;

    }
}