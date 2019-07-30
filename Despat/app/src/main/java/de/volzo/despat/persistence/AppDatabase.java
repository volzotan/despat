package de.volzo.despat.persistence;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import android.content.Context;

@Database(entities = {  Status.class,
                        Session.class,
                        Capture.class,
                        HomographyPoint.class,
                        Position.class,
                        ErrorEvent.class,
                        Event.class,
                        Benchmark.class,
                        DeviceLocation.class},
                        version = 1)

@TypeConverters(RoomConverter.class)

public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase INSTANCE;

    public abstract StatusDao statusDao();
    public abstract SessionDao sessionDao();
    public abstract CaptureDao captureDao();
    public abstract HomographyPointDao homographyPointDao();
    public abstract PositionDao positionDao();
    public abstract ErrorEventDao errorEventDao();
    public abstract EventDao eventDao();
    public abstract BenchmarkDao benchmarkDao();
    public abstract DeviceLocationDao deviceLocationDao();

    public static AppDatabase getAppDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE =
                    Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "despat-database")
                            // TODO
                            // allow queries on the main thread.
                            // Don't do this on a real app! See PersistenceBasicSample for an example.
                            .allowMainThreadQueries()
                            .fallbackToDestructiveMigration()
                            .build();
        }
        return INSTANCE;
    }

    public static void purgeDatabase(Context context) {
        AppDatabase.destroyInstance();
        AppDatabase db = AppDatabase.getAppDatabase(context);

        db.deviceLocationDao().dropTable();
        db.benchmarkDao().dropTable();
        db.eventDao().dropTable();
        db.errorEventDao().dropTable();
        db.homographyPointDao().dropTable();
        db.positionDao().dropTable();
        db.captureDao().dropTable();
        db.sessionDao().dropTable();
        db.statusDao().dropTable();
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }
}