package com.paintguesser.persistance;

public class GameHistory {

    public enum Player {
        PAINTER,
        GUESSER
    }

    public final long id;
    public final Player player;
    public final float rating;
    public final String canvasData;
    public long timestamp;
    public final String rivalUsername;
    public final String guess;

    public GameHistory(long id, Player player, float rating, String canvasData, long timestamp, String rivalUsername, String guess) {
        this.id = id;
        this.player = player;
        this.rating = rating;
        this.canvasData = canvasData;
        this.timestamp = timestamp;
        this.rivalUsername = rivalUsername;
        this.guess = guess;
    }

    public GameHistory(Player player, float rating, String canvasData, long timestamp, String rivalUsername, String guess) {
        this(-1, player, rating, canvasData, timestamp, rivalUsername, guess);
    }
}
