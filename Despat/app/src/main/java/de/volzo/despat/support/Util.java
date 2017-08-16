package de.volzo.despat.support;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.BitmapFactory;

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
                .setOngoing(true) // Again, THIS is the important line. This method lets the notification to stay.
                .build();

        notificationManager.notify(NOTIFICATION_IDENTIFIER, notification);
    }

    public static void stopNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_IDENTIFIER);
    }

}
