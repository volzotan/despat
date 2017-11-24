package de.volzo.despat.messaging;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

/**
 * Created by christophergetschmann on 24.11.17.
 */

@Database(entities = {StatusMessage.class}, version = 1)
public abstract class MessageDatabase extends RoomDatabase {
    public abstract StatusMessageDao statusMessageDao();
}
