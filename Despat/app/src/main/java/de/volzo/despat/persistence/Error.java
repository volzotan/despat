package de.volzo.despat.persistence;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;

import java.io.File;
import java.util.Date;

/**
 * Created by christophergetschmann on 24.11.17.
 */

@Entity(foreignKeys = @ForeignKey(  entity = Session.class,
                                    parentColumns = "id",
                                    childColumns = "session_id"))

public class Error {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "session_id")
    private long sessionId;

    @ColumnInfo(name = "occurence_time")
    private Date occurenceTime;

    @ColumnInfo(name = "type")
    private String type;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "stacktrace")
    private String stacktrace;

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

    public Date getOccurenceTime() {
        return occurenceTime;
    }

    public void setOccurenceTime(Date occurenceTime) {
        this.occurenceTime = occurenceTime;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStacktrace() {
        return stacktrace;
    }

    public void setStacktrace(String stacktrace) {
        this.stacktrace = stacktrace;
    }
}