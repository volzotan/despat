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
    private int eventType;

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

    public int getEventType() {
        return eventType;
    }

    public void setEventType(int eventType) {
        this.eventType = eventType;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
}