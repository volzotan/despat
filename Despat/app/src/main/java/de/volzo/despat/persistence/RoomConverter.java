package de.volzo.despat.persistence;

import android.arch.persistence.room.TypeConverter;

import java.io.File;
import java.util.Date;

/**
 * Created by christophergetschmann on 18.01.18.
 */

public class RoomConverter {

    @TypeConverter
    public static Date toDate(Long timestamp) {
        return timestamp == null ? null : new Date(timestamp);
    }

    @TypeConverter
    public static Long toTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

    @TypeConverter
    public static File toFile(String path) {
        return path == null ? null : new File(path);
    }

    @TypeConverter
    public static String toPath(File file) {
        return file == null ? null : file.getAbsolutePath();
    }

}
