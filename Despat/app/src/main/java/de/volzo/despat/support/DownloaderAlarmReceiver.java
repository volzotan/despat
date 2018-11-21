package de.volzo.despat.support;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import com.google.android.vending.expansion.downloader.DownloaderClientMarshaller;

import de.volzo.despat.services.ObbDownloadService;

public class DownloaderAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            DownloaderClientMarshaller.startDownloadServiceIfRequired(context, intent, ObbDownloadService.class);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
}
