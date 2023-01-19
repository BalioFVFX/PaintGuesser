package com.paintguesser.network.socket.client;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.paintguesser.App;
import com.paintguesser.Utils;
import com.paintguesser.network.backend.BackendRepository;
import com.paintguesser.network.socket.Constants;
import com.paintguesser.network.socket.DrawData;
import com.paintguesser.network.socket.DrawDataTransformer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {

    public interface ConnectListener {
        void onConnected(String username);
        void onFailure();
    }

    public interface GameUpdatesListener {
        void onUpdate(DrawData drawData);
        void onTimerUpdate(final String time);
        void onGameStart();
        void onImageReceived(Bitmap bitmap);
        void onGameEnded();
    }

    private static final String TAG = "CLIENT_SOCKET";

    private volatile Socket socket;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ExecutorService readExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService writeExecutor = Executors.newSingleThreadExecutor();
    private final DrawDataTransformer<String, DrawData> transformer = new ClientDrawDataTransformer();
    private BufferedReader reader;
    private PrintWriter writer;

    public Client() {
    }

    public void connect(@NonNull String ip, int port, String clientUsername, ConnectListener listener) {
        readExecutor.execute(() -> {
            try {
                Log.d(TAG, "connect:" + ip + ":" + port);
                socket = new Socket(ip, port);
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new PrintWriter(socket.getOutputStream(), true);

                String line = reader.readLine();
                String username = line.substring(line.indexOf(":") + 1);

                writer.println(Constants.MSG_USERNAME + clientUsername);

                handler.post(() -> {
                   listener.onConnected(username);
                });

            } catch (Exception e) {
                e.printStackTrace();
                handler.post(() -> {
                    listener.onFailure();
                });
            }
        });
    }

    public void listenForUpdates(GameUpdatesListener updatesListener) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Log.d(TAG, "listenForUpdates");
                        final String line = reader.readLine();

                        if (line != null) {
                            if (line.startsWith(Constants.MSG_DRAW)) {
                                handleDrawing(line, updatesListener);
                            } else if (line.startsWith(Constants.MSG_TIMER)) {
                                handleTimer(line, updatesListener);
                            } else if (line.startsWith(Constants.MSG_START_GAME)) {
                                handleStartGame(updatesListener);
                            } else if (line.startsWith(Constants.MSG_IMG)) {
                                handleImage(line, updatesListener);
                            }
                        } else {
                            Log.d(TAG, "listenForUpdates: received null");
                            handler.post(updatesListener::onGameEnded);
                            break;
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                        break;
                    }
                    try {
                        Thread.sleep(1000 / 60);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                }
            }
        };

        readExecutor.execute(runnable);
    }

    public void sendGuess(String guess) {
        writeExecutor.execute(() -> {
            Log.d(TAG, "sendGuess:");
            writer.println(Constants.MSG_GUESS + guess);
        });
    }

    public void sendRating(float rating) {
        writeExecutor.execute(() -> {
            Log.d(TAG, "sendRating:");
            writer.println(Constants.MSG_RATING + rating);
        });
    }

    public void close() {
        Log.d(TAG, "Close called.");
        writeExecutor.execute(() -> {
            Utils.closeCloseable(socket);
            Utils.closeCloseable(writer);
            Utils.closeCloseable(reader);
            Log.d(TAG, "close: Everything is closed");
        });
    }

    private void handleDrawing(String line, GameUpdatesListener updatesListener) {
        final DrawData drawData = transformer.transform(line);

        handler.post(() -> {
            updatesListener.onUpdate(drawData);
        });
    }

    private void handleTimer(String line, GameUpdatesListener updatesListener) {
        handler.post(() -> {
            updatesListener.onTimerUpdate(line.substring(line.indexOf(":") + 1));
        });
    }

    private void handleStartGame(GameUpdatesListener gameUpdatesListener) {
        handler.post(() -> gameUpdatesListener.onGameStart());
    }

    private void handleImage(String imageUrl, GameUpdatesListener updatesListener) {
        imageUrl = imageUrl.substring(imageUrl.indexOf(":") + 1);
        App.backendRepository.getImageBitmap(imageUrl, new BackendRepository.OnResultListener<Bitmap>() {
            @Override
            public void onSuccess(Bitmap data) {
                updatesListener.onImageReceived(data);
            }

            @Override
            public void onError() {
                updatesListener.onImageReceived(null);
            }
        });
    }
}
