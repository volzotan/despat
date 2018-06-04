package de.volzo.despat.persistence;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

/**
 * Created by christophergetschmann on 24.11.17.
 */

@Database(entities = {  Status.class,
                        Session.class,
                        Capture.class,
                        HomographyPoint.class,
                        Position.class,
                        ErrorEvent.class,
                        Event.class},
                        version = 1)

@TypeConverters(RoomConverter.class)

public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase INSTANCE;

    public abstract StatusDao statusDao();
    public abstract SessionDao sessionDao();
    public abstract CaptureDao captureDao();
    public abstract HomographyPointDao homographyPointDao();
    // public abstract PositionDao positionDao();
    public abstract ErrorEventDao errorEventDao();
    public abstract EventDao eventDao();

    public static AppDatabase getAppDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE =
                    Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "despat-database")
                            // TODO
                            // allow queries on the main thread.
                            // Don't do this on a real app! See PersistenceBasicSample for an example.
                            .allowMainThreadQueries()
                            .build();
        }
        return INSTANCE;
    }

    public static void purgeDatabase(Context context) {
        AppDatabase db = AppDatabase.getAppDatabase(context);

        db.eventDao().dropTable();
        db.errorEventDao().dropTable();
        db.homographyPointDao().dropTable();
        db.captureDao().dropTable();
        db.sessionDao().dropTable();
        db.statusDao().dropTable();
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }
}