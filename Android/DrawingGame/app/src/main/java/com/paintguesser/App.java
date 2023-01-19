package com.paintguesser;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;

import com.paintguesser.network.backend.BackendRepository;
import com.paintguesser.network.socket.client.Client;
import com.paintguesser.network.socket.server.Server;
import com.paintguesser.persistance.GameHistoryDbHelper;
import com.paintguesser.persistance.GameHistoryPersistence;
import com.paintguesser.persistance.Preferences;

import java.io.IOException;

public class App extends Application {

    public static Server server;
    public static Client client;
    public static BackendRepository backendRepository;
    public static GameHistoryPersistence persistence;
    public static Preferences preferences;

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            server = new Server();
            client = new Client();
            backendRepository = new BackendRepository();
            persistence = new GameHistoryDbHelper(this);
            preferences = new Preferences(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    TimerService.NOTIFICATION_CHANNEL_ID,
                    getString(R.string.service_notification_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT);

            channel.setSound(null, null);

            NotificationManager manager = getSystemService(NotificationManager.class);

            manager.createNotificationChannel(channel);
        }
    }
}
