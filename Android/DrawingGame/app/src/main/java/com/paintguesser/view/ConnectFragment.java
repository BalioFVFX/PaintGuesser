package com.paintguesser.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.paintguesser.App;
import com.paintguesser.R;
import com.paintguesser.databinding.FragmentConnectBinding;
import com.paintguesser.network.socket.Constants;
import com.paintguesser.network.socket.client.Client;
import com.paintguesser.view.game.GameClientFragment;
import com.paintguesser.view.game.GameFragment;
import com.paintguesser.view.game.GameHostFragment;

public class ConnectFragment extends Fragment {

    private static final int EMULATOR_SERVER_PORT = 6000;

    FragmentConnectBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentConnectBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.radioHost.setChecked(true);
        binding.hostTextInputLayout.setVisibility(View.GONE);
        binding.checkboxEmulator.setVisibility(View.GONE);

        binding.radioHost.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    binding.btnCreateJoinGame.setText(R.string.create_game);
                    binding.hostTextInputLayout.setVisibility(View.GONE);
                    binding.checkboxEmulator.setVisibility(View.GONE);
                }
            }
        });

        binding.radioJoin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    binding.btnCreateJoinGame.setText(R.string.join_game);
                    binding.hostTextInputLayout.setVisibility(View.VISIBLE);
                    binding.checkboxEmulator.setVisibility(View.VISIBLE);
                }
            }
        });

        binding.btnCreateJoinGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.radioHost.isChecked()) {
                    hostGame();
                } else {
                    App.preferences.saveHost(binding.hostEditText.getText().toString());
                    joinGame();
                }

                App.preferences.saveUsername(binding.usernameEditText.getText().toString());
            }
        });

        binding.checkboxEmulator.setOnCheckedChangeListener((buttonView, isChecked) -> {
            App.preferences.saveConnectingToEmulator(isChecked);
        });

        binding.usernameEditText.setText(App.preferences.getLastUsername());
        binding.hostEditText.setText(App.preferences.getLastHost());
    }

    private void joinGame() {

        final int port;

        if (binding.checkboxEmulator.isChecked()) {
            port = EMULATOR_SERVER_PORT;
        } else {
            port = Constants.SERVER_SOCKET_PORT;
        }

        App.client.connect(
                binding.hostEditText.getText().toString(),
                port,
                binding.usernameEditText.getText().toString(),
                new Client.ConnectListener() {

                    @Override
                    public void onConnected(String username) {
                        final GameFragment fragment = new GameClientFragment();
                        final Bundle bundle = new Bundle(1);
                        bundle.putString(GameFragment.RIVAL_USERNAME_KEY, username);
                        fragment.setArguments(bundle);

                        getParentFragmentManager()
                                .beginTransaction()
                                .replace(R.id.fragment_container, fragment, GameFragment.FRAGMENT_TAG)
                                .addToBackStack(GameFragment.class.getName())
                                .commit();
                    }

                    @Override
                    public void onFailure() {
                        Toast.makeText(getContext(), R.string.connection_failed, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void hostGame() {
        binding.tvTitle.setVisibility(View.GONE);
        binding.usernameTextInputLayout.setVisibility(View.GONE);
        binding.hostTextInputLayout.setVisibility(View.GONE);
        binding.radioGroup.setVisibility(View.GONE);
        binding.btnCreateJoinGame.setVisibility(View.GONE);

        binding.progressCircular.setVisibility(View.VISIBLE);
        binding.tvStatus.setVisibility(View.VISIBLE);
        

        App.server.acceptClient(binding.usernameEditText.getText().toString(), username -> {
            final GameFragment fragment = new GameHostFragment();
            final Bundle bundle = new Bundle(1);
            bundle.putString(GameFragment.RIVAL_USERNAME_KEY, username);
            fragment.setArguments(bundle);

            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment, GameFragment.FRAGMENT_TAG)
                    .addToBackStack(GameFragment.class.getName())
                    .commit();
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        App.server.close();
        App.client.close();
    }
}