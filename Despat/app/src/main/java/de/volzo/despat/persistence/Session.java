package de.volzo.despat.persistence;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by christophergetschmann on 24.11.17.
 */

@Entity

public class Session {

    /*

    sid

    name
    start
    end

    exclusion_image
    compressed_image

    [pointer to positionSets]

     */

    @PrimaryKey(autoGenerate = true)
    private int sid;

    @ColumnInfo(name = "name")
    private String sessionName;

    @ColumnInfo(name = "start")
    private int start;



    // Getters and setters are ignored for brevity,
    // but they're required for Room to work.
}