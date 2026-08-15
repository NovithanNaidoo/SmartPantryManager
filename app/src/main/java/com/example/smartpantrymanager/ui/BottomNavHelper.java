package com.example.smartpantrymanager.ui;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;

import com.example.smartpantrymanager.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Wires up the bottom navigation bar on the four main screens.
 *
 * Each screen is its own Activity, so without this the same switching code would
 * be copied into all four of them. Putting it here means the behaviour is written
 * once and every screen navigates identically.
 */
final class BottomNavHelper {

    private BottomNavHelper() {
        // Utility class, never instantiated.
    }

    /**
     * Sets up the bar and handles taps.
     *
     * @param activity      the screen the bar is on
     * @param currentItemId which tab should appear selected
     */
    static void setup(@NonNull Activity activity, @IdRes int currentItemId) {
        BottomNavigationView navigation = activity.findViewById(R.id.bottomNavigation);
        if (navigation == null) {
            return;
        }

        // Marking the current tab before adding the listener, so highlighting it
        // does not immediately trigger a navigation to the screen already open.
        navigation.setSelectedItemId(currentItemId);

        navigation.setOnItemSelectedListener(item -> {
            int selectedId = item.getItemId();

            // Tapping the tab you are already on should do nothing.
            if (selectedId == currentItemId) {
                return true;
            }

            Class<?> destination = screenFor(selectedId);
            if (destination == null) {
                return false;
            }

            Intent intent = new Intent(activity, destination);

            // CLEAR_TOP brings an already-open screen back to the front instead of
            // opening a second copy, and SINGLE_TOP reuses it rather than recreating
            // it. Together they stop the back stack filling up with duplicate
            // screens as the user moves between tabs.
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            activity.startActivity(intent);

            // No slide animation, so switching tabs feels instant rather than like
            // opening a new page.
            activity.overridePendingTransition(0, 0);

            return true;
        });
    }

    /** Maps a tab id to the screen it opens. */
    private static Class<?> screenFor(int itemId) {
        if (itemId == R.id.nav_pantry) {
            return PantryListActivity.class;
        }
        if (itemId == R.id.nav_suggested) {
            return SuggestedRecipesActivity.class;
        }
        if (itemId == R.id.nav_recipes) {
            return AllRecipesActivity.class;
        }
        if (itemId == R.id.nav_settings) {
            return SettingsActivity.class;
        }
        return null;
    }
}
