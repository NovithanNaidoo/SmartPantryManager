package com.example.smartpantrymanager.data;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Stores the user's settings.
 *
 * Settings are a handful of small values, not records, so SharedPreferences suits
 * them better than the database. Android saves them to a small file automatically
 * and they survive the app being closed, exactly like the pantry data does.
 *
 * Wrapping SharedPreferences in this class means the key names are written once
 * here instead of being retyped in every screen that needs them. A typo in a key
 * would silently read the wrong value rather than causing an error.
 */
public class AppPreferences {

    private static final String FILE_NAME = "smart_pantry_settings";

    private static final String KEY_EXPIRY_ALERTS = "expiry_alerts_enabled";
    private static final String KEY_EXPIRY_DAYS = "expiry_alert_days";

    /** Alerts are on by default, since reducing waste is the point of the app. */
    private static final boolean DEFAULT_EXPIRY_ALERTS = true;
    private static final int DEFAULT_EXPIRY_DAYS = 3;

    private final SharedPreferences preferences;

    public AppPreferences(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    }

    /** Whether items close to expiry should be highlighted in the pantry list. */
    public boolean isExpiryAlertsEnabled() {
        return preferences.getBoolean(KEY_EXPIRY_ALERTS, DEFAULT_EXPIRY_ALERTS);
    }

    public void setExpiryAlertsEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_EXPIRY_ALERTS, enabled).apply();
    }

    /** How many days ahead counts as "expiring soon". */
    public int getExpiryAlertDays() {
        return preferences.getInt(KEY_EXPIRY_DAYS, DEFAULT_EXPIRY_DAYS);
    }

    public void setExpiryAlertDays(int days) {
        // apply() writes in the background. commit() would block until the write
        // finished, which is unnecessary for a setting this small.
        preferences.edit().putInt(KEY_EXPIRY_DAYS, days).apply();
    }
}
