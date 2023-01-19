package com.paintguesser.persistance;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferences {

    private final static String HOST_KEY = "host";
    private final static String USERNAME_KEY = "username";
    private final static String CONNECTING_TO_EMULATOR_KEY = "connecting_to_emulator";

    private final SharedPreferences preferences;

    public Preferences(Context context) {
        preferences = context.getSharedPreferences("prefs", Context.MODE_PRIVATE);
    }

    public String getLastHost() {
        return preferences.getString(HOST_KEY, "");
    }

    public String getLastUsername() {
        return preferences.getString(USERNAME_KEY, "");
    }

    public boolean getConnectingToEmulator() {
        return preferences.getBoolean(CONNECTING_TO_EMULATOR_KEY, false);
    }

    public void saveHost(String host) {
        preferences.edit().putString(HOST_KEY, host).apply();
    }

    public void saveUsername(String username) {
        preferences.edit().putString(USERNAME_KEY, username).apply();
    }

    public void saveConnectingToEmulator(boolean value) {
        preferences.edit().putBoolean(CONNECTING_TO_EMULATOR_KEY, value).apply();
    }
}
