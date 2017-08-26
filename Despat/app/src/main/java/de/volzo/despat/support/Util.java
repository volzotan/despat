package de.volzo.despat.support;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.StatFs;

import java.io.File;

import de.volzo.despat.Orchestrator;
import de.volzo.despat.R;

/**
 * Created by volzotan on 16.08.17.
 */

public class Util {

    public static final int NOTIFICATION_IDENTIFIER = 0x1002;

    public static void startNotification(Context context, int numberOfImages) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification.Builder(context.getApplicationContext())
                .setContentTitle("Despat active")
                .setContentText("Number of images: " + numberOfImages)
                .setSmallIcon(R.drawable.ic_notification)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                .setOngoing(true)
                .build();

        notificationManager.notify(NOTIFICATION_IDENTIFIER, notification);
    }

    public static void updateNotification(Context context, int numberOfImages) {
        startNotification(context, numberOfImages);
    }

    public static void stopNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_IDENTIFIER);
    }

    public static float getFreeSpaceOnDevice(File dir) {
        StatFs stat = new StatFs(dir.getPath());
        long bytesAvailable = 0;
        bytesAvailable = (long) stat.getBlockSizeLong() * (long) stat.getAvailableBlocksLong();

        return bytesAvailable / (1024.f * 1024.f);
    }

}
