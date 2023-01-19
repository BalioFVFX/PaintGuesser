package com.paintguesser.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;

import com.paintguesser.R;
import com.paintguesser.databinding.ActivityMainBinding;
import com.paintguesser.view.game.GameFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container, new MainFragment())
                .commit();
    }

    @Override
    public void onBackPressed() {
        final int backStackSize = getSupportFragmentManager().getBackStackEntryCount() - 1;

        if (backStackSize < 0) {
            super.onBackPressed();
            return;
        }

        final FragmentManager.BackStackEntry backStackEntry = getSupportFragmentManager()
                .getBackStackEntryAt(backStackSize);

        if (GameFragment.class.getName().equals(backStackEntry.getName())) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.cancel_game_dialog_title)
                    .setMessage(R.string.cancel_game_dialog_message)
                    .setPositiveButton(R.string.Ok, (dialog, which) ->
                            MainActivity.super.onBackPressed())
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        } else {
            super.onBackPressed();
        }
    }
}