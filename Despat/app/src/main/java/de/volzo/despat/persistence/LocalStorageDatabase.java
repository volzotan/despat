package de.volzo.despat.persistence;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

/**
 * Created by christophergetschmann on 24.11.17.
 */

@Database(entities = {Status.class}, version = 1)
public abstract class LocalStorageDatabase extends RoomDatabase {
    public abstract StatusDao StatusDao();
}
