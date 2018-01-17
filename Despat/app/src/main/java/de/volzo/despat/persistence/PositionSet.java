package de.volzo.despat.persistence;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by christophergetschmann on 24.11.17.
 */

@Entity(foreignKeys = @ForeignKey(  entity = Session.class,
                                    parentColumns = "sid",
                                    childColumns = "session"))

public class PositionSet {

    @PrimaryKey(autoGenerate = true)
    private int pid;

    @ColumnInfo(name = "session")
    private int session;

    @ColumnInfo(name = "time")
    private int recordingTime;

    @ColumnInfo(name = "image_path")
    private int image;


    // Getters and setters are ignored for brevity,
    // but they're required for Room to work.
}