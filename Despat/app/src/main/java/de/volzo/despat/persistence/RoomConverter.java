package de.volzo.despat.persistence;

import android.arch.persistence.room.TypeConverter;
import android.util.Log;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import de.volzo.despat.support.Config;
import de.volzo.despat.web.ServerConnector;

/**
 * Created by christophergetschmann on 18.01.18.
 */

public class RoomConverter {

    @TypeConverter
    public static Date toDate(String timeString) {
        DateFormat dateFormat = new SimpleDateFormat(Config.DATEFORMAT, new Locale("de", "DE"));
        try {
            return timeString == null ? null : dateFormat.parse(timeString);
        } catch (ParseException e) {
            Log.e("RoomConverter", "unit conversion failed", e);
            return null;
        }
    }

    @TypeConverter
    public static String toTimeString(Date date) {
        DateFormat dateFormat = new SimpleDateFormat(Config.DATEFORMAT, new Locale("de", "DE"));
        return date == null ? null : dateFormat.format(Calendar.getInstance().getTime());
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
