package com.paintguesser.view.game;

import android.app.AlertDialog;
import android.content.Intent;
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
import com.paintguesser.TimerService;
import com.paintguesser.databinding.FragmentGameClientBinding;
import com.paintguesser.network.socket.DrawData;
import com.paintguesser.network.socket.server.SeverDrawDataTransformer;
import com.paintguesser.persistance.GameHistory;
import com.paintguesser.network.socket.client.Client;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;

public class GameClientFragment extends GameFragment {

    private FragmentGameClientBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentGameClientBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        handler = new Handler(Looper.getMainLooper());
        binding.drawingView.setPlayable(false);

        binding.ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            if (fromUser) {
                App.client.sendRating(rating);
                binding.ratingBar.setEnabled(false);
                this.rating = rating;
            }
        });

        binding.tvRival.setText(getString(R.string.painter) + ": " + rivalUsername);

        prepareClient();
    }

    private void prepareClient() {
        binding.linearProgress.setVisibility(View.GONE);
        binding.drawingView.setPlayable(false);
        final String timeLeftText = getString(R.string.time_left);

        App.client.listenForUpdates(new Client.GameUpdatesListener() {

            @Override
            public void onUpdate(DrawData drawData) {
                binding.drawingView.update(drawData);
            }

            @Override
            public void onTimerUpdate(String time) {
                binding.tvTimer.setText(timeLeftText + " " + time);

                if (time.equals("0")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    View dialogView = getLayoutInflater().inflate(R.layout.dialog_guess, null);
                    builder.setView(dialogView);

                    builder.setPositiveButton(R.string.guess, (dialog, which) -> {
                        TextInputEditText editText = dialogView.findViewById(R.id.edit_text);
                        App.client.sendGuess(editText.getText().toString());
                        guess = editText.getText().toString();
                        binding.progressBar.setVisibility(View.VISIBLE);
                        binding.screenShadow.setVisibility(View.VISIBLE);
                    });
                    AlertDialog dialog = builder.create();
                    dialog.setCancelable(false);
                    dialog.show();
                    stopTimeLeftService();
                } else {
                    updateTimeLeftService(time);
                }
            }

            @Override
            public void onGameStart() {
                binding.screenShadow.setVisibility(View.GONE);
                binding.tvTitle.setVisibility(View.GONE);
                binding.progressBar.setVisibility(View.GONE);
                binding.linearProgress.setVisibility(View.GONE);
                binding.imageView.setVisibility(View.GONE);
                rating = -1;
            }

            @Override
            public void onImageReceived(Bitmap bitmap) {
                if (bitmap == null) {
                    Toast.makeText(getContext(), R.string.image_download_error, Toast.LENGTH_LONG).show();
                }

                binding.screenShadow.setVisibility(View.VISIBLE);
                binding.progressBar.setVisibility(View.GONE);
                binding.imageView.setVisibility(View.VISIBLE);
                binding.imageView.setImageBitmap(bitmap);
                binding.ratingBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onGameEnded() {
                if (rating == -1) {
                    onGameEnd();
                    return;
                }

                GameHistory gameHistory = new GameHistory(
                        GameHistory.Player.GUESSER,
                        binding.ratingBar.getRating(),
                        new SeverDrawDataTransformer().transform(binding.drawingView.getDrawData()),
                        Calendar.getInstance().getTimeInMillis(),
                        rivalUsername,
                        guess
                );

                App.persistence.saveGameHistory(gameHistory, data -> {
                    onGameEnd();
                });
            }
        });
    }

    @Override
    protected void disconnect() {
        super.disconnect();
        App.client.close();
        getActivity().stopService(new Intent(getContext(), TimerService.class));
    }
}
