package de.volzo.despat.messaging;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by christophergetschmann on 24.11.17.
 */

@Entity
public class StatusMessage {

    @PrimaryKey
    private int mid;

    @ColumnInfo(name = "images_takes")
    private int numberImagesTaken;
    private int numberImagesSaved;
    private float freeSpaceInternal;
    private float freeSpaceExternal;

    private int batteryInternal;
    private int batteryExternal;
    private boolean stateCharging;

    private float temperature;

    // Getters and setters are ignored for brevity,
    // but they're required for Room to work.
}