package de.volzo.despat.support;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.util.List;

import de.volzo.despat.MainActivity;
import de.volzo.despat.R;
import de.volzo.despat.services.ShutterService;

public class NotificationUtil {

    private static final String TAG = NotificationUtil.class.getSimpleName();

    @TargetApi(Build.VERSION_CODES.O)
    private static void setUpShutterNotificationChannel(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.deleteNotificationChannel(ShutterService.NOTIFICATION_CHANNEL_ID);

        NotificationChannel channel = new NotificationChannel(
                ShutterService.NOTIFICATION_CHANNEL_ID,
                "ShutterServiceNotification",
                NotificationManager.IMPORTANCE_DEFAULT);

        channel.setSound(null, null);
        notificationManager.createNotificationChannel(channel);
    }

    public static Notification getShutterNotification(Context context, int numberOfCaptures, int numberOfErrors, List<String> additionalInfo) {
        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        String contentText = numberOfCaptures + " captures";
        if (numberOfErrors > 0) {
            contentText = contentText + " | " + numberOfErrors + " errors";
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setUpShutterNotificationChannel(context);
        }

        Notification.Builder builder =  new Notification.Builder(context.getApplicationContext())
                .setContentTitle("despat active")
                .setContentText(contentText)
                .setSmallIcon(R.mipmap.despat_icon_notification)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.despat_icon))
                .setContentIntent(pendingIntent)
                .setPriority(Notification.PRIORITY_DEFAULT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(ShutterService.NOTIFICATION_CHANNEL_ID);
        }

        Notification.InboxStyle inboxNotification = new Notification.InboxStyle(builder);

        inboxNotification.addLine(numberOfCaptures + " captures");
        inboxNotification.addLine(numberOfErrors + " errors");
        if (additionalInfo != null) {
            for (String info : additionalInfo) {
                inboxNotification.addLine(info);
            }
        }

        Notification notification = inboxNotification.build();
        return notification;
    }

    public static void updateShutterNotification(
            Context context,
            int notificationIdentifier,
            int numberOfCaptures,
            int numberOfErrors,
            List<String> additionalInfo) {

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        StatusBarNotification[] notifications = notificationManager.getActiveNotifications();
        for (StatusBarNotification not : notifications) {
            if (not.getId() == notificationIdentifier) {
                // only update if already existing
                notificationManager.notify(notificationIdentifier, getShutterNotification(context, numberOfCaptures, numberOfErrors, additionalInfo));
                return;
            }
        }

        Log.w(TAG, "no notification to update");
    }

    public static void stopShutterNotification(Context context, int notificationIdentifier) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notificationIdentifier);
    }

    public static void showStandardNotification(Context context, String message) {
        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

        Notification notification = new Notification.Builder(context.getApplicationContext())
                .setContentTitle("despat")
                .setContentText(message)
                .setSmallIcon(R.mipmap.despat_icon_notification)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.despat_icon))
                .setContentIntent(pendingIntent)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .build();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0x100, notification);
    }

    // ---------------------------------------------------------------------------------------------

    @TargetApi(Build.VERSION_CODES.O)
    public static void setUpServiceProgressNotificationChannel(Context context, String notificationChannelId, String channelName) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.deleteNotificationChannel(ShutterService.NOTIFICATION_CHANNEL_ID);
        NotificationChannel channel = new NotificationChannel(
                notificationChannelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.setSound(null, null);
        notificationManager.createNotificationChannel(channel);
    }


    public static void showProgressNotification(Context context, int pos, int total, String title, String content, int notificationId, String notificationChannelId, String notificationChannelName) {
        showProgressNotification(context, pos, total, title, content, notificationId, notificationChannelId, notificationChannelName, -1);
    }

    public static void showProgressNotification(Context context, int pos, int total, String title, String content, int notificationId, String notificationChannelId, String notificationChannelName, int timeout) {
        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setUpServiceProgressNotificationChannel(context, notificationChannelId, notificationChannelName);
        }

        Notification.Builder builder =  new Notification.Builder(context.getApplicationContext())
                .setContentTitle(title)
                .setContentText(content)
                .setProgress(total, pos, false)
                .setSmallIcon(R.mipmap.despat_icon_notification)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.despat_icon))
                .setContentIntent(pendingIntent)
                .setPriority(Notification.PRIORITY_DEFAULT);

        if (timeout > 0) {
            if (Build.VERSION.SDK_INT >= 26) {
                builder.setTimeoutAfter(timeout);
            } else {
                Log.d(TAG, "timeout not supported due to API level. Notification deleted");
                notificationManager.cancel(notificationId);
                return;
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(notificationChannelId);
        }
        notificationManager.notify(notificationId, builder.build());
    }

}
