package com.example.foodfestivalcrew;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF = "crew_session";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_EMAIL = "email";
    private final SharedPreferences prefs;

    public SessionManager(Context ctx) {
        prefs = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public void save(String token, String email) {
        prefs.edit().putString(KEY_TOKEN, token).putString(KEY_EMAIL, email).apply();
    }

    public String getToken() { return prefs.getString(KEY_TOKEN, ""); }
    public String getEmail() { return prefs.getString(KEY_EMAIL, ""); }

    public void clear() { prefs.edit().clear().apply(); }
}