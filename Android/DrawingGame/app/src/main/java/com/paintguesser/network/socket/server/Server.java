package com.paintguesser.network.socket.server;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.paintguesser.Utils;
import com.paintguesser.network.socket.Constants;
import com.paintguesser.network.socket.DrawData;
import com.paintguesser.network.socket.DrawDataTransformer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    public interface ConnectionListener {
        void onClientConnected(String username);
    }

    public interface ClientListener {
        void onClientGuess(String guess);
        void onClientRated(float rating);
        void onGameEnded();
    }

    private static final String TAG = "SOCKET_SERVER";

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ExecutorService writeExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService readExecutor = Executors.newSingleThreadExecutor();
    private final DrawDataTransformer<DrawData, String> dataTransformer = new SeverDrawDataTransformer();
    private volatile ServerSocket serverSocket;
    private volatile BufferedReader reader;
    private volatile PrintWriter writer;
    private volatile Socket client;

    public Server() throws IOException {

    }

    public void acceptClient(String hostUsername, final ConnectionListener listener) {
        writeExecutor.execute(() -> {
            try {
                serverSocket = new ServerSocket(Constants.SERVER_SOCKET_PORT);
                client = serverSocket.accept();
                reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                writer = new PrintWriter(client.getOutputStream(), true);

                Log.d(TAG, "acceptClient: accepted client");

                writer.println(Constants.MSG_USERNAME + hostUsername);
                String line = reader.readLine();
                String username = line.substring(line.indexOf(":") + 1);

                handler.post(() -> {
                    listener.onClientConnected(username);
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void sendDrawUpdate(DrawData drawData) {
        writeExecutor.execute(() -> {
            writer.println(dataTransformer.transform(drawData));
            Log.d(TAG, "sendDrawUpdate");
        });
    }

    public void sendTimerUpdate(final String timer) {
        writeExecutor.execute(() -> {
            writer.println(Constants.MSG_TIMER + timer);
            Log.d(TAG, "sendTimerUpdate: " + timer);
        });
    }

    public void startListeningForClientUpdates(ClientListener listener) {
        readExecutor.execute(() -> {
            while (true) {
                try {
                    final String line = reader.readLine();
                    Log.d(TAG, "startListeningForClientUpdates: " + line);
                    if (line == null) {
                        Log.d(TAG, "startListeningForClientUpdates: null");
                        handler.post(listener::onGameEnded);

                        return;
                    }

                    if (line.startsWith(Constants.MSG_GUESS)) {
                        handleClientGuess(line, listener);
                    } else if (line.startsWith(Constants.MSG_RATING)) {
                        handleRating(line, listener);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        });
    }

    public void sendStartGame() {
        writeExecutor.execute(() -> {
            writer.println(Constants.MSG_START_GAME);
            Log.d(TAG, "sendStartGame");
        });
    }

    public void sendImage(String imageUrl) {
        writeExecutor.execute(() -> {
            writer.println(Constants.MSG_IMG + imageUrl);
            Log.d(TAG, "sendImage: " + imageUrl);
        });
    }

    public void close() {
        Log.d(TAG, "close: Closing started");

        if (client == null) {
            Utils.closeCloseable(serverSocket);
        }

        writeExecutor.execute(() -> {
            Utils.closeCloseable(serverSocket);
            Log.d(TAG, "close: Server closed");
            Utils.closeCloseable(client);
            Log.d(TAG, "close: Client closed");
            Utils.closeCloseable(writer);
            Log.d(TAG, "close: Writer closed");
            Utils.closeCloseable(reader);
            Log.d(TAG, "close: Reader closed");
        });
    }

    private void handleClientGuess(String line, ClientListener listener) {
        handler.post(() -> {
            listener.onClientGuess(line.substring(line.indexOf(":") + 1));
        });
    }

    private void handleRating(String line, ClientListener listener) {
        handler.post(() -> {
           listener.onClientRated(Float.parseFloat(line.substring(line.indexOf(":") + 1)));
        });
    }
}
