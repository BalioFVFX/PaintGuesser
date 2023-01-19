package com.paintguesser.network.backend;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.paintguesser.GameConstants;
import com.paintguesser.network.backend.data.GeneratedGuess;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BackendRepository {

    public interface OnResultListener<T> {
        void onSuccess(T data);
        void onError();
    }

    private static final String TAG = "BackendRepository";
    private static final String BASE_URL = GameConstants.BACKEND_BASE_URL + "/api/generate-guess";
    private static final int CONNECT_TIMEOUT = 5000;
    private static final int READ_TIMEOUT = 5000;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    public void getGeneratedGuess(OnResultListener<GeneratedGuess> listener) {
        Log.d(TAG, "getGeneratedGuess: called");
        executor.execute(() -> {
            HttpURLConnection urlConnection = null;
            BufferedReader inputStream = null;
            try {
                final URL url = new URL(BASE_URL);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setConnectTimeout(CONNECT_TIMEOUT);
                urlConnection.setReadTimeout(READ_TIMEOUT);
                inputStream = new BufferedReader(
                        new InputStreamReader(urlConnection.getInputStream()));

                final StringBuilder builder = new StringBuilder();
                String line = "";

                while ((line = inputStream.readLine()) != null) {
                    builder.append(line);
                }

                final JSONObject jsonObject = new JSONObject(builder.toString());

                handler.post(() -> {
                    try {
                        listener.onSuccess(new GeneratedGuess(
                                jsonObject.getString("url"),
                                jsonObject.getString("type")));

                        Log.d(TAG, "getGeneratedGuess: success");
                    } catch (JSONException e) {
                        Log.e(TAG, "getGeneratedGuess: error");
                        e.printStackTrace();
                        listener.onError();
                    }
                });

            } catch (IOException | JSONException e) {
                Log.e(TAG, "getGeneratedGuess: error");
                e.printStackTrace();
                handler.post(listener::onError);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        handler.post(listener::onError);
                    }
                }
            }
        });
    }

    public void getImageBitmap(final String imgUrl, OnResultListener<Bitmap> listener) {
        Log.d(TAG, "getImageBitmap: called");
        executor.execute(() -> {
            HttpURLConnection urlConnection = null;
            InputStream inputStream = null;
            try {
                final URL url = new URL(imgUrl);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setConnectTimeout(CONNECT_TIMEOUT);
                urlConnection.setReadTimeout(READ_TIMEOUT);
                inputStream = urlConnection.getInputStream();

                final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                handler.post(() -> {
                    if (bitmap != null) {
                        listener.onSuccess(bitmap);
                        Log.d(TAG, "getImageBitmap: success");
                    } else {

                    }
                });

            } catch (IOException e) {
                Log.e(TAG, "getImageBitmap: error");
                handler.post(() -> {
                    listener.onError();
                });

                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
