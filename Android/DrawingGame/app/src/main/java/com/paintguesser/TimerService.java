package com.paintguesser;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

public class TimerService extends Service {

    public static final String NOTIFICATION_CHANNEL_ID = "TIMER_SERVICE_NOTIFICATION";
    public static final String TIME_COMMAND_KEY = "time";
    private static final int NOTIFICATION_ID = 1;
    private static final String TAG = "TimerService";
    private Notification.Builder builder;
    private NotificationManager manager;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, NOTIFICATION_CHANNEL_ID);
        } else {
            builder = new Notification.Builder(this);
        }

        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        builder = builder.setContentTitle(this.getString(R.string.app_name))
                .setContentText(getString(R.string.app_name))
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setOngoing(true)
                .setSound(null);

        startForeground(NOTIFICATION_ID, builder.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
        String time = null;
        if (intent != null) {
            time = intent.getStringExtra(TIME_COMMAND_KEY);
            if (time == null) {
                return START_NOT_STICKY;
            }
            builder.setContentText(time);
        }

        manager.notify(NOTIFICATION_ID, builder.build());
        return Service.START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }
}
