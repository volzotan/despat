package de.volzo.despat.persistence;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.util.Date;

/**
 * Created by christophergetschmann on 24.11.17.
 */

@Entity
public class Event {

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

    public class EventType {
        public static final int INIT                = 10;
        public static final int BOOT                = 20;
        public static final int SHUTDOWN            = 30;

        public static final int START               = 40;
        public static final int STOP                = 41;
        public static final int RESTART             = 42;

        public static final int INFO                = 45;

        public static final int ERROR               = 50;
        public static final int SCHEDULE_GLITCH     = 51;

        public static final int SLEEP_MODE_CHANGE   = 60;
        public static final int DISPLAY_ON          = 61;
        public static final int DISPLAY_OFF         = 62;
    }
}