package com.paintguesser.view.game;

import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.paintguesser.App;
import com.paintguesser.R;
import com.paintguesser.databinding.FragmentGameHostBinding;
import com.paintguesser.network.socket.DrawData;
import com.paintguesser.network.socket.server.SeverDrawDataTransformer;
import com.paintguesser.persistance.GameHistory;
import com.paintguesser.view.ColorPickerDialog;
import com.paintguesser.view.ui.DrawingView;
import com.paintguesser.GameConstants;
import com.paintguesser.network.backend.BackendRepository;
import com.paintguesser.network.backend.data.GeneratedGuess;
import com.paintguesser.network.socket.server.Server;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

public class GameHostFragment extends GameFragment {

    private static final String TAG = "GameHostFragment";

    private FragmentGameHostBinding binding;
    private GeneratedGuess generatedGuess;
    private Timer timer;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentGameHostBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        handler = new Handler(Looper.getMainLooper());
        binding.drawingView.setPlayable(false);
        binding.btnUndo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.drawingView.undo();
            }
        });

        binding.tvRival.setText(getString(R.string.guesser) + ": " + rivalUsername);

        binding.ivColorPicker.setOnClickListener(v -> {
            ColorPickerDialog dialog = new ColorPickerDialog(v.getContext());
            dialog.setListener(color -> {
                binding.drawingView.setPaintColor(color);
                binding.ivColorPicker.setImageTintList(ColorStateList.valueOf(color));
            });

            dialog.show();
        });

        prepareHost();
    }

    private void prepareHost() {
        final AtomicInteger seconds = new AtomicInteger(GameConstants.SERVER_PREPARE_SECONDS);

        App.backendRepository.getGeneratedGuess(new BackendRepository.OnResultListener<GeneratedGuess>() {
            @Override
            public void onSuccess(GeneratedGuess guess) {
                App.backendRepository.getImageBitmap(guess.url, new BackendRepository.OnResultListener<Bitmap>() {
                    @Override
                    public void onSuccess(Bitmap bitmap) {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.linearProgress.setVisibility(View.VISIBLE);
                        binding.imageView.setVisibility(View.VISIBLE);
                        binding.imageView.setImageBitmap(bitmap);
                        binding.linearProgress.setProgress(seconds.get(), false);
                        generatedGuess = guess;

                        timer = new Timer();

                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                if (seconds.get() < 0) {
                                    handler.post(() -> {
                                        cancel();
                                        startHostGame();
                                    });
                                }
                                handler.post(() -> {
                                    binding.linearProgress.setProgress(seconds.get(), true);
                                });
                                seconds.decrementAndGet();
                            }
                        }, 0, 1000);
                    }

                    @Override
                    public void onError() {
                        Toast.makeText(getContext(), R.string.image_download_error, Toast.LENGTH_LONG).show();
                        App.server.close();
                        onGameEnd();
                    }
                });
            }

            @Override
            public void onError() {
                Toast.makeText(getContext(), R.string.image_download_error, Toast.LENGTH_LONG).show();
                App.server.close();
                onGameEnd();
            }
        });
    }

    private void startHostGame() {
        final AtomicInteger seconds = new AtomicInteger(GameConstants.GAME_SECONDS);
        final String timeLeftText = getString(R.string.time_left);

        binding.drawingView.setPlayable(true);
        binding.screenShadow.setVisibility(View.GONE);
        binding.tvTitle.setVisibility(View.GONE);
        binding.linearProgress.setVisibility(View.GONE);
        binding.imageView.setImageBitmap(null);
        binding.imageView.setVisibility(View.GONE);

        App.server.sendStartGame();

        binding.drawingView.setDrawingUpdate(new DrawingView.DrawingListener() {
            @Override
            public void onDraw(DrawData drawData) {
                App.server.sendDrawUpdate(drawData);
            }
        });

        timer = new Timer();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                String timeLeft = String.valueOf(seconds.decrementAndGet());
                if (seconds.get() < 0) {
                    cancel();
                    handler.post(() -> {
                        binding.drawingView.setPlayable(false);
                        binding.tvTitle.setText(R.string.other_player_guessing);
                        binding.progressBar.setVisibility(View.VISIBLE);
                        binding.tvTitle.setVisibility(View.VISIBLE);
                        binding.btnUndo.setVisibility(View.GONE);
                        binding.screenShadow.setVisibility(View.VISIBLE);
                        stopTimeLeftService();
                    });
                } else {
                    App.server.sendTimerUpdate(timeLeft);
                    handler.post(() -> {
                        binding.tvTimer.setText(timeLeftText + " " + timeLeft);
                        updateTimeLeftService(timeLeft);
                    });
                }
            }
        }, 1000, 1000);

        App.server.startListeningForClientUpdates(new Server.ClientListener() {
            @Override
            public void onClientGuess(String guess) {
                binding.tvTitle.setText(getString(R.string.guess) + ": " + guess);
                binding.progressBar.setVisibility(View.GONE);
                GameHostFragment.this.guess = guess;

                App.server.sendImage(generatedGuess.url);
            }

            @Override
            public void onClientRated(float rating) {
                binding.ratingBar.setVisibility(View.VISIBLE);
                binding.tvRatingTitle.setVisibility(View.VISIBLE);
                binding.ratingBar.setRating(rating);
                GameHostFragment.this.rating = rating;

                handler.postDelayed(() -> {
                    App.server.close();
                    SeverDrawDataTransformer transformer = new SeverDrawDataTransformer();
                    App.persistence.saveGameHistory(new GameHistory(
                            GameHistory.Player.PAINTER,
                            rating,
                            transformer.transform(binding.drawingView.getDrawData()),
                            Calendar.getInstance().getTimeInMillis(),
                            rivalUsername,
                            guess
                    ), data -> {
                        onGameEnd();
                    });
                }, GameConstants.OBSERVE_RATING_MS);
            }

            @Override
            public void onGameEnded() {
                App.server.close();
                onGameEnd();
            }
        });
    }

    @Override
    protected void disconnect() {
        super.disconnect();
        App.server.close();
        if (timer != null) {
            timer.cancel();
        }
    }
}
