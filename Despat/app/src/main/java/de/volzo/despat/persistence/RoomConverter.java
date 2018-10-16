package de.volzo.despat.persistence;

import androidx.room.TypeConverter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import java.io.File;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.volzo.despat.preferences.Config;

public class RoomConverter {

    @TypeConverter
    public static Date stringToDate(String timeString) {
        DateFormat dateFormat = new SimpleDateFormat(Config.DATEFORMAT, new Locale("de", "DE"));
        try {
            return timeString == null ? null : dateFormat.parse(timeString);
        } catch (ParseException e) {
            Log.e("RoomConverter", "unit conversion failed", e);
            return null;
        }
    }

    @TypeConverter
    public static String dateToString(Date date) {
        DateFormat dateFormat = new SimpleDateFormat(Config.DATEFORMAT, new Locale("de", "DE"));
        return date == null ? null : dateFormat.format(date);
    }

    @TypeConverter
    public static File stringToFile(String path) {
        return path == null ? null : new File(path);
    }

    @TypeConverter
    public static String fileToString(File file) {
        return file == null ? null : file.getAbsolutePath();
    }

    @TypeConverter
    public static String rectToString(Rect rect) {
        if (rect == null) return null;
        return String.format(Config.LOCALE, "%d;%d;%d;%d", rect.left, rect.top, rect.right, rect.bottom);
    }

    @TypeConverter
    public static Rect stringToRect(String rect) {
        if (rect == null || rect.equals("")) {
            return null;
        }
        String[] numbers = rect.split(";");
        return new Rect(Integer.parseInt(numbers[0]), Integer.parseInt(numbers[1]), Integer.parseInt(numbers[2]), Integer.parseInt(numbers[3]));
    }

    @TypeConverter
    public static Double[][] stringToDoubleArray(String doubleArrayString) {
        if (doubleArrayString == null || doubleArrayString.length() == 0) {
            return null;
        }

        String[] rowStrings = doubleArrayString.split("\n");
        List<List<Double>> arrList = new ArrayList<>();

        for (String row : rowStrings) {
            String[] cols = row.split(" ");
            List<Double> curRow = new ArrayList<>();

            for (String col : cols) {
                curRow.add(Double.parseDouble(col));
            }

            arrList.add(curRow);
        }

        Double[][] ret = new Double[arrList.size()][arrList.get(0).size()];

        for (int rows = 0; rows<arrList.size(); rows++) {
            for (int cols = 0; cols < arrList.get(0).size(); cols++) {
                ret[rows][cols] = arrList.get(rows).get(cols);
            }
        }

        return ret;
    }

    @TypeConverter
    public static String doubleArrayToString(Double[][] array) {
        if (array == null) return null;

        StringBuilder sb = new StringBuilder();
        for (int rows = 0; rows<array.length; rows++) {
            for (int cols = 0; cols<array[0].length; cols++) {
                sb.append(Double.toString(array[rows][cols]));
                sb.append(" ");
            }
            sb.append("\n");
        }

        return sb.toString();
    }

}
