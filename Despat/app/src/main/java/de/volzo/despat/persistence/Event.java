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
    private long eid;

    @ColumnInfo(name = "timestamp")
    private Date timestamp;

    @ColumnInfo(name = "type")
    private int type;

    @ColumnInfo(name = "payload")
    private String payload;

    public long getEid() {
        return eid;
    }

    public void setEid(long eid) {
        this.eid = eid;
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
        public static final int INIT        = 10;
        public static final int BOOT        = 20;
        public static final int SHUTDOWN    = 30;

        public static final int START       = 40;
        public static final int STOP        = 41;

        public static final int ERROR       = 50;
    }
}