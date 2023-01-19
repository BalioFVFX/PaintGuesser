package com.paintguesser.persistance;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferences {

    private final static String HOST_KEY = "host";
    private final static String USERNAME_KEY = "username";

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

    public void saveHost(String host) {
        preferences.edit().putString(HOST_KEY, host).apply();
    }

    public void saveUsername(String username) {
        preferences.edit().putString(USERNAME_KEY, username).apply();
    }
}
