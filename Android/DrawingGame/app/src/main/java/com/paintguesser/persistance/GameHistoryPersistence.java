package com.paintguesser.persistance;

import java.util.List;

public interface GameHistoryPersistence {

    interface Result<T> {
        void onSuccess(T data);
    }

    void saveGameHistory(final GameHistory gameHistory, Result<Void> result);
    void loadGameHistories(Result<List<GameHistory>> gameHistories);
}
