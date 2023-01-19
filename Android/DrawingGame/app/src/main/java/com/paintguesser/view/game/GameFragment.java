package com.paintguesser.view.game;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.paintguesser.R;
import com.paintguesser.TimerService;
import com.paintguesser.view.MainFragment;

public abstract class GameFragment extends Fragment {

    public static final String RIVAL_USERNAME_KEY = "rival_username";
    public static final String FRAGMENT_TAG = "GameFragment";
    
    private static final String TAG = "GameFragment";

    protected Handler handler;
    protected String rivalUsername;
    protected String guess;
    protected float rating;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");

        handler = new Handler(Looper.getMainLooper());
        rivalUsername = getArguments().getString(RIVAL_USERNAME_KEY, "");
    }

    protected final void onGameEnd() {
        Toast.makeText(getContext(), R.string.game_ended, Toast.LENGTH_LONG).show();

        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new MainFragment())
                .commitAllowingStateLoss();
    }

    protected void disconnect() {
        Log.d(TAG, "disconnect: ");
    }

    protected void updateTimeLeftService(String time) {
        final String timeLeftText = getString(R.string.time_left);
        final Intent timerServiceIntent = new Intent(getContext(), TimerService.class);

        timerServiceIntent.putExtra(TimerService.TIME_COMMAND_KEY, timeLeftText + " " + time);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getActivity().startForegroundService(timerServiceIntent);
        }
    }

    protected void stopTimeLeftService() {
        getActivity().stopService(new Intent(getContext(), TimerService.class));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView: ");
        disconnect();
    }
}
