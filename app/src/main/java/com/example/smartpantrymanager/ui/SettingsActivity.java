package com.example.smartpantrymanager.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.smartpantrymanager.R;
import com.example.smartpantrymanager.data.AppPreferences;
import com.google.android.material.materialswitch.MaterialSwitch;

/**
 * Lets the user turn expiring-soon highlighting on or off, and choose how many
 * days ahead counts as "soon".
 *
 * Settings are saved the moment they are changed rather than behind a Save
 * button, which is the normal pattern on Android and means nothing is lost if
 * the user presses back.
 */
public class SettingsActivity extends AppCompatActivity {

    /** The choices offered in the days dropdown. */
    private static final int[] DAY_OPTIONS = {1, 3, 7, 14};

    private AppPreferences preferences;
    private Spinner spinnerDays;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        BottomNavHelper.setup(this, R.id.nav_settings);

        preferences = new AppPreferences(this);

        setupSwitch();
        setupDaysSpinner();
    }

    private void setupSwitch() {
        MaterialSwitch switchAlerts = findViewById(R.id.switchExpiryAlerts);

        // Show the saved value first, then start listening. Setting the value
        // would otherwise fire the listener straight away and save it again.
        switchAlerts.setChecked(preferences.isExpiryAlertsEnabled());

        switchAlerts.setOnCheckedChangeListener((button, isChecked) -> {
            preferences.setExpiryAlertsEnabled(isChecked);
            spinnerDays.setEnabled(isChecked);
        });

        spinnerDays = findViewById(R.id.spinnerDays);
        spinnerDays.setEnabled(preferences.isExpiryAlertsEnabled());
    }

    private void setupDaysSpinner() {
        String[] labels = new String[DAY_OPTIONS.length];
        for (int i = 0; i < DAY_OPTIONS.length; i++) {
            int days = DAY_OPTIONS[i];
            labels[i] = days == 1 ? "1 day" : days + " days";
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, labels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDays.setAdapter(adapter);

        // Select whichever option matches the saved setting.
        int saved = preferences.getExpiryAlertDays();
        for (int i = 0; i < DAY_OPTIONS.length; i++) {
            if (DAY_OPTIONS[i] == saved) {
                spinnerDays.setSelection(i);
                break;
            }
        }

        spinnerDays.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                preferences.setExpiryAlertDays(DAY_OPTIONS[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Nothing to do. A spinner always has something selected.
            }
        });
    }
}
