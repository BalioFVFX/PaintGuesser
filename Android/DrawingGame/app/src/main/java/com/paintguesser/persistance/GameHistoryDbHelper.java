package com.paintguesser.persistance;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.os.Looper;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class GameHistoryDbHelper extends SQLiteOpenHelper implements GameHistoryPersistence {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "GameHistory.db";

    private static final String TAG = "GameHistoryDbHelper";

    private static class Entry implements BaseColumns {
        private static final String TABLE_NAME = "game_history";
        private static final String COLUMN_NAME_PLAYER_TYPE = "player_type";
        private static final String COLUMN_NAME_CANVAS_DATA = "canvas_data";
        private static final String COLUMN_NAME_RATING = "rating";
        private static final String COLUMN_RIVAL_USERNAME = "rival_username";
        private static final String COLUMN_GUESS = "guess";
        private static final String COLUMN_NAME_TIMESTAMP = "timestamp";
    }

    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    public GameHistoryDbHelper(Context context) {
        super(context,DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + Entry.TABLE_NAME + " (" +
                Entry._ID + " INTEGER PRIMARY KEY," +
                Entry.COLUMN_NAME_PLAYER_TYPE + " INT," +
                Entry.COLUMN_NAME_RATING + " REAL," +
                Entry.COLUMN_NAME_TIMESTAMP + " LONG," +
                Entry.COLUMN_RIVAL_USERNAME + " TEXT," +
                Entry.COLUMN_GUESS + " TEXT," +
                Entry.COLUMN_NAME_CANVAS_DATA + " TEXT)";

        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @Override
    public void saveGameHistory(GameHistory gameHistory, Result<Void> result) {
        Log.d(TAG, "saveGameHistory: ");
        executor.execute(() -> {
            ContentValues values = new ContentValues();

            values.put(Entry.COLUMN_NAME_PLAYER_TYPE, gameHistory.player.ordinal());
            values.put(Entry.COLUMN_NAME_CANVAS_DATA, gameHistory.canvasData);
            values.put(Entry.COLUMN_NAME_RATING, gameHistory.rating);
            values.put(Entry.COLUMN_NAME_TIMESTAMP, gameHistory.timestamp);
            values.put(Entry.COLUMN_RIVAL_USERNAME, gameHistory.rivalUsername);
            values.put(Entry.COLUMN_GUESS, gameHistory.guess);

            getWritableDatabase().insert(Entry.TABLE_NAME, null, values);

            handler.post(() -> {
                result.onSuccess(null);
            });
        });
    }

    @Override
    public void loadGameHistories(Result<List<GameHistory>> result) {
        Log.d(TAG, "loadGameHistories: ");
        executor.execute(() -> {
            Cursor cursor = getReadableDatabase().query(
                    Entry.TABLE_NAME,
                    null,
                    null,
                    null,
                    null,
                    null,
                    Entry.COLUMN_NAME_TIMESTAMP + " DESC"
            );

            List<GameHistory> gameHistories = new ArrayList<>();

            while(cursor.moveToNext()) {
                final long id = cursor.getLong(
                        cursor.getColumnIndexOrThrow(Entry._ID));
                final int playerType = cursor.getInt(
                        cursor.getColumnIndexOrThrow(Entry.COLUMN_NAME_PLAYER_TYPE));
                final float rating = cursor.getFloat(
                        cursor.getColumnIndexOrThrow(Entry.COLUMN_NAME_RATING));
                final String canvasData = cursor.getString(
                        cursor.getColumnIndexOrThrow(Entry.COLUMN_NAME_CANVAS_DATA));
                final long timestamp = cursor.getLong(
                        cursor.getColumnIndexOrThrow(Entry.COLUMN_NAME_TIMESTAMP));
                final String rivalUsername = cursor.getString(
                        cursor.getColumnIndexOrThrow(Entry.COLUMN_RIVAL_USERNAME));
                final String guess = cursor.getString(
                        cursor.getColumnIndexOrThrow(Entry.COLUMN_GUESS));

                gameHistories.add(new GameHistory(
                        id,
                        GameHistory.Player.values()[playerType],
                        rating,
                        canvasData,
                        timestamp,
                        rivalUsername,
                        guess
                ));
            }

            cursor.close();

            handler.post(() -> {
                result.onSuccess(gameHistories);
            });
        });
    }
}
