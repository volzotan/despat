package de.volzo.despat.support;

import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class Stopwatch {

    public static final String TAG = Stopwatch.class.getSimpleName();

    HashMap<String, Date> start;
    HashMap<String, ArrayList<Double>> runtime;

    public Stopwatch() {
        start = new HashMap<String, Date>();
        runtime = new HashMap<String, ArrayList<Double>>();
    }

    public void start(String key) {
        if (start.containsKey(key)) {
            Log.w(TAG, "restarting timer for event " + key + " without stopping first");
            stop(key);
        }

        start.put(key, Calendar.getInstance().getTime());
    }

    public void stop(String key) {
        if (!start.containsKey(key)) {
            Log.w(TAG, "failed stopping timer. key " + key + " missing");
        }

        Date startTime = start.remove(key);

        ArrayList<Double> measurements = runtime.get(key);
        if (measurements == null) {
            measurements = new ArrayList<Double>();
        }

        Date now = Calendar.getInstance().getTime();
        double diff = now.getTime() - startTime.getTime();
        measurements.add(diff);
        runtime.put(key, measurements);
    }

    public Double getLast(String key) {
        ArrayList<Double> times = runtime.get(key);
        return times.get(times.size()-1);
    }

    public List<Double> getLast(String key, int count) {
        ArrayList<Double> times = runtime.get(key);
        if (count > times.size()) return times;
        return times.subList(times.size()-count, times.size()-1);
    }

    public Double getAverage(String key) {
        return getAverage(key, 0, 0);
    }

    public Double getAverage(String key, int count) {
        ArrayList<Double> times = runtime.get(key);

        if (count < 0) {
            return getAverage(key, times.size()-count, times.size()-1);
        }

        if (count > 0) {
            return getAverage(key, 0, count-1);
        }

        return getAverage(key);
    }

    public Double getAverage(String key, int start, int end) {
        ArrayList<Double> times = runtime.get(key);

        if (start < 0) start = 0;
        if (start > end) start = end;
        if (end > times.size()-1) end = times.size()-1;

        List<Double> sublist;
        if (start == 0 & end == 0) {
            sublist = times;
        } else {
            sublist = times.subList(start, end);
        }

        double sum = 0;
        int counter = 0;

        for (Double value : sublist) {
            sum += value;
            counter++;
        }

        return sum/counter;
    }

    public void print() {
        StringBuilder sb = new StringBuilder();

        sb.append("STOPWATCH \n");

        for (String key : runtime.keySet()) {
            sb.append(String.format("%-40s | ", key));
            for (Double val : getLast(key, 10)) {
                sb.append(String.format(Locale.ENGLISH, "%6.0f ", val));
            }
            sb.append(String.format(Locale.ENGLISH,"| avg: %-5.3f", getAverage(key, -10)));
        }

        System.out.println(sb.toString());
    }

    public void reset() {
        start.clear();
        runtime.clear();
    }
}
